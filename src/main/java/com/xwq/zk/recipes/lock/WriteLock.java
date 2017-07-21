package com.xwq.zk.recipes.lock;

import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**   
 * @ClassName: WriteLock   
 * @Description: 一个实现排它写锁或领导选举的方案。
 * 调用lock()方法启动获取锁的进程；可能会马上拿到锁或者会有一些延迟。
 * 可以尝试注册一个监听listener便于在拿到锁的时候进行调用逻辑；亦或调用isOwner()方法来看是否拿到锁。，
 * @author: XiaWenQiang
 * @date: 2017年7月21日 下午2:10:22   
 *      
 */
public class WriteLock extends ProtocolSupport{

	private static final Logger LOG = LoggerFactory.getLogger(WriteLock.class);
	
	private final String dir;           // 加锁节点的父目录路径
	private String id;                  // 获取锁的节点的id
	private ZNodeName idName;           // 当前ZNodeNamee
	private String ownerId;             // 锁拥有者id
	private String lastChildId;         // 最小序号节点id
	private byte[] data = {0x12, 0x34};
	private LockListener callback;
	private LockZooKeeperOperation zop;
	
	public WriteLock(ZooKeeper zookeeper, String dir, List<ACL> acl) {
		super(zookeeper);
		this.dir = dir;
		if(acl != null) {
			setAcl(acl);
		}
		this.zop = new LockZooKeeperOperation();
	}
	
	public WriteLock(ZooKeeper zookeeper, String dir, List<ACL> acl, LockListener callback) {
		this(zookeeper, dir, acl);
		this.callback = callback;
	}
	
	/**
	 * 尝试获取排它写锁，返回获取状态。
	 * 该方法调用可能会有延迟，因为需要当前锁的拥有者释放。
	 */
	public synchronized boolean lock() throws KeeperException, InterruptedException {
		if(isClosed()) {
			return false;
		}
		ensurePathExists(dir);
		return (Boolean) retryOperation(zop);
	}
	
	/**
	 * 如果不再使用锁，就移除锁或关联的节点。同时也从队列中移除获取锁的请求。，
	 */
	public synchronized void unlock() {
		if(!isClosed() && id != null) {
			try {
				ZooKeeperOperation zopdel = new ZooKeeperOperation() {
					
					@Override
					public boolean execute() throws KeeperException, InterruptedException {
						zookeeper.delete(id, -1);
						return Boolean.TRUE;
					}
				};
				zopdel.execute();
			} catch (KeeperException.NoNodeException e) {
				 // do nothing
			} catch (InterruptedException e) {
				 LOG.warn("Caught: " + e, e);
				 //set that we have been interrupted.
				 Thread.currentThread().interrupt();
			} catch (KeeperException e) {
                LOG.warn("Caught: " + e, e);
                throw (RuntimeException) new RuntimeException(e.getMessage()).initCause(e);
            } finally {
            	if(callback != null) {
            		callback.lockReleased();
            	}
            	id = null;
            }
		}
	}
	
	/**
	 * 该node是否是锁(或领导者)的所有者
	 */
	public boolean isOwner() {
		return id != null && ownerId != null && id.equals(ownerId);
	}

	public LockListener getCallback() {
		return callback;
	}
	
	public void setCallback(LockListener callback) {
		this.callback = callback;
	}
	public String getId() {
		return id;
	}
	public String getDir() {
		return dir;
	}
	
	/**
	 * @ClassName: LockWatcher   
	 * @Description: 在需要观察获取watch的时候调用 
	 * @author: XiaWenQiang
	 * @date: 2017年7月21日 下午3:50:53   
	 *
	 */
	private class LockWatcher implements Watcher {

		/**   
		 * @Title: process  
		 * @Description:   
		 * @param event   
		 * @see org.apache.zookeeper.Watcher#process(org.apache.zookeeper.WatchedEvent)   
		 */
		@Override
		public void process(WatchedEvent event) {
			LOG.debug("Watcher fired on path: " + event.getPath() + " state: " + 
					event.getState() + " type: " + event.getType());
			try {
				lock();
			} catch (Exception e) {
				LOG.warn("Failed to acquire lock: " + e, e);
			}
		}
		
	}
	
	
	/**
	 * 
	 * @ClassName: LockZooKeeperOperation   
	 * @Description: 主要的zookeeper操作
	 * @author: XiaWenQiang
	 * @date: 2017年7月21日 下午3:30:05   
	 *
	 */
	private class LockZooKeeperOperation implements ZooKeeperOperation {

		/**
		 * 
		 * @param prefix                 前缀节点
		 * @param zookeeper              客户端
		 * @param dir                    父节点目录
		 * @throws KeeperException      
		 * @throws InterruptedException
		 *
		 */
		private void findPrefixInChildren(String prefix, ZooKeeper zookeeper, String dir)
				throws KeeperException, InterruptedException {
			//在父节点目录下查找指定前缀的子节点
			List<String> names = zookeeper.getChildren(dir, false);
			for (String name : names) {
				if(name.startsWith(prefix)) {
					id = name;
					if(LOG.isDebugEnabled()) {
						LOG.debug("Found id created last time: " + id);
					}
					break;
				}
			}
			//如果当前节点id不存在则创建
			if(id == null) {
				id = zookeeper.create(dir + "/" + prefix, data,
						getAcl(), CreateMode.EPHEMERAL_SEQUENTIAL);
				if(LOG.isDebugEnabled()) {
					LOG.debug("Created id: " + id);
				}
			}
		}
		
		/**
		 * 获取锁操作过程令
		 */
		@Override
		public boolean execute() throws KeeperException, InterruptedException {
			do {
				if(id == null) {
					long sessionId = zookeeper.getSessionId();
					String prefix = "x-" + sessionId + "-";
					
					//观察在创建znode过程中失败情况下的当前id
					findPrefixInChildren(prefix, zookeeper, dir);
					idName = new ZNodeName(id);
				}
				
				if(id != null) {
					List<String> names = zookeeper.getChildren(dir, false);
					if(names.isEmpty()) {
						LOG.warn("No children in: " + dir + "when we've just " + 
								"created one! Let's recreated it...");
						//强制重建id
						id = null;
					}else{
						//明确排下序下
						SortedSet<ZNodeName> sortedNames = new TreeSet<ZNodeName>();
						for (String name : names) {
							sortedNames.add(new ZNodeName(dir + "/" + name));
						}
						
						ownerId = sortedNames.first().getName();
						SortedSet<ZNodeName> lessThanMe = sortedNames.headSet(idName);
						
						if(!lessThanMe.isEmpty()) {
							ZNodeName lastChildName = lessThanMe.last();
							lastChildId = lastChildName.getName();
							if(LOG.isDebugEnabled()) {
								LOG.debug("watching less than me node: " + lastChildId);
							}
							
							Stat stat = zookeeper.exists(lastChildId, new LockWatcher());
							if(stat != null) {
								return Boolean.FALSE;
							}else{
								LOG.warn("Could not find the" + 
										" stats for less than me: " + lastChildName.getName());
							}
						}else{
							if(isOwner()) {
								if(callback != null) {
									callback.lockAcquired();
								}
								return Boolean.TRUE;
							}
						}
					}
				}
			} while (id == null);
			return Boolean.FALSE;
		}
		
	}
	
}

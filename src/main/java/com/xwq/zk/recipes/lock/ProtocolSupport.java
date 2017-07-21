package com.xwq.zk.recipes.lock;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**   
 * @ClassName: ProtocolSupport   
 * @Description: 一个方案基础类，该方案实现提供了一些高阶帮助方法来和zookeeper协同工作。
 * @author: XiaWenQiang
 * @date: 2017年7月21日 上午10:42:53   
 *      
 */
public class ProtocolSupport {
	 private static final Logger LOG = LoggerFactory.getLogger(ProtocolSupport.class);
	 
	 protected final ZooKeeper zookeeper;
	 private AtomicBoolean closed = new AtomicBoolean(false);
	 private long retryDelay = 500L;
	 private int retryCount = 10;
	 private List<ACL> acl = ZooDefs.Ids.OPEN_ACL_UNSAFE;
	 
	 public ProtocolSupport(ZooKeeper zookeeper) {
		 this.zookeeper = zookeeper;
	 }
	 
	 /**
	  * 关闭此策略，释放所有zookeeper资源；
	  * 但保持zookeeper实例处于open状态
	  */
	 public void close() {
		 if(closed.compareAndSet(false, true)) {
			 doClose();
		 }
	 }
	 
	 /**
	  * 由继承类实现释放资源的关闭操作
	  */
	 protected void doClose() {
		 
	 }
	 
	 /**
	  * 执行给定的操作，如果连接失败则重试
	  */
	 protected Object retryOperation(ZooKeeperOperation operation) throws KeeperException, InterruptedException {
		 KeeperException exception = null;
		 for (int i = 0; i < retryCount; i++) {
			try {
				return operation.execute();
			} catch (KeeperException.SessionExpiredException e) {
				LOG.warn("Session expired for: " + zookeeper + " so reconnecting due to: " + e, e);
				throw e;
			} catch (KeeperException.ConnectionLossException e) {
				if(exception == null) {
					exception = e;
				}
				LOG.debug("Attempt " + i + " failed with connection loss so " + 
						"attempting to reconnect: " + e, e);
				retryDelay(i);
			}
		}
		throw exception;
	 }
	 
	 /**
	  * 保证节点path存在(无数据，当前权限，持久化类型)
	  */
	 protected void ensurePathExists(String path) {
		 ensureExists(path, null, acl, CreateMode.PERSISTENT);
	 }
	 
	 /**
	  *	保证给定的相关数据，权限及类型的节点path存在
	  */
	 protected void ensureExists(final String path, final byte[] data, 
			 final List<ACL> acl, final CreateMode flags) {
		 try {
			retryOperation(new ZooKeeperOperation() {
				
				@Override
				public boolean execute() throws KeeperException, InterruptedException {
					Stat stat = zookeeper.exists(path, false);
					if(stat != null) {
						return true;
					}
					zookeeper.create(path, data, acl, flags);
					return true;
				}
			});
		} catch (KeeperException e) {
			LOG.warn("Caught: " + e, e);
		} catch (InterruptedException e) {
			LOG.warn("Caught: " + e, e);
		}
	 }
	 
	 /**
	  * 如果非第一次尝试则执行重试延迟
	  */
	 protected void retryDelay(int attemptCount) {
		 if(attemptCount > 0) {
			 try {
				Thread.sleep(attemptCount * retryDelay);
			} catch (InterruptedException e) {
				LOG.debug("Failed to sleep: " + e, e);
			}
		 }
	 }
	 
	 /**
	  * 返回协议是否关闭
	  */
	 protected boolean isClosed() {
		 return closed.get();
	 }
	 
	 /**
	  * 返回zookeeper客户端实例
	  */
	 public ZooKeeper getZookeeper() {
		 return zookeeper;
	 }
	 

	 public long getRetryDelay() {
		 return retryDelay;
	 }

	 public void setRetryDelay(long retryDelay) {
		 this.retryDelay = retryDelay;
	 }

	 public List<ACL> getAcl() {
		 return acl;
	 }

	 public void setAcl(List<ACL> acl) {
		 this.acl = acl;
	 }
	
}

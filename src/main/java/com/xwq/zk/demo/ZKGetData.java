/**  
 *  
 * @Title: ZKGetData.java   
 * @Package com.xwq.zk.demo   
 * @Description: TODO(用一句话描述该文件做什么)   
 * @author: XiaWenQiang  
 * @date: 2017年7月18日 下午4:31:24   
 * @version V1.0     
 */
package com.xwq.zk.demo;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.CountDownLatch;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

/**   
 * @ClassName: ZKGetData   
 * @Description: TODO(这里用一句话描述这个类的作用)   
 * @author: XiaWenQiang
 * @date: 2017年7月18日 下午4:31:24   
 *      
 */
public class ZKGetData {

	private static ZooKeeper zk;
	private static ZookeeperConnection conn;
	
	//检查znode节点是否存在
	public static Stat znode_exists(String path) throws KeeperException, InterruptedException {
		return zk.exists(path, true);
	}
	
	public static void main(String[] args) {
		String path = "/MyFirstZnode";
	    final CountDownLatch connectedSignal = new CountDownLatch(1);
	    
	    try {
			conn = new ZookeeperConnection();
			zk = conn.connect("localhost");
			Stat stat = znode_exists(path);
			
			if(stat != null) {
				byte[] b = zk.getData(path, new Watcher() {
					
					//应用程序将等待ZooKeeper集合的进一步通知再处理
					//可以在zookeeper客户端set /MyFirstZnode HelloWorld 试试
					@Override
					public void process(WatchedEvent we) {
						if(we.getType() == Event.EventType.None) {
							switch (we.getState()) {
								case Expired:
									connectedSignal.countDown();
									break;
								}
						}else{
							String path = "/MyFirstZnode";
							try {
								byte[] bn = zk.getData(path, false, null);
								String data = new String(bn, "UTF-8");
								System.out.println("we data = " + data);
							} catch (KeeperException | InterruptedException | UnsupportedEncodingException e) {
								e.printStackTrace();
							}
						}
						
					}
				}, null);
				
				String data = new String(b, "UTF-8");
				System.out.println("stat data = " + data);
				connectedSignal.await();
			
			}else{
				System.out.println("Node does not exists");
			}
		} catch (IOException | InterruptedException | KeeperException e) {
			e.printStackTrace();
		}
	}
}

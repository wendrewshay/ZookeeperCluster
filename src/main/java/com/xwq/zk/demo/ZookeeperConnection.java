/**  
 *  
 * @Title: ZookeeperConnection.java   
 * @Package com.xwq.zk.demo   
 * @Description: TODO(用一句话描述该文件做什么)   
 * @author: XiaWenQiang  
 * @date: 2017年7月18日 下午3:28:40   
 * @version V1.0     
 */
package com.xwq.zk.demo;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooKeeper;

/**   
 * @ClassName: ZookeeperConnection   
 * @Description: TODO(这里用一句话描述这个类的作用)   
 * @author: XiaWenQiang
 * @date: 2017年7月18日 下午3:28:40   
 *      
 */
public class ZookeeperConnection {

	private ZooKeeper zoo;
	private final CountDownLatch connectedSignal = new CountDownLatch(1);
	
	//连接服务主机
	public ZooKeeper connect(String host) throws IOException, InterruptedException {
		zoo = new ZooKeeper(host, 5000, new Watcher() {
			
			@Override
			public void process(WatchedEvent evt) {
				if(evt.getState() == KeeperState.SyncConnected) {
					connectedSignal.countDown();
				}
			}
		});
		connectedSignal.await();
		return zoo;
	}
	
	//断开连接
	public void close() throws InterruptedException {
		zoo.close();
	}
}

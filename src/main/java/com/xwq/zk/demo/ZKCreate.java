/**  
 *  
 * @Title: ZKCreate.java   
 * @Package com.xwq.zk.demo   
 * @Description: TODO(用一句话描述该文件做什么)   
 * @author: XiaWenQiang  
 * @date: 2017年7月18日 下午3:37:56   
 * @version V1.0     
 */
package com.xwq.zk.demo;

import java.io.IOException;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;

/**   
 * @ClassName: ZKCreate   
 * @Description: TODO(这里用一句话描述这个类的作用)   
 * @author: XiaWenQiang
 * @date: 2017年7月18日 下午3:37:56   
 *      
 */
public class ZKCreate {

	private static ZooKeeper zk;
	private static ZookeeperConnection conn;
	
	//创建znode节点
	public static void create(String path, byte[] data) throws KeeperException, InterruptedException {
		zk.create(path, data, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
	}
	
	public static void main(String[] args) {
		String path = "/MyFirstZnode";
		byte[] data = "My First Zookeeper App".getBytes();
		
		try {
			conn = new ZookeeperConnection();
			zk = conn.connect("127.0.0.1");
			
			create(path, data);
			conn.close();
		} catch (IOException | InterruptedException | KeeperException e) {
			e.printStackTrace();
		}
	}
}

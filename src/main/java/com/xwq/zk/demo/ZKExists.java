/**  
 *  
 * @Title: ZKExists.java   
 * @Package com.xwq.zk.demo   
 * @Description: TODO(用一句话描述该文件做什么)   
 * @author: XiaWenQiang  
 * @date: 2017年7月18日 下午3:49:01   
 * @version V1.0     
 */
package com.xwq.zk.demo;

import java.io.IOException;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

/**   
 * @ClassName: ZKExists   
 * @Description: TODO(这里用一句话描述这个类的作用)   
 * @author: XiaWenQiang
 * @date: 2017年7月18日 下午3:49:01   
 *      
 */
public class ZKExists {

	private static ZooKeeper zk;
	private static ZookeeperConnection conn;
	
	//检查znode节点是否存在
	public static Stat znode_exists(String path) throws KeeperException, InterruptedException {
		return zk.exists(path, true);
	}
	
	public static void main(String[] args) {
		String path = "/MyFirstZnode";
		
		try {
			conn = new ZookeeperConnection();
			zk = conn.connect("127.0.0.1");
			
			Stat stat = znode_exists(path);
			if(stat != null) {
				System.out.println("Node exists and the node version is " + stat.getVersion());
			}else{
				System.out.println("Node does not exists");
			}
			conn.close();
		} catch (IOException | InterruptedException | KeeperException e) {
			e.printStackTrace();
		}
	}
}

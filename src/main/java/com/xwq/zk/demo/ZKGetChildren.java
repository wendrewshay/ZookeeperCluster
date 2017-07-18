/**  
 *  
 * @Title: ZKGetChildren.java   
 * @Package com.xwq.zk.demo   
 * @Description: TODO(用一句话描述该文件做什么)   
 * @author: XiaWenQiang  
 * @date: 2017年7月18日 下午5:16:50   
 * @version V1.0     
 */
package com.xwq.zk.demo;

import java.io.IOException;
import java.util.List;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

/**   
 * @ClassName: ZKGetChildren   
 * @Description: TODO(这里用一句话描述这个类的作用)   
 * @author: XiaWenQiang
 * @date: 2017年7月18日 下午5:16:50   
 *      
 */
public class ZKGetChildren {

	private static ZooKeeper zk;
   	private static ZookeeperConnection conn;
   	
   	public static Stat znode_exists(String path) throws KeeperException,InterruptedException {
	    return zk.exists(path,true);
   	}
   	
   	public static void main(String[] args) {
   		String path = "/MyFirstZnode";
   		
   		try {
			conn = new ZookeeperConnection();
			zk = conn.connect("127.0.0.1");
			
			Stat stat = znode_exists(path);
			if(stat != null) {
				List<String> children = zk.getChildren(path, false);
				for (int i = 0; i < children.size(); i++) {
					System.out.println(children.get(i));
				}
			}else{
				System.out.println("Node does not exists");
			}
		} catch (IOException | InterruptedException | KeeperException e) {
			e.printStackTrace();
		}
	}
}

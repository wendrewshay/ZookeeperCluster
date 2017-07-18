/**  
 *  
 * @Title: ZKDelete.java   
 * @Package com.xwq.zk.demo   
 * @Description: TODO(用一句话描述该文件做什么)   
 * @author: XiaWenQiang  
 * @date: 2017年7月18日 下午5:21:31   
 * @version V1.0     
 */
package com.xwq.zk.demo;

import java.io.IOException;
import java.util.List;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;

/**   
 * @ClassName: ZKDelete   
 * @Description: TODO(这里用一句话描述这个类的作用)   
 * @author: XiaWenQiang
 * @date: 2017年7月18日 下午5:21:31   
 *      
 */
public class ZKDelete {

	private static ZooKeeper zk;
	private static ZookeeperConnection conn;
	
	//删除节点
	public static void delete(String path) throws InterruptedException, KeeperException {
		List<String> children = zk.getChildren(path,false);
		for (int i = 0; i < children.size(); i++) {
			String childPath = path+"/"+children.get(i);
			zk.delete(childPath, zk.exists(childPath, true).getVersion());
		}
		zk.delete(path, zk.exists(path, true).getVersion());
	}
	
	public static void main(String[] args) {
		String path = "/MyFirstZnode";
		
		try {
			conn = new ZookeeperConnection();
			zk = conn.connect("127.0.0.1");
			
			delete(path);
		} catch (IOException | InterruptedException | KeeperException e) {
			e.printStackTrace();
		}
	}
}

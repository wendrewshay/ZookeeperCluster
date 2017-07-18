/**  
 *  
 * @Title: ZKSetData.java   
 * @Package com.xwq.zk.demo   
 * @Description: TODO(用一句话描述该文件做什么)   
 * @author: XiaWenQiang  
 * @date: 2017年7月18日 下午4:56:45   
 * @version V1.0     
 */
package com.xwq.zk.demo;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;

/**   
 * @ClassName: ZKSetData   
 * @Description: TODO(这里用一句话描述这个类的作用)   
 * @author: XiaWenQiang
 * @date: 2017年7月18日 下午4:56:45   
 *      
 */
public class ZKSetData {

	private static ZooKeeper zk;
	private static ZookeeperConnection conn;
	
	//更新数据
	public static void update(String path, byte[] data) throws KeeperException, InterruptedException {
		zk.setData(path, data, zk.exists(path, true).getVersion());
	}
	
	public static void main(String[] args) {
		String path= "/MyFirstZnode";
		byte[] data = "Success".getBytes();
		
		try {
	         conn = new ZookeeperConnection();
	         zk = conn.connect("127.0.0.1");
	         update(path, data);
	      } catch(Exception e) {
	         System.out.println(e.getMessage());
	      }
	}
}

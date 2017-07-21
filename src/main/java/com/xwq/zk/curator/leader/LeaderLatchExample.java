/**  
 *  
 * @Title: LeaderLatchExample.java   
 * @Package com.xwq.zk.curator   
 * @Description: TODO(用一句话描述该文件做什么)   
 * @author: XiaWenQiang  
 * @date: 2017年7月19日 上午11:45:16   
 * @version V1.0     
 */
package com.xwq.zk.curator.leader;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.leader.LeaderLatch;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.test.TestingServer;
import org.apache.curator.utils.CloseableUtils;

import com.google.common.collect.Lists;

/**   
 * 参考网址：http://colobu.com/tags/Curator/
 * @ClassName: LeaderLatchExample   
 * @Description: TODO(leader选举--示例1)   
 * @author: XiaWenQiang
 * @date: 2017年7月19日 上午11:45:16   
 *      
 */
public class LeaderLatchExample {

	private static final int CLIENT_QTY = 10;
	private static final String PATH = "/examples/leader";
	
	public static void main(String[] args) throws Exception {
		List<CuratorFramework> clients = Lists.newArrayList();
		List<LeaderLatch> examples = Lists.newArrayList();
		TestingServer server = new TestingServer();
		
		try {
			for (int i = 0; i < CLIENT_QTY; i++) {
				CuratorFramework client = CuratorFrameworkFactory.newClient(server.getConnectString(), new ExponentialBackoffRetry(1000, 3));
				clients.add(client);
				
				LeaderLatch example = new LeaderLatch(client, PATH, "Client #" + i);
				examples.add(example);
				
				client.start();
				example.start();
			}
			
			Thread.sleep(20000);
			
			LeaderLatch currentLeader = null;
			for (int i = 0; i < CLIENT_QTY; i++) {
				LeaderLatch example = examples.get(i);
				if(example.hasLeadership()) {
					currentLeader = example;
				}
			}
			System.out.println("current leader is " + currentLeader.getId());
			System.out.println("release the leader " + currentLeader.getId());
			currentLeader.close();
			
			examples.get(0).await(2, TimeUnit.SECONDS);
			System.out.println("Client #0 maybe is elected as the leader or not although it want to be");
			System.out.println("the new leader is " + examples.get(0).getLeader().getId());
			
			System.out.println("Press enter/return to quit\n");
			new BufferedReader(new InputStreamReader(System.in)).readLine();
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			System.out.println("Shutting down...");
			for (LeaderLatch exampleClient : examples) {
				CloseableUtils.closeQuietly(exampleClient);
			}
			for (CuratorFramework client : clients) {
				CloseableUtils.closeQuietly(client);
			}
			CloseableUtils.closeQuietly(server);
		}
	}
}

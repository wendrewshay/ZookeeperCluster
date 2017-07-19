package com.xwq.zk.curator;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListenerAdapter;
import org.apache.curator.framework.recipes.leader.LeaderSelector;
import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**   
 * @ClassName: LeaderLatchExample   
 * @Description: TODO(leader选举--示例2)
 * @author: XiaWenQiang
 * @date: 2017年7月19日 上午11:45:16   
 *      
 */
public class ExampleClient extends LeaderSelectorListenerAdapter implements Closeable {
	private final String name;
	private final LeaderSelector leaderSelector;
	private final AtomicInteger leaderCount = new AtomicInteger();
	
	public ExampleClient(CuratorFramework client, String path, String name) {
		this.name = name;
		leaderSelector = new LeaderSelector(client, path, this);
		leaderSelector.autoRequeue();//保证在此实例释放领导权之后还可能获得领导权。
	}
	public void start() throws IOException {
		leaderSelector.start();
	}
	@Override
	public void close() throws IOException {
		leaderSelector.close();
	}
	
	@Override
	public void takeLeadership(CuratorFramework client) throws Exception {
		final int waitSeconds = (int) (5 * Math.random()) + 1;
		System.out.println(name + " is now the leader. Waiting " + waitSeconds + " seconds...");
		System.out.println(name + " has been leader " + leaderCount.getAndIncrement() + " time(s) before.");
		try {
			Thread.sleep(TimeUnit.SECONDS.toMillis(waitSeconds));
		} catch (InterruptedException e) {
			System.err.println(name + " was interrupted.");
			Thread.currentThread().interrupt();
		} finally {
			System.out.println(name + " relinquishing leadership.\n");
		}
	}
}
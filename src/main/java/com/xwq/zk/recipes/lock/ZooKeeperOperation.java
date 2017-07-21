package com.xwq.zk.recipes.lock;

import org.apache.zookeeper.KeeperException;

/**   
 * @ClassName: ZooKeeperOperation   
 * @Description: 用来实现可重试操作的回调 
 * @author: XiaWenQiang
 * @date: 2017年7月21日 下午12:00:39   
 *      
 */
public interface ZooKeeperOperation {
	
	/**
	 * 执行操作-如果在操作期间与Zookeeper的连接断开可能会重试多次间
	 */
	public boolean execute() throws KeeperException, InterruptedException;
}

package com.xwq.zk.recipes.lock;

/**   
 * @ClassName: LockListener   
 * @Description: 锁的获取和释放监听回调   
 * @author: XiaWenQiang
 * @date: 2017年7月21日 上午10:03:33   
 *      
 */
public interface LockListener {

	/**
	 * 获取锁回调
	 */
	public void lockAcquired();
	
	 /**
	  * 释放锁回调
	  */
	public void lockReleased();
}

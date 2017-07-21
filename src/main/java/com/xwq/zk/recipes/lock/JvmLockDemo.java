package com.xwq.zk.recipes.lock;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**   
 * 分布式锁实现
 * @ClassName: JvmLock
 * @Description: JVM可重入锁示例中以日期时间作为订单号，但在多线程场景下JVM可重入锁并不能保证订单号唯一，
 * 				  在有可自增数时才唯一。 
 * @author: XiaWenQiang
 * @date: 2017年7月20日 上午9:43:25   
 *      
 */
public class JvmLockDemo {

	private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyyMMddHHmmssSSS");
	private static final int LEN = 10;
	
	public static void exec() {
		ExecutorService executorService = Executors.newFixedThreadPool(LEN);
		CountDownLatch latch = new CountDownLatch(1);
		Lock lock = new ReentrantLock();
		
		for (int i = 0; i < LEN; i++) {
			executorService.submit(new Runnable() {
				
				@Override
				public void run() {
					try {
						latch.await();
						lock.lock();
						System.out.println(getOrderNo());
						lock.unlock();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					
				}
			});
		}
		latch.countDown();
		executorService.shutdown();
	}
	
	//获取订单号，有个自增数num才可保证唯一
	static int num=0;
	protected static String getOrderNo() {
		return SDF.format(new Date())+num++;
	}
	
	public static void main(String[] args) {
		exec();
	}
}

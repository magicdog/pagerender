package com.hipu.render.thread;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.log4j.Logger;

import com.hipu.render.entity.MainRender;

/**
 * @ClassName: ThreadPoolMonitor
 * @Description: monitor the timeout thread and the thread pool information
 * @author shelton
 * @Date:2013-3-20
 *
 */
public class ThreadPoolMonitor implements Runnable{
	
	private static final Logger LOG = Logger.getLogger(MainRender.class);
	
	private MainRender mainThread;
	
	/**
	 * @Fields:threadPool:thread pool instance
	 */
	private ThreadPoolExecutor threadPool;
	
	/**
	 * @Fields: startTime : record the time when this object is generated
	 */
	private long startTime; 
	
	/**
	 * @Fields:threadStatus:record the thread start time
	 */
	private Map<Thread, Long> threadStatus;
	
	private int timeout;
	
	private Thread thread;
	
	private boolean isRunning = true;
	
	public ThreadPoolMonitor() {
		this.mainThread 	= MainRender.getInstance();
		this.threadPool 	= mainThread.getThreadPool();
		this.threadStatus 	= mainThread.getThreadStatus();
		this.timeout 		= mainThread.getTimeout();
		this.startTime		= System.currentTimeMillis();
		thread = new Thread(this);
		
	}

	@Override
	public void run() {
		while(isRunning) {
			LOG.info(getPoolInfo());
			try {
				checkThreadStatus();
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				LOG.error("Pool Monitor is error "+thread.getName());
			}
		}
	}
	
	public void start() {
		this.thread.start();
	}
	
	public void stop() {
		this.isRunning = false;
	}
	
	/**
	 * @Title: getPoolInfo
	 * @Description: get the information of thread pool
	 * @return information
	 * @throws
	 */
	public String getPoolInfo() {
		int activeCount 		= threadPool.getActiveCount();
		long completedTaskCount = threadPool.getCompletedTaskCount();
		long taskCount 			= threadPool.getTaskCount();
		int buffer 				= threadPool.getQueue().size();
		int retryCount 			= mainThread.getRetryCount();
		int rejectCount 		= mainThread.getRejecCount();
		int timeoutCount		= mainThread.getTimeoutCount();
		double second 			= (System.currentTimeMillis() - startTime) / 1000.0;
		
		StringBuffer sb = new StringBuffer("Task in Queue ");
		sb.append(buffer);
		sb.append(" Active Count ").append(activeCount);
		sb.append(" Completed Task ").append(completedTaskCount);
		sb.append(" retry count ").append(retryCount);
		sb.append(" reject count ").append(rejectCount);
		sb.append(" timeout count ").append(timeoutCount);
		sb.append(" Total Task ").append(taskCount);
		sb.append(" average count ").append(completedTaskCount/second);
		return sb.toString();
	}
	
	/**
	 * @Title: checkThreadStatus
	 * @Description: check the timeout thread
	 * @throws
	 */
	public void checkThreadStatus() {
		Iterator<Map.Entry<Thread, Long>> it = threadStatus.entrySet().iterator();
		while(it.hasNext()) {
			Map.Entry<Thread, Long> entry = it.next();
			//!!always have forwarding thread, and always timeout
			//LOG.info(entry.getKey().getName()+now+"  "+entry.getValue());
			//!entry.getKey().getName().contains("Forwarding") && 
			if ((System.currentTimeMillis() - entry.getValue() > timeout*1000)) {
				entry.getKey().interrupt();
				threadStatus.remove(entry.getKey());
				mainThread.addTimeoutCount();
				LOG.info("interrupted thread "+entry.getKey().getId()+" because of timeout。");
			}
		}
	}
	
	public void join() {
		try {
			this.thread.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			LOG.error("the monitor thread "+thread.getName()+" interrupted");
			return;
		}
	}

}

package com.hipu.render.entity;
import java.io.File;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.hipu.render.config.Config;
import com.hipu.render.thread.PageRenderThread;
import com.hipu.render.thread.ThreadPoolMonitor;

/**
 * @ClassName: MainThread
 * @Description: the main part of this program, contains browser entities, thread pool, and monitor thread 
 * usage:
 * 		MainThread mainThread = MainThread.getInstance();
 * 		mainThread.Initial();		//only initial one time
 * 		....
 * @author shelton
 * @Date:2013-3-14
 *
 */

public class MainRender {
	
	private static final Logger LOG = Logger.getLogger(MainRender.class);
	
	private Config conf;
	
	/**
	 * @Fields: timeout : the maximum time to load a page 
	 */
	private int timeout;
	/**
	 * @Fields: cacheDir : the directory of cache
	 */
	private String cacheDir;
	
	/**
	 * @Fields: threadpool : thrad pool to limit the number of running thread 
	 */
	private ThreadPoolExecutor threadPool;
	/**
	 * @Fields: bq : this blocking queue is used in thread pool
	 */
	private BlockingQueue<Runnable> bq;
	
	/**
	 * @Fields:browserPool:the browser pool object
	 */
	private BrowserPool browserPool;
	/**
	 * @Fields: browserCount : the amount of browser in browser pool
	 */
	private Integer browserCount = 0;
	
	/**
	 * @Fields:retryCount: the amount of task have been retried 
	 */
	private volatile Integer retryCount = 0;
	
	/**
	 * @Fields:rejectCount:the amount of task have been rejected
	 */
	private volatile Integer rejectCount = 0;
	
	/**
	 * @Fields:timeoutCount:the amount of thread which is timeout
	 */
	private volatile Integer timeoutCount = 0;
	
	/**
	 * @Fields:poolMonitor:the monitor thread 
	 */
	private ThreadPoolMonitor poolMonitor;
	/**
	 * @Fields:threadStatus:the start time of every thread, this data is used by MonitorThread to check the timeout thread.
	 */
	private Map<Thread, Long> threadStatus;
	
	private static MainRender mainThread = new MainRender();
	
	private MainRender(){
	}
	
	/**
	 * @Title: Initial
	 * @Description: initial the class
	 */
	public void Initial() {
		
		threadStatus 	= new ConcurrentHashMap<Thread, Long>();
		bq 				= new LinkedBlockingQueue<Runnable>();
		
		conf 			= Config.getInstance();
		timeout 		= conf.getInt(Config.RENDER_BROWSER_TIMEOUT);
		browserCount 	= conf.getInt(Config.RENDER_BROWSER_COUNT);
		threadPool 		= new ThreadPoolExecutor(browserCount, browserCount, Long.MAX_VALUE, TimeUnit.NANOSECONDS, bq);
		
		//make the directory of cache and initial BrowserPool
		cacheDir 		= conf.getString(Config.RENDER_BROWSER_CACHE_DIR);
		if (cacheDir.endsWith(File.separator)) {
			cacheDir 	= (String) cacheDir.subSequence(0, cacheDir.length()-1);
		}
		File dir = new File(cacheDir);
		if (dir.exists() && dir.canWrite() && dir.isDirectory()) {
			browserPool = BrowserPool.getInstance();
			browserPool.initial();
		} else {
			LOG.error("can not use cache dir.");
			System.exit(1);
		}
		
		//new the monitor thread
		poolMonitor = new ThreadPoolMonitor();
		
		LOG.info("MainThread Initial Success.");
	}

	public static MainRender getInstance() {
		if (mainThread == null)
			synchronized (mainThread) {
				if (mainThread == null)
					mainThread = new MainRender();
			}
		return mainThread;
	}
	
	/**
	 * @Title: start
	 * @Description: start the monitor thread 
	 */
	public void start() {
		poolMonitor.start();
		LOG.info("PoolMonitor Start Success.");
	}
	 
	/**
	 * @Title: submit
	 * @Description: submit the task to thread pool
	 * @param url : like http://www.hipu.com
	 * @return future, it will return null if encountered problem when submit
	 */
	public synchronized Future<String> submit(String url){
		if (url == null)
			return null;
		
		PageRenderThread renderthread = new PageRenderThread(url);
		Future<String> future = null;
		
		try {
			
			future = this.threadPool.submit(renderthread);
			
		}catch (RejectedExecutionException e) {
			// there is not enough space allocate this task
			LOG.warn("Task "+url+ "Rejected");
			this.addRejectCount();
			return future;
		}catch (Exception e) {
			// other exception
			LOG.warn("",e);
			return future;
		}
		
		return future;
	}
	
	/**
	 * @Title: addBrowser
	 * @Description: add new browser
	 * @param count 
	 */
	public void addBrowser(int count) {
		LOG.debug("add new browser "+count);
		browserPool.addBrowser(count, null);
		this.browserCount += count;
		threadPool.setMaximumPoolSize(this.browserCount);
		threadPool.setCorePoolSize(this.browserCount);
		LOG.debug(this.browserCount + count);
		
	}
	
	/**
	 * @Title: releaseBrowser
	 * @Description: release browser
	 * @param count 
	 */
	public void releaseBrowser(int count) {
		LOG.debug("releaseBrowser"+count);
		browserPool.removeBrowser(count);
		this.browserCount -= count;
		threadPool.setCorePoolSize(this.browserCount);
		threadPool.setMaximumPoolSize(this.browserCount);
		return;
	}
	
	public  Map<Thread, Long> getThreadStatus() {
		return threadStatus;
	}
	
	public ThreadPoolExecutor getThreadPool() {
		return threadPool;
	}
	
	public int getTimeout() {
		return timeout;
	}
	
	public void updateTimeout(int newCount) {
		if (newCount <=0 )
			return ;
		browserPool.setTimeout(newCount);
	}

	public void addRetryCount() {
		retryCount++;
	}
	
	public int getRetryCount() {
		return retryCount;
	}

	public void addRejectCount() {
		rejectCount ++;
	}
	
	public int getRejecCount() {
		return rejectCount;
	}
	
	public void addTimeoutCount() {
		timeoutCount++;
	}
	
	public int getTimeoutCount() {
		return timeoutCount;
	}
	
	public int getBrowserCount() {
		return browserCount;
	}
		
	/**
	 * @Title: destroy
	 * @Description: destroy the threadpool, browserpool and monitor thread.
	 * @throws
	 */
	public void destroy(){
		this.threadPool.shutdown();
		while (!this.threadPool.isShutdown()){
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				LOG.warn("encountered problem when shutdown threadpool");
			}
		}
		this.browserPool.destroy();
		poolMonitor.stop();
		LOG.info("MainThread destroyed Success.");
	}

}

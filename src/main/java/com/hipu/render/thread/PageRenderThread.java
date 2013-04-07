package com.hipu.render.thread;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import org.apache.log4j.Logger;
import org.openqa.selenium.remote.SessionNotFoundException;
import org.openqa.selenium.remote.UnreachableBrowserException;

import com.hipu.render.entity.Browser;
import com.hipu.render.entity.BrowserPool;
import com.hipu.render.entity.MainRender;

/**
 * @ClassName: RenderThread
 * @Description: every RenderThread process one task
 * @author shelton
 * @Date:2013-3-20
 *
 */
public class PageRenderThread implements Callable<String>{
	
	private static final Logger LOG = Logger.getLogger(PageRenderThread.class);
	
	/**
	 * @Fields:browser:the browser entity used to get page source
	 */
	private Browser browser;
	
	/**
	 * @Fields:url: url address
	 */
	private String url;
	
	private Thread thread;
	
	/**
	 * @Fields:threadSatatus: 
	 * record the thread start time when started and remove the record after the thread finished task,
	 * this fields is shared by different threads
	 */
	private Map<Thread, Long> threadSatatus;

	public PageRenderThread(String url){
		
		this.threadSatatus = MainRender.getInstance().getThreadStatus();
		
		this.url = url;
	}

	/* (non-Javadoc)
	 * <p>Title: call</p>
	 * <p>Description: </p>
	 * @return
	 * @throws Exception
	 * @see java.util.concurrent.Callable#call()
	 */
	@Override
	public String call() throws Exception{
		
		this.browser = BrowserPool.getInstance().getBrowser();
		this.thread = Thread.currentThread();
		
		LOG.info(beginThreadInfo()+browser.hashCode());
		this.threadSatatus.put(thread, System.currentTimeMillis());					//record the start time
		//long start = System.currentTimeMillis();
		
		String source = null;
		try {
			source = browser.load(url);
		} catch (UnreachableBrowserException e) {
			//the browser can not be used 
			LOG.error("Browser can not be used."+url);
			BrowserPool.getInstance().replaceBrowser(browser);
			throw new ExecutionException(e.getCause());
		} catch (SessionNotFoundException e) {
			throw new ExecutionException(e.getCause());
		} catch (Exception e) {
			LOG.error(e.getClass().toString());
			throw new Exception();
		}
		finally {
			//long diff = System.currentTimeMillis() - start;
			//LOG.debug(thread.getName()+" used time "+diff);
			this.threadSatatus.remove(thread);
			LOG.info(endThreadInfo());
			BrowserPool.getInstance().releaseBrowser(browser);
		}
		return source;
	}
	
	/**
	 * @Title: beginThreadInfo
	 * @Description: print the thread start info
	 * @throws
	 */
	public String beginThreadInfo () {
		
		StringBuffer sb = new StringBuffer("Begin New Thread ");
		
		sb.append(thread.getName()).append(" Process ").append(url);
		
		return sb.toString();
	}
	
	/**
	 * @Title: endThreadInfo
	 * @Description: print the thread end info
	 * @return 设定文件
	 * @throws
	 */
	public String endThreadInfo () {
		
		StringBuffer sb = new StringBuffer("End Thread ");
		
		sb.append(thread.getName()).append(" Process ").append(url);
		
		return sb.toString();
	}
	
	
	public Thread getThread() {
		return this.thread;
	}
	
	public void interrupt() {
		this.thread.interrupt();
	}
	
}

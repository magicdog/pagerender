package com.hipu.render.entity;

import java.io.File;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jboss.netty.util.internal.ConcurrentHashMap;

import com.hipu.render.config.Config;

/**
 * @ClassName: BrowserPool
 * @Description: This pool can maintain specific browsers, it was completed using single instance and this class is threadsafe.
 * usage:
 * 		BrowserPool pool = BrowserPool.getInstance();
 * 		pool.initial();   // only initial one time
 * 		.....
 * 		pool.addBrowser(2);
 * @author shelton
 * @Date:2013-3-20
 *
 */
public class BrowserPool {
	
	private static final Logger LOG = Logger.getLogger(BrowserPool.class);
	
	/**
	 * @Fields: conf : contains the concrete settings about firefox
	 */
	private Config conf;
	
	/**
	 * @Fields:browserCount : the amount of browsers in  this pool
	 */
	private  volatile Integer browserCount = 1;
	
	
	/**
	 * @Fields:browsers 	: maintains browser object and the number of thread using or waiting it.
	 * <browser, 0> means this browser is free now and can allocate it to other thread when needed.
	 */
	private  Map<Browser, Integer> browsers = null;
	
	
	/**
	 * @Fields:replaceCount : the amount of browsers that have been destroyed because of exception
	 */
	private  volatile Integer replaceCount = 0;
	
	/**
	 * @Fields: cacheDir : the directory of cache
	 */
	private String cacheDir = "";
	
	
	/**
	 * @Fields: timeout	 : the maximun time load a page
	 */
	private Integer timeout;
	
	private  static BrowserPool browserPool = new BrowserPool();
	
	private BrowserPool() {
	}
	
	public static BrowserPool getInstance() {
		if (browserPool == null) {
			synchronized (browserPool) {
				if (browserPool == null)
					browserPool = new BrowserPool();
			}
		}
		return browserPool;
	}
	
	/**
	 * @Title: initial
	 * @Description: initial BrowserPool 
	 */
	public void initial() {
		conf = Config.getInstance();
		browserCount = conf.getInt(Config.RENDER_BROWSER_COUNT);
		browsers = new ConcurrentHashMap<Browser, Integer>();
		Browser browser = null;
		for (int i = 0; i < browserCount; i++) {
			String dir = cacheDir+File.separator+"fire_cache"+i;
			createDir(dir);
			browser = new Browser(dir);
			browsers.put(browser, 0);
		}
	}
	
	 /**
	 * @Title: createDir
	 * @Description: create the directory of cache
	 * @param destDirName 
	 * @return  true : successed to create the directory
	 * 			false: failed to create the directory
	 */
	public boolean createDir(String destDirName) {
	        if ( !destDirName.endsWith(File.separator) ) {
	            destDirName = destDirName + File.separator;
	        }
	        File dir = new File(destDirName);
	        //创建目录
	        if (dir.mkdirs()) {
	            return true;
	        } else {
	            return false;
	        }
	    }
	
	/**
	 * @Title: addBrowser
	 * @Description: add a new browser entity to BrowserPool
	 * @param count: the amount of new browsers
	 * @param cache: the directory of cache ,if you do not need cache ,set cache = null
	 */
	public synchronized void addBrowser(int count, String cache) {
		if ( count < 0 ) {
			LOG.info("failed to add browser because of the value of count is negative" + count);
			return;
		}
			
		Browser browser = null;
		
		for (int i = 0; i < count; i++) {
			browserCount++;
			if ( null == cache )
				browser = new Browser(cacheDir + "/fire_cache" + browserCount);
			else
				browser = new Browser(cache);
			browsers.put(browser, 0);
			LOG.debug("add ok");
		}
	}
	
	/**
	 * @Title: removeBrowser
	 * @Description: remove the browser entity
	 * @param browser
	 * @return true  success; false fail
	 */
	public synchronized Integer removeBrowser(Browser browser) {
		browser.destroy();
		Integer success = null;
		success = browsers.remove(browser);
		
		if ( success != null ) {
			browserCount--;
		}
		return success;
	}
	
	/**
	 * @Title: removeBrowser
	 * @Description: delete the browsers
	 * @param count : the amount of browsers
	 * @return the real amount of browsers that have been removed
	 */
	public synchronized int removeBrowser(int count) {
		int size = 0;
		if ( count <=0 || count > browserCount ) {
			return size;
		}
		
		Iterator<Map.Entry<Browser, Integer>> it = browsers.entrySet().iterator();
		while( size++ < count && it.hasNext() ) {
			Map.Entry<Browser, Integer> entry = it.next();
			removeBrowser(entry.getKey());
		}
		
		return size;
	}
	
	/**
	 * @Title: replaceBrowser
	 * @Description: destroy the browser and add a new one
	 * @param browser 
	 * @throws
	 */
	public synchronized void replaceBrowser(Browser browser) {
		Integer removeSuccess = removeBrowser(browser);
		
		if ( removeSuccess != null ) {
			addBrowser(1, browser.getCache());
			replaceCount++;
		}
		
		LOG.debug("replace browser" + removeSuccess);
	}

	/**
	 * @Title: getReplaceCount
	 * @Description: get the amount of browsers that have been replaced
	 * @return 
	 */
	public int getReplaceCount() {
		return replaceCount;
	}
	
	public int getBrowserCount() {
		return browserCount;
	}

	/**
	 * @Title: getBrowser
	 * @Description: return the browser which was shared by the least amount of threads
	 * this method will return the first browser which map value is 0, if not find, return the browser with the minimum map value.
	 * @return 
	 */
	public synchronized Browser getBrowser() {		
		Browser browser = null;
		int mini = -1;
		for (Browser bw : browsers.keySet()) {
			int shareCount = browsers.get(bw);
			if ( mini < 0 ) {							//set the mini equals the first browsers' value
				mini = shareCount;
				browser = bw;
			}
			if ( shareCount == 0 ) {					//if shareCount==0, means this browser is free, then set 1 to the browser
				browsers.put(bw, 1);
				return bw;
			} else if ( shareCount < mini ){			//record the minimum value
				mini = shareCount;
				browser = bw;
			}
		}
		browsers.put(browser, mini+1);
		return browser;	
	}
	
	public void showStatus() {
		LOG.info("total amount of browser : "+browserCount);
		for (Browser browser : browsers.keySet()){
			LOG.info(browser.getCache()+" : "+browsers.get(browser));
		}
	}
	
	/**
	 * @Title: releaseBrowser
	 * @Description: reduce the number of threads, thread should release the browser after using it.
	 * @param browser 
	 */
	public synchronized void releaseBrowser(Browser browser) {
		
		if (browsers.containsKey(browser)) {
			int count = browsers.get(browser);
			count = count <= 0 ? 0 : count - 1;
			browsers.put(browser, count);
		}
	}
	
	/**
	 * @Title: setTimeout
	 * @Description: update the timeout whenever modified the value of timeout
	 * @param timeout 
	 */
	public void setTimeout(int timeout) {
		Iterator<Map.Entry<Browser, Integer>> it = browsers.entrySet().iterator();
		while(it.hasNext()) {
			Map.Entry<Browser, Integer> entry = it.next();
			entry.getKey().setTimeout(timeout);
		}
	}
	
	public synchronized int getTimeout() {
		return timeout;
	}
	
	/**
	 * @Title: destroy
	 * @Description: destroy all the browser in this BrowserPool
	 * @throws
	 */
	public void destroy() {
		Iterator<Map.Entry<Browser, Integer>> it = browsers.entrySet().iterator();
		while(it.hasNext()) {
			Map.Entry<Browser, Integer> entry = it.next();
			entry.getKey().destroy();
			it.remove();
		}
	}
	

}

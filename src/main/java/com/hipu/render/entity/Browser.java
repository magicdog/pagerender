package com.hipu.render.entity;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.remote.SessionNotFoundException;

import com.hipu.render.config.Config;
import com.hipu.render.thread.PageRenderThread;

/**
 * @author weijian
 *         Date : 2013-03-08 14:27
 */
public class Browser {
	
	private static final Logger LOG = Logger.getLogger(PageRenderThread.class);

    /**
     * @Fields: profile	 : contains the concrete settings about firefox
     * @Fields: webDriver: using this object to load url
     */
	private FirefoxProfile profile;
	private WebDriver webDriver;
    
	/**
	 * @Fields: conf : get the global single instance of Config 
	 */
	private Config conf;
    
	/**
	 * @Fields: timeout 	: the max time when loading a page
	 * @Fields: cacheDir 	: the directory of cache
	 * @Fields: enableProxy : whether enable proxy
	 * @Fields: ip 			: the ip of proxy
	 * @Fields: port 		: the port of proxy
	 */
	private int timeout;
    private String cacheDir;
    private boolean enableProxy = false;
    private String ip;
    private int port;

	public void initial() {
		
		conf = Config.getInstance();
		
		enableProxy = conf.getBoolean(Config.RENDER_BROWSER_ENABLE_PROXY);
		ip = conf.getString(Config.RENDER_BROWSER_PROXY_IP);
		port = conf.getInt(Config.RENDER_BROWSER_PROXY_PORT);

		profile = new FirefoxProfile();
		profile.setPreference( "permissions.default.image", 					2 ); 	// forbidding loading image
		profile.setPreference( "network.http.pipelining", 						true ); // open multithread load
		profile.setPreference( "network.http.proxy.pipelining", 				true ); // open proxy multithread load
		profile.setPreference( "network.http.pipelining.maxrequests",			12 ); 	// the number of thread loading one page

		profile.setPreference( "plugins.click_to_play", 						true ); // forbidding loading plugins, including flash
		profile.setPreference( "media.autoplay.enabled", 						false );// disable autoplay
		profile.setPreference( "plugin.default_plugin_disabled", 				false );// never providing hint when failed to load plugin
		profile.setPreference( "network.http.max-persistent-connections-per-proxy",12 );

		profile.setPreference( "privacy.popups.disable_from_plugins", 			3 );
		profile.setPreference( "extensions.enabledAddons", 						"" );

		profile.setPreference( "content.notify.interval", 						750000 );
		profile.setPreference( "content.notify.ontimer", 						true );

		profile.setPreference( "content.switch.threshold", 						250000 );
		profile.setPreference( "browser.cache.memory.capacity", 				65536 );
		profile.setPreference( "browser.cache.memory.enable", 					true );

		profile.setPreference( "browser.cache.disk.enable", 					true );

		profile.setPreference( "nglayout.initialpaint.delay", 					0 );
		profile.setPreference( "ui.submenuDelay", 								0 );

		profile.setPreference( "network.http.max-connections", 					256 );
		profile.setPreference( "network.dns.disableIPv6", 						true );
		profile.setPreference( "network.http.requests.max-start-delay", 		0 );
		profile.setPreference( "content.interrupt.parsing", 					true );
		profile.setPreference( "content.max.tokenizing.time", 					2250000 );

		profile.setPreference( "content.notify.backoffcount",   				5 );
		profile.setPreference( "plugin.expose_full_path", 						true );
		profile.setPreference( "ui.submenuDelay", 								0 );
		profile.setPreference( "network.http.keep-alive", 						true );
		profile.setPreference( "network.http.version", 							"1.1" );
		profile.setPreference( "dom.popup_maximum", 							0 );
	}

	public Browser(String cache) {
		initial();
		
		cacheDir = cache;
    	timeout = conf.getInt(Config.RENDER_BROWSER_TIMEOUT);
    	profile.setPreference( "browser.cache.disk.parent_directory", 			cache );
    	
    	if (enableProxy) {
    		profile.setPreference("network.proxy.http", ip);
            profile.setPreference("network.proxy.http_port", port);
            profile.setPreference("network.proxy.type", 1);
    	}
        
        webDriver = new FirefoxDriver(profile);
        webDriver.manage().timeouts().setScriptTimeout(this.timeout, TimeUnit.SECONDS);
        webDriver.manage().timeouts().implicitlyWait(this.timeout, TimeUnit.SECONDS);
        webDriver.manage().timeouts().pageLoadTimeout(this.timeout, TimeUnit.SECONDS);
        
    }
	
	public void setTimeout(int timeout) {
		this.timeout = timeout;
		webDriver.manage().timeouts().setScriptTimeout(this.timeout, TimeUnit.SECONDS);
        webDriver.manage().timeouts().implicitlyWait(this.timeout, TimeUnit.SECONDS);
        webDriver.manage().timeouts().pageLoadTimeout(this.timeout, TimeUnit.SECONDS);
	}
	
    /**
     * @Title: load
     * @Description: return the source of page
     * @param url : like http://www.hipu.com
     * @return String : the page source, if failed return ""
     */
    public String load(String url) throws Exception{
    	if ( url == null )
    		return "";
        try{
        	
        	webDriver.get(url);
        	
        } catch (SessionNotFoundException e) {
        	LOG.error(e.getClass().toString());
        	return "";
        } catch (Exception e) {
        	LOG.warn("load not complete "+url);
        	return webDriver.getTitle();
        }
        return webDriver.getTitle();
//        return webDriver.getPageSource();
    }

    /**
     * @Title: getCache
     * @Description: get the directory of cache
     * @return String : cache directory
     */
    public String getCache() {
    	return cacheDir;
    }
    
    
    public void destroy(){
        //((FirefoxDriver)webDriver).kill();
    	try {
    		webDriver.quit();
    	}  catch (Exception e) {
    		LOG.warn("web driver quit has a little problem."+e.getClass().toString());
    	}
    }

    public static void main(String[] args) {
        Browser browser = new Browser("D:\\cache");
//
//        String url = "http://hb.qq.com/a/20130222/000482.htm";
//        String url = "http://ent.ifeng.com/tv/special/haoshengyin/kuaixun2/detail_2012_09/30/18021061_0.shtml ";
        try {
        	Date start = new Date();
//			System.out.println(browser.load(url));
//			System.out.println(browser.load("http://sports.xinmin.cn/2012/09/30/16564327.html"));
			
        	System.out.println(browser.load("http://district.ce.cn/newarea/roll/201302/22/t20130222_24134162.shtml"));
			System.out.println(System.currentTimeMillis() - start.getTime());
			
			System.out.println(browser.load("http://www.huffingtonpost.com/2012/09/30/eagles-beat-giants-19-17-tynes-missed-field-goal_n_1927862.html "));
			Date end = new Date();
			System.out.println(end.getTime() - start.getTime());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//        
//        browser.destroy();
////        System.out.println(browser.load(url));
////        System.out.println(browser.load(url));
////        System.out.println(browser.load(url));
////        System.out.println(browser.load(url));
//
    }
}

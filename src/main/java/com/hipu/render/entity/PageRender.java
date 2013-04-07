package com.hipu.render.entity;

import java.net.URL;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;


/**
 * @ClassName: PageRender
 * @Description:submit client requests to MainThread and blocked until get the page source
 * usage:	PageRender render = new PageRender();
 * 			render.load(url); // this method will blocked until finished loading
 * @author shelton
 * @Date:2013-3-20
 *
 */
public class PageRender {
	
	private static final Logger LOG = Logger.getLogger(PageRender.class);
	
	private MainRender mainthread;
	
	public PageRender() {
		this.mainthread = MainRender.getInstance();
	}
	
	/**
	 * @Title: load
	 * @Description: get the page source.
	 * @param url : like http://www.hipu.com
	 * @return the source of page when load success, otherwise return null.
	 * @throws
	 */
	public String load(String url) {
		String source = null;
		try {
			
			Future<String> future = mainthread.submit(url);
			source = (String) future.get();
			
		} catch (NullPointerException e) {
			//can not get the future
			LOG.error(e.getClass().toString()+" is null.");
			return source;
		} catch (InterruptedException e) {
			LOG.error("InterruptedException");
			return source;
		} catch (ExecutionException e) {
			//can not using browser, and reload again
			LOG.error("Error communicating with the remote browser,load the url again."+url);
			mainthread.addRetryCount();
			return load(url);
		}  catch (Exception e){
			LOG.error("Exception"+e.getClass().toString());
			return source;
		} 
		return source;
	}
	
	public static void main(String[] args) {
		 URL url = PageRender.class.getResource("/log4j.properties");
	     if ( url == null ) {
	         url =  PageRender.class.getResource("/conf/log4j.properties");
	     }
	     PropertyConfigurator.configure(url);
		
		MainRender mt = MainRender.getInstance();
		mt.Initial();
		PageRender mainrender = new PageRender();
		mt.start();
		
		//System.out.println(mainrender.load("http://www.sina.com.cn/"));
		//System.out.println(mainrender.load("http://www.baidu.com.cn/"));
		//System.out.println(mainrender.load("http://down.51cto.com/data/318281"));
		//System.out.println(mainrender.load("http://developer.51cto.com/art/200906/132360.htm"));
	
		//mt.destroy();
		
	}
}

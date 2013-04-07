package com.hipu.render.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.Vector;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.hipu.render.entity.MainRender;
import com.hipu.render.service.RenderService;

public class Main {
	
	private static final Logger LOG = Logger.getLogger(MainRender.class);
	
	private Vector<String> urls;
	
	private WriteThread wt;
	
	private int readcount;
	
	private String common;
	
	public int getQueueSize() {
		return urls.size();
	}
	
	public  Main(int readcount, String common) {
		this.common = common;
		this.readcount = readcount;
		urls = new Vector<String>();
		URL properties = Main.class.getResource("/resource/url/sohu");
		//System.out.println(properties);
		//return;

		File file = new File(properties.getPath());
		BufferedReader reader = null;
		try {
	            reader = new BufferedReader(new FileReader(file));
	            String tempString = null;
	            while ((tempString = reader.readLine()) != null) {
	                urls.add(tempString);
	            }
	            reader.close();
	        } catch (Exception e) {
	            e.printStackTrace();
	        } finally {
	            if (reader != null) {
	                try {
	                    reader.close();
	                } catch (IOException e1) {
	                }
	            }
	        }
	}
	
	
	public void start() {
		System.out.println(urls.size());
		
		Date start = null;
		Date end = null;
		HttpRequestThread hqt ;
		int size = 5;
		Thread thread[] = new Thread[size];
		HttpRequest hq = new HttpRequest();
		Calculate.start();
		for (int i=0; i<urls.size(); i+=size) {
			for (int j=0;j<size && (i+j)<urls.size();j++) {
				hqt = new HttpRequestThread(urls.get(i+j), common);
				thread[j] = new Thread(hqt);
				thread[j].start();
			}
			if (i>100)
				return;
//			for (int k=0; k<size;k++) {
//				try {
//					thread[k].join();
//				} catch (InterruptedException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}
			
			//return;
		}
		
	}
	
	public static void main(String[] args) {
		 URL url = Main.class.getResource("/log4j.properties");
	     if ( url == null ) {
	         url =  Main.class.getResource("/conf/log4j.properties");
	     }
	     PropertyConfigurator.configure(url);
		
		Main m = new Main(40,"http://192.168.68.13:8081/service/render");
	    //Main m = new Main(40,"http://localhost:8081/service/render");
		m.start();
	}
}

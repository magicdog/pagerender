package com.hipu.render.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;

public class WriteThread implements Runnable{
	
	private BlockingQueue<String> urls;
	
	private String filepath;
	
	private Thread thread;
	
	private boolean isrunning = true;
	
	public WriteThread(BlockingQueue<String> urls, String filepath) {
		this.urls = urls;
		this.filepath = filepath;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		File file = new File(filepath);
		 BufferedReader reader = null;
		 try {
	            reader = new BufferedReader(new FileReader(file));
	            String tempString = null;
//	            int line = 1;
	            // 一次读入一行，直到读入null为文件结束
	            while ((tempString = reader.readLine()) != null && isrunning) {
	                // 显示行号
	                urls.put(tempString);
//	            	System.out.println("line " + line + ": " + tempString);
//	                line++;
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
	
	public void stop() {
		this.isrunning = false;
	}

}

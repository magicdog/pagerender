package com.hipu.render.test;

import java.util.Date;

public class Calculate {
	
	private static Long requestCount = (long) 0;
	
	private static Long responseCount = (long) 0;
	
	private static long startTime;
	
	public static Long maxTime = (long) 0;
	
	public static Long minTime = (long) 200000;
	
	public static void start() {
		requestCount = (long) 0;
		responseCount = (long) 0;
		startTime = System.currentTimeMillis();
	}
	
	public static void addRequest() {
		synchronized (requestCount) {
			requestCount++;
		}
	}
	
	public static void setMax(Long max){
		synchronized (maxTime) {
			maxTime = max;
		}
	}
	
	public static void setMin(Long min){
		synchronized (minTime) {
			minTime = min;
		}
	}
	
	public static long getMax() {
		synchronized (maxTime) {
			return maxTime;
		}
	}
	
	
	public static long getMin() {
		synchronized (minTime) {
			return minTime;
		}
	}
	
	public static void showSpeed() {
		long diff = System.currentTimeMillis() - startTime;
		//System.out.println("request"+(1.0*requestCount)/diff*1000);
		System.out.println("aerage response "+(1.0*responseCount)/diff*1000);
		System.out.println("max response time "+getMax());
		System.out.println("min response time "+getMin());
	}
	
	public static void addResponse() {
		synchronized (responseCount) {
			responseCount++;
		}
	}
}

package com.hipu.render.test;

import java.util.concurrent.BlockingQueue;

import org.apache.log4j.Logger;

import com.hipu.render.entity.MainRender;
import com.hipu.render.entity.PageRender;
import com.hipu.render.service.RenderService;

public class HttpRequestThread implements Runnable  {
	
	private static final Logger LOG = Logger.getLogger(RenderService.class);
	
	private  String url;
	
	private boolean isrunning = true;
	
	private HttpRequest hq;
	
	private String common;
	
	public HttpRequestThread(String url, String common) {
		this.url = url;
		this.common = common;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		hq = new HttpRequest();
		//while (isrunning) {
			long start = System.currentTimeMillis();
			Calculate.addRequest();
			String result = hq.sendGetRequest(common, url);
			Calculate.addResponse();
			long end = System.currentTimeMillis();
			long diff = end -start;
			System.out.println(start+"  "+end+" "+diff);
			if (diff > Calculate.getMax())
				Calculate.setMax(diff);
			if (diff < Calculate.getMin())
				Calculate.setMin(diff);
			LOG.info(result);
			Calculate.showSpeed();
		//}
	}
	
	public void stop() {
		this.isrunning = false;
	}
}

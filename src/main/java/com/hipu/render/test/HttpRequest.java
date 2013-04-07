package com.hipu.render.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import org.apache.log4j.Logger;

import com.hipu.render.service.RenderService;

public class HttpRequest {
	private static final Logger LOG = Logger.getLogger(RenderService.class);
	
	//comon:     http.../service/render
	//parameter: http://www.baidu.com
	public String sendGetRequest(String common, String parameter) {
		String result = null;
		String urlStr = common;
		if (parameter != null && parameter.length () > 0)
		{
			urlStr += "?url=" + parameter;
		}
		
		try {
			URL url = new URL(urlStr);
			URLConnection conn = url.openConnection ();
			BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			StringBuffer sb = new StringBuffer();
			String line;
			while ((line = rd.readLine()) != null)
			{
				sb.append(line);
			}
			rd.close();
			result = sb.toString();
		} catch (MalformedURLException e) {
			LOG.error("",e);
			return null;
		} catch (IOException e) {
			LOG.error("",e);	
			return null;
		}
		return result;
	}
	
	public static void main(String[] args) {
		HttpRequest hq = new HttpRequest();
		String title = hq.sendGetRequest("http://localhost:8081/service/render", "http://www.baidu.com");
		LOG.info(title);
	}
}

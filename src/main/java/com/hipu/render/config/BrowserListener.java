package com.hipu.render.config;

import org.apache.commons.configuration.event.ConfigurationEvent;
import org.apache.commons.configuration.event.ConfigurationListener;
import org.apache.log4j.Logger;

import com.hipu.render.entity.MainRender;

public class BrowserListener implements ConfigurationListener {
	
	private Config conf = Config.getInstance();

	private static final int EVENT_SET_PROPERTY = 3;
	
	private static final Logger LOG = Logger.getLogger(BrowserListener.class);
	
	private MainRender mainRender = MainRender.getInstance();
	
	private static final String RENDER_BROWSER_COUNT = "render.browser.count";
	
	private static final String RENDER_BROWSER_TIMEOUT = "render.browser.timeout";

	@Override
	public void configurationChanged(ConfigurationEvent event) {
		// TODO Auto-generated method stub
		if (event.getType() == EVENT_SET_PROPERTY) {
			if ( event.getPropertyName().trim().equalsIgnoreCase(RENDER_BROWSER_COUNT)) {
				updateBrowsers(conf.getInt(Config.RENDER_BROWSER_COUNT), (Integer) event.getPropertyValue());
			}
			
			if ( event.getPropertyName().trim().equalsIgnoreCase(RENDER_BROWSER_TIMEOUT)) {
				mainRender.updateTimeout((Integer) event.getPropertyValue());
			}
		}
	}
	
	public void updateBrowsers(int oldCount, int newCount) {
		if ( oldCount == newCount || newCount < 0)
			return;
		if ( oldCount > newCount ) {
			mainRender.releaseBrowser(oldCount - newCount);
		} else {
			mainRender.addBrowser(newCount - oldCount);
			
		}
	}
	
	
	
	
}

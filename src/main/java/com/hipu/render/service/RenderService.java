package com.hipu.render.service;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.hipu.render.config.Config;
import com.hipu.render.entity.MainRender;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.ResourceHandler;
import org.mortbay.jetty.nio.SelectChannelConnector;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;

import java.net.URL;

/**
 * @author weijian
 * Date : 2012-11-26 15:48
 */
public class RenderService implements Runnable{
    private static final Logger LOG = Logger.getLogger(RenderService.class);

    @Parameter(description="Help", names={"--help","-help"})
    private boolean help = false;
    
    @Parameter(description="Service Host", names="-h")
    private String host  = "localhost";

    @Parameter(description="Port Number", names="-p")
    private int port = 8081;

    /**
     * @Fields:mainThread:main process
     */
    private MainRender mainThread;
    
    private Config conf;

    public RenderService(Configuration config){
    	conf = Config.getInstance();
    	conf.initConfig(config);
    }

    @Override
    public void run() {
    	mainThread = MainRender.getInstance();
   		mainThread.Initial();
        mainThread.start();
        
    	Server server = new Server();
    	Connector conn = new SelectChannelConnector();
        conn.setHost(host);
        conn.setPort(port);
        server.setConnectors(new Connector[] { conn });
        
        Context root = new Context(server, "/service", Context.SESSIONS);
        
        root.addServlet(new ServletHolder(new ConfigServlet()), "/config");
        root.addServlet(new ServletHolder(new RenderServlet()), "/render");
        
        Context control = new Context(server, "/control", Context.SESSIONS);
        control.setHandler(new ControlHandler(this));

        Context pages = new Context(server, "/", Context.SESSIONS);
        ResourceHandler handler = new ResourceHandler();
        System.out.println(org.mortbay.resource.JarResource.newClassPathResource("/resource").getURL());
        handler.setBaseResource(org.mortbay.resource.JarResource.newClassPathResource("/resource"));
        handler.setCacheControl("max-age=5");
        pages.setHandler(handler);
        
        try {
            server.start();
            server.join();
        } catch (Exception e) {
            LOG.fatal("", e);
        }

    }
    
    public void stop() {
    	mainThread.destroy();
    }

    /**
     * @param args
     */
    public static void main(String[] args) throws ConfigurationException {

        URL url = RenderService.class.getResource("/log4j.properties");
        if ( url == null ) {
            url =  RenderService.class.getResource("/conf/log4j.properties");
        }
        PropertyConfigurator.configure(url);
        
        URL properties = RenderService.class.getResource("/render.properties");
        if ( properties == null ) {
        	properties =  RenderService.class.getResource("/conf/render.properties");
        }

        Configuration config = new PropertiesConfiguration(properties);

        RenderService service = new RenderService(config);
        JCommander commander = new JCommander(service);
        try {
            commander.parse(args);
        } catch (ParameterException e) {
            LOG.error(e.getMessage());
            commander.usage();
        }
        if ( service.help ){
            commander.usage();
        }else{
            service.run();
        }
    }
}
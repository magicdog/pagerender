package com.hipu.render.service;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import com.hipu.render.config.Config;

import org.apache.log4j.Logger;

import sun.awt.SunHints.Value;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;


public class ConfigServlet extends HttpServlet {
	private static final Logger LOG = Logger
			.getLogger(ConfigServlet.class);

    private Config config;

    public ConfigServlet() {
        super();
        config = Config.getInstance();
    }

    @Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

        String action = req.getParameter("action");
        resp.setCharacterEncoding("utf-8");
        resp.setContentType("application/json");

        if ( "query".equalsIgnoreCase(action) ){
            resp.getWriter().print(JSON.toJSONString(config.getArgsInfo()));
        } else if ( "update".equalsIgnoreCase(action) ){
            Object err = -1;
            String val = req.getParameter("value");
            String name = req.getParameter("name").trim();
            if ( val != null && !"".equals(val) ) {
                try{
                    config.setProperty(name, val);
                    LOG.info("Update config: " + name + " to " + val);
                } catch (Exception e){
                    LOG.error(e);
                    err = e.toString();
                }
            } else {
                err = "Could not update null value!";
            }

            Map map = Maps.newHashMap();
            map.put("error", err);
            resp.getWriter().print(JSON.toJSONString(map));
            
        } else {
            resp.getWriter().print("{\"error\":\"wrong action!\"}");
        }
        resp.getWriter().close();
	}

}

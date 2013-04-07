package com.hipu.render.service;

import org.apache.log4j.Logger;
import org.mortbay.util.ajax.JSON;
import org.w3c.dom.Document;

import com.hipu.render.entity.PageRender;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class RenderServlet extends HttpServlet {
	private static final Logger LOG = Logger.getLogger(RenderServlet.class);

	private PageRender mainrender;

    public RenderServlet() {
        super();
        this.mainrender = new PageRender();
    }

    @Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
        String url = req.getParameter("url").trim();
        String res = ""; 
        res = mainrender.load(url);
        resp.setHeader("Cache-control", "max-age=10");
        resp.setCharacterEncoding("utf-8");
		resp.getWriter().print(res);
		resp.getWriter().close();
	}
}

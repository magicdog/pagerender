package com.hipu.render.service;

import org.mortbay.jetty.handler.AbstractHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author weijian
 *         Date : 2013-01-08 19:06
 */
public class ControlHandler extends AbstractHandler {

    private RenderService service;
    public ControlHandler(RenderService service) {
        super();
        this.service = service;
    }

    @Override
    public void handle(String target, HttpServletRequest request, HttpServletResponse response, int dispatch) throws IOException, ServletException {
        if ( target.equals("/stop") ){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    service.stop();
                }
            }).start();
        } else if  ( target.equals("/reload_domain") ){
//            Domains.getInstance().reloadDomains();
        }
    }
}

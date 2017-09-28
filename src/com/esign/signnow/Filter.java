package com.esign.signnow;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class Filter implements javax.servlet.Filter {
	
	private FilterConfig filterConfig = null;
	   public void init(FilterConfig filterConfig) 
	      throws ServletException {
	      this.filterConfig = filterConfig;
	   }

	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) {
		HttpServletResponse response = (HttpServletResponse) res;
		HttpServletRequest request = (HttpServletRequest) req;
		System.out.println("Inside Filter - " + request.getMethod());

		String origin = request.getHeader("Origin");

		response.setHeader("Access-Control-Allow-Origin", origin);

		response.setHeader("Access-Control-Allow-Methods", "POST, PUT, GET, OPTIONS");
		response.setHeader("Access-Control-Allow-Headers", "X-ACCESS_TOKEN, x-requested-with, Access-Control-Allow-Origin, Authorization, Origin, Content-Type, response-type");
		response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
		response.setHeader("Access-Control-Max-Age", "3600");

		if (!request.getMethod().equalsIgnoreCase("OPTIONS")) {
			System.out.println("Continuing with the Filter Chain");
			try {
				chain.doFilter(req, res);
			} catch (IOException | ServletException e) {
				e.printStackTrace();
			}
		} else {
		}
	}

  

  public void destroy() {}

}
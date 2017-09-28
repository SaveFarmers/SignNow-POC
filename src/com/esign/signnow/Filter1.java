package com.esign.signnow;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet Filter implementation class Filter1
 */
@WebFilter("/SignNow/eSign1")
public class Filter1 implements Filter {

    /**
     * Default constructor. 
     */
    public Filter1() {
        // TODO Auto-generated constructor stub
    }

	private FilterConfig filterConfig = null;
	   public void init(FilterConfig filterConfig) 
	      throws ServletException {
	      this.filterConfig = filterConfig;
	   }
	   
	/**
	 * @see Filter#destroy()
	 */
	public void destroy() {
		// TODO Auto-generated method stub
	}

	/**
	 * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
	 */
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
		HttpServletResponse response = (HttpServletResponse) res;
		HttpServletRequest request = (HttpServletRequest) req;
		System.out.println("Continuing with the Filter Chain");

		String origin = request.getHeader("Origin");

		response.setHeader("Access-Control-Allow-Origin", origin);

		response.setHeader("Access-Control-Allow-Methods", "POST, PUT, GET, OPTIONS");
		response.setHeader("Access-Control-Allow-Headers", "X-ACCESS_TOKEN, x-requested-with, Access-Control-Allow-Origin, Authorization, Origin, Content-Type");
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

		// pass the request along the filter chain
//		chain.doFilter(request, response);
	}


}

package com.swisscom.heroes.filter;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class RequestContext {

	private final boolean initialised=false;
	private String usuario="";
	private boolean fail=false;
	private final HashMap<String,String> headers=new HashMap<>();


	public String getUsuario() {
		return usuario;
	}

	public boolean isFail() {
		return fail;
	}

	public Map<String,String> getHeaders() {
		return headers;
	}

	public void init(final HttpServletRequest request, final HttpServletResponse response) {
		if (initialised) {
			return;
		}

		final String valorUsuario= request.getHeader(Header.USUARIO.getHeaderName());
		if (valorUsuario!= null) {
			this.usuario= valorUsuario;
		}
		final String valorFail= request.getHeader(Header.FAIL.getHeaderName());
		if (valorFail!= null) {
			this.fail= true;
		}

		final Enumeration<String> headerNames = request.getHeaderNames();

		while (headerNames.hasMoreElements()) {
			final String headerName = headerNames.nextElement();
			headers.put(headerName , request.getHeader(headerName));
		}

	}

}


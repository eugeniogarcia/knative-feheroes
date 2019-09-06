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
			if(Header.isPropagated(headerName)) {
				//Propate x-b3-parentspanid only when it has a value
				if(headerName.compareTo(Header.x_b3_parentspanid.getHeaderName())!=0 ||
						!request.getHeader(headerName).isEmpty()) {
					headers.put(headerName , request.getHeader(headerName));
				}
			}
		}
	}
}


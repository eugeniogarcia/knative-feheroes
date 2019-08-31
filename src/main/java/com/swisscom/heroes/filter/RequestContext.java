package com.swisscom.heroes.filter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class RequestContext {

	private final boolean initialised=false;
	private String cookie="";
	private String csrf="";
	private String usuario="";
	private boolean fail=false;

	public String getCookie() {
		return cookie;
	}

	public String getCSRF() {
		return csrf;
	}

	public String getUsuario() {
		return usuario;
	}

	public boolean isFail() {
		return fail;
	}

	public void init(final HttpServletRequest request, final HttpServletResponse response) {
		if (initialised) {
			return;
		}
		final String valorCookie = request.getHeader(Header.COOKIE.getHeaderName());
		if (valorCookie != null) {
			this.cookie= valorCookie ;
		}
		final String valorCabecera = request.getHeader(Header.CSRF.getHeaderName());
		if (valorCabecera != null) {
			this.csrf= valorCabecera ;
		}
		final String valorUsuario= request.getHeader(Header.USUARIO.getHeaderName());
		if (valorUsuario!= null) {
			this.usuario= valorUsuario;
		}
		final String valorFail= request.getHeader(Header.FAIL.getHeaderName());
		if (valorFail!= null) {
			this.fail= true;
		}
	}

}


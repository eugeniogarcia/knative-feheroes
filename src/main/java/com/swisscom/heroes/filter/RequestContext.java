package com.swisscom.heroes.filter;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;

import brave.SpanCustomizer;


@JsonFilter("MiContexto")
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

	public void updateSpan(SpanCustomizer span) {
		for (final Map.Entry<String, Object> entry : toMap(this).entrySet()) {
			if (entry.getValue() != null) {
				span.tag(entry.getKey(), entry.getValue().toString());
			}
		}
	}

	static Map<String, Object> toMap(Object obj) {
		return objectMapper.convertValue(obj, Map.class);
	}

	private static ObjectMapper objectMapper = new ObjectMapper();

	static {
		final SimpleBeanPropertyFilter theFilter = SimpleBeanPropertyFilter
				.serializeAllExcept("fail","usuario");
		final FilterProvider filters = new SimpleFilterProvider()
				.addFilter("MiContexto", theFilter);
		objectMapper.setFilterProvider(filters);
		objectMapper.registerModule(new Jdk8Module());
	}
}


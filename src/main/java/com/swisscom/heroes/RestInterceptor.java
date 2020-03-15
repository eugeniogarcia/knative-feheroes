package com.swisscom.heroes;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import com.swisscom.heroes.filter.Header;
import com.swisscom.heroes.filter.RequestContext;

public class RestInterceptor implements ClientHttpRequestInterceptor {
	private static final Logger LOGGER= LoggerFactory.getLogger(RestInterceptor.class);

	private final RequestContext context;

	public RestInterceptor(final RequestContext context) {
		super();
		this.context = context;
	}

	@Override
	public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
			throws IOException {
		final HttpHeaders headers = request.getHeaders();

		LOGGER.info("Calling a backend");

		if(!context.getUsuario().isEmpty()) {
			LOGGER.info("Forwarding the header value {}",context.getUsuario());
			headers.add(Header.USUARIO.getHeaderName(), context.getUsuario());
		}
		if(context.isFail()) {
			LOGGER.info("Forwarding a failure");
			headers.add(Header.FAIL.getHeaderName(), "");
		}

		//Print Headers
		headers.entrySet().stream().forEach(x -> LOGGER.info("{} : {}",x.getKey(),x.getValue().get(0)));

		return execution.execute(request, body);
	}
}
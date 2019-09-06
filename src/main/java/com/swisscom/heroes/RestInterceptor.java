package com.swisscom.heroes;

import java.io.IOException;

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

		if(!context.getUsuario().isEmpty()) {
			LOGGER.info("Forwarding the header value {0}",context.getUsuario());
			headers.add(Header.USUARIO.getHeaderName(), context.getUsuario());
		}
		if(context.isFail()) {
			LOGGER.info("Forwarding a failure");
			headers.add(Header.FAIL.getHeaderName(), "");
		}

		/*
		//Copy all headers for Zipkin
		final Set<Entry<String, String>> set = context.getHeaders().entrySet();
		final Iterator<Entry<String, String>> iterator = set.iterator();
		while(iterator.hasNext()) {
			final Entry<String, String> mentry = iterator.next();
			headers.add(mentry.getKey(), mentry.getValue());
		}
		 */
		return execution.execute(request, body);
	}
}
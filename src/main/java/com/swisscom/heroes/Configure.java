package com.swisscom.heroes;

import java.util.Collections;
import java.util.List;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.annotation.RequestScope;

import com.swisscom.heroes.filter.HttpFiltro;
import com.swisscom.heroes.filter.RequestContext;


@Configuration
public class Configure {

	private static final int ORDER = 2;

	@Bean
	@RequestScope
	public RequestContext requestContext() {
		return new RequestContext();
	}

	@Bean
	public FilterRegistrationBean<HttpFiltro> trackingFilterRegistrar(final RequestContext oceRequestContext) {
		final FilterRegistrationBean<HttpFiltro> registration = new FilterRegistrationBean<>();
		registration.setFilter(new HttpFiltro(oceRequestContext));
		registration.setOrder(ORDER);
		return registration;
	}

	@Bean
	public RestTemplate service(RequestContext context) {
		final RestTemplate template = new RestTemplate();

		final List<ClientHttpRequestInterceptor> interceptors = template.getInterceptors();
		if (interceptors==null){
			template.setInterceptors(Collections.singletonList(new RestInterceptor(context)));
		}
		else{
			interceptors.add(new RestInterceptor(context));
			template.setInterceptors(interceptors);
		}

		return template;
	}

}

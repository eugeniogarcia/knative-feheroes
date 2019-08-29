package com.swisscom.heroes;

import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.annotation.RequestScope;

import com.swisscom.heroes.filter.RequestContext;

@Configuration
public class Configure {

	@Autowired
	RequestContext context;

	@Bean
	@RequestScope
	public RequestContext requestContext() {
		return new RequestContext();
	}

	@Bean
	public RestTemplate service() {
		final RestTemplate template = new RestTemplate();

		final List interceptors = template.getInterceptors();
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

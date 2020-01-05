# Cabeceras de Zipkin

Para que Zipkin funcione hay una serie de cabeceras que deben propagarse. Las cabeceras las tenemos declaradas en un `enum` la clase `Header`:

```java
package com.swisscom.heroes.filter;

public enum Header {
	x_request_id("X-Request-Id"),
	x_b3_traceid("X-B3-TraceId"),
	x_b3_spanid("X-B3-SpanId"),
	x_b3_parentspanid("X-B3-ParentSpanId"),
	x_b3_sampled("X-B3-Sampled"),
	x_b3_flags("X-B3-Flags"),
	x_ot_span_context("X-Ot-Span-Context")
	;

	private final String headerName;

	Header(final String headerName) {
		this.headerName = headerName;
	}

	public static boolean isPropagated(String value) {
		for (final Header c : Header.values()) {
			if (c.headerName.toUpperCase().compareTo(value.toUpperCase())==0) {
				return true;
			}
		}
		return false;
	}

	public String getHeaderName() {
		return headerName;
	}
}
```

Todas las cabeceras se propagan con el valor que tienen salvo __X-B3-ParentSpanId__ que solo se propaga cuando tiene un valor informado.

# Procedimiento

En este MiSe seguimos dos procedimientos:

- Pasar las cabeceras manualmente
- Pasar las cabeceras usando la dependencia de sleuth

En ambas implementaciones se utiliza un filtro para guardar el valor de las cabeceras que el MiSe ha recibido cuando es llamado. El valor se guarda en una bean con scope Request. El bean de scope request:

```java
@Bean
@RequestScope
public RequestContext requestContext() {
	return new RequestContext();
}
```

La clase de tipo __RequestContext__:

```java
@JsonFilter("MiContexto")
public class RequestContext {
	private static final Logger LOGGER= LoggerFactory.getLogger(RequestContext.class);
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
			LOGGER.info("{} : {}",headerName,request.getHeader(headerName));
			if(Header.isPropagated(headerName)) {
				//Propate x-b3-parentspanid only when it has a value
				if(headerName.toUpperCase().compareTo(Header.x_b3_parentspanid.getHeaderName().toUpperCase())!=0 ||
						!request.getHeader(headerName).isEmpty()) {
					headers.put(headerName , request.getHeader(headerName));
				}
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
```

Observese como en el metodo `init(final HttpServletRequest request, final HttpServletResponse response)` se guarda una copia de las cabeceras:

```java
final Enumeration<String> headerNames = request.getHeaderNames();

while (headerNames.hasMoreElements()) {
	final String headerName = headerNames.nextElement();
	LOGGER.info("{} : {}",headerName,request.getHeader(headerName));
	if(Header.isPropagated(headerName)) {
		//Propate x-b3-parentspanid only when it has a value
		if(headerName.toUpperCase().compareTo(Header.x_b3_parentspanid.getHeaderName().toUpperCase())!=0 ||
				!request.getHeader(headerName).isEmpty()) {
			headers.put(headerName , request.getHeader(headerName));
		}
	}
}
```

Finalmente el filtro:

```java
@Bean
public FilterRegistrationBean<HttpFiltro> trackingFilterRegistrar(final RequestContext oceRequestContext) {
	final FilterRegistrationBean<HttpFiltro> registration = new FilterRegistrationBean<>();
	registration.setFilter(new HttpFiltro(oceRequestContext));
	registration.setOrder(ORDER);
	return registration;
}
```

y

```java
public class HttpFiltro extends OncePerRequestFilter {

	private static final Logger LOGGER = LoggerFactory.getLogger(HttpFiltro.class);
	private final RequestContext requestContext;

	public HttpFiltro(final RequestContext requestContext) {
		this.requestContext = requestContext;
	}

	@Override
	protected void doFilterInternal(@NonNull final HttpServletRequest httpServletRequest,
			@NonNull final HttpServletResponse httpServletResponse,
			@NonNull final FilterChain filterChain) throws ServletException, IOException {


		LOGGER.trace("Filter Start");
		requestContext.init(httpServletRequest, httpServletResponse);
		LOGGER.trace("Filter End");

		// Continue Processing
		filterChain.doFilter(httpServletRequest, httpServletResponse);
	}
}
```

Finalmente indicar que hemos añadido un __RestInterceptor__ al __RestTemplate__:

```java
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
```


## Manualmente

En la branch `ManualPropagation` se sigue la tactica de propagar las cabeceras explicitamente en el ____RestInterceptor__:

```java
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

	//Copy all headers for Zipkin
	final Set<Entry<String, String>> set = context.getHeaders().entrySet();
	final Iterator<Entry<String, String>> iterator = set.iterator();
	while(iterator.hasNext()) {
		final Entry<String, String> mentry = iterator.next();
		headers.add(mentry.getKey(), mentry.getValue());
	}

	//Print Headers
	headers.entrySet().stream().forEach(x -> LOGGER.info("{} : {}",x.getKey(),x.getValue().get(0)));

	return execution.execute(request, body);
}
```

## Sleuth

En la branch `SleuthWithBrave` se sigue la tactica de no propagar explicatamente las cabeceras en el ____RestInterceptor__, y dejar que sea la dependencia de Sleuth quien lo haga. Hemos añadido una dependencia:

```xml
<dependency> 
	<groupId>org.springframework.cloud</groupId>
	<artifactId>spring-cloud-starter-sleuth</artifactId>
</dependency>
```

Y notese como en este caso el interceptor no propaga las cabeceras:

```java
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
```
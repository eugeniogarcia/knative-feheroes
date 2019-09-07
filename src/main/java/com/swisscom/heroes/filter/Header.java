package com.swisscom.heroes.filter;

public enum Header {

	USUARIO("x-usuario"),
	FAIL("x-fail"),
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

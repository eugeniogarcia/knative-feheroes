package com.swisscom.heroes.filter;

public enum Header {

	USUARIO("x-usuario"),
	FAIL("x-fail"),
	x_request_id("x-request-id"),
	x_b3_traceid("x-b3-traceid"),
	x_b3_spanid("x-b3-spanid"),
	x_b3_parentspanid("x-b3-parentspanid"),
	x_b3_sampled("x-b3-sampled"),
	x_b3_flags("x-b3-flags"),
	x_ot_span_context("x-ot-span-context")
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

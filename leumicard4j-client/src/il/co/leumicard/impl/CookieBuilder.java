package il.co.leumicard.impl;

public class CookieBuilder {
	private final StringBuilder builder = new StringBuilder();
	
	public CookieBuilder() {}
	
	public CookieBuilder add(String name, String value) {
		builder.append(name).append('=').append(value).append("; ");
		return this;
	}
	
	public String build() {
		return builder.toString();
	}
}

package com.seaway.game.manager.entity;

import org.apache.http.client.CookieStore;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultConnectionKeepAliveStrategy;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.impl.client.HttpClientBuilder;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BankerHttpEntity {

	private CloseableHttpClient httpClient;

	private HttpClientContext context;

	private CookieStore cookieStore;

	public BankerHttpEntity() {
		cookieStore = new BasicCookieStore();

		context = HttpClientContext.create();

		RequestConfig requestConfig = RequestConfig.custom()
				.setConnectTimeout(10 * 1000).setSocketTimeout(10 * 1000)
				.setConnectionRequestTimeout(10 * 1000)
				.setRedirectsEnabled(true).build();

		String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.132 Safari/537.36";

		httpClient = HttpClientBuilder.create()
				.setKeepAliveStrategy(new DefaultConnectionKeepAliveStrategy())
				.setRedirectStrategy(new DefaultRedirectStrategy())
				.setDefaultRequestConfig(requestConfig).setUserAgent(userAgent)
				.setDefaultCookieStore(cookieStore).build();
	}
}

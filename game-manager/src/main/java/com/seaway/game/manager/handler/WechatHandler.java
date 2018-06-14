package com.seaway.game.manager.handler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.google.gson.Gson;
import com.seaway.game.common.entity.manager.wechat.AccessToken;
import com.seaway.game.common.entity.manager.wechat.WechatUserInfo;
import com.seaway.game.common.utils.Constants;

@Slf4j
@Component
public class WechatHandler {

	@Value("${wechat.appid}")
	private String appId;

	@Value("${wechat.appsecret}")
	private String appSecret;

	private final String apiUrlPrefix = "https://api.weixin.qq.com/sns/";

	private CloseableHttpClient httpClient = null;

	@Autowired
	public WechatHandler() {
	}

	public WechatUserInfo getUserInfoByCode(String code) {
		AccessToken accessToken = getTokenByCode(code);
		if (accessToken == null) {
			return null;
		}

		if (StringUtils.isEmpty(accessToken.getAccessToken())) {
			return null;
		}

		return getUserInfoByAccessToken(accessToken);
	}

	private AccessToken getTokenByCode(String code) {
		Map<String, String> map = new LinkedHashMap<String, String>();
		map.put("appid", appId);
		map.put("secret", appSecret);
		map.put("code", code);
		map.put("grant_type", "authorization_code");

		String url = getUrlByParams("oauth2/access_token", map);

		String response = get(url);
		if (response == null) {
			return null;
		}

		Gson gson = new Gson();
		return gson.fromJson(response, AccessToken.class);
	}

	// private AccessToken refreshTokenByCode(String refreshToken) {
	// Map<String, String> map = new LinkedHashMap<String, String>();
	// map.put("appid", appId);
	// map.put("grant_type", "refresh_token");
	// map.put("refresh_token", refreshToken);
	//
	// String url = getUrlByParams("oauth2/refresh_token", map);
	//
	// String response = get(url);
	// if (response == null) {
	// return null;
	// }
	//
	// Gson gson = new Gson();
	// return gson.fromJson(response, AccessToken.class);
	// }

	private WechatUserInfo getUserInfoByAccessToken(AccessToken accessToken) {
		Map<String, String> map = new LinkedHashMap<String, String>();
		map.put("access_token", accessToken.getAccessToken());
		map.put("openid", accessToken.getOpenId());

		String url = getUrlByParams("userinfo", map);

		String response = get(url);
		if (response == null) {
			return null;
		}

		Gson gson = new Gson();
		WechatUserInfo wechatUserInfo = gson.fromJson(response,
				WechatUserInfo.class);
		if (wechatUserInfo != null) {
			downloadHeadImage(wechatUserInfo.getOpenId(),
					wechatUserInfo.getHeadImgUrl());
		}

		return wechatUserInfo;
	}

	private String get(String url) {
		log.info("Start to get {}", url);
		try {
			HttpGet httpGet = new HttpGet(url);

			CloseableHttpResponse response = getHttpClient().execute(httpGet);
			if (response.getStatusLine().getStatusCode() != HttpStatus.OK
					.value()) {
				return null;
			}

			String responseEntity = EntityUtils.toString(response.getEntity(),
					"UTF-8");
			response.close();

			log.info("Success to get {}, response {}", url, responseEntity);

			return responseEntity;
		} catch (Exception e) {
			log.warn("Exception in http get, reason {}", e.getMessage());
		}

		return null;
	}

	private void downloadHeadImage(String wxUid, String headImgUrl) {
		try {
			headImgUrl = headImgUrl.replaceAll("\\\\/", "/");
			HttpGet httpGet = new HttpGet(headImgUrl);

			CloseableHttpResponse response = getHttpClient().execute(httpGet);
			if (response.getStatusLine().getStatusCode() != HttpStatus.OK
					.value()) {
				return;
			}

			HttpEntity entity = response.getEntity();
			if (entity == null) {
				return;
			}

			String headImgPath = Constants.HEAD_IMAGE_FOLDER + wxUid + ".jpg";
			File file = new File(headImgPath);
			FileOutputStream fos = new FileOutputStream(file);
			InputStream is = entity.getContent();

			try {
				byte b[] = new byte[1024];
				int j = 0;
				while ((j = is.read(b)) != -1) {
					fos.write(b, 0, j);
				}
				fos.flush();
				fos.close();
			} catch (RuntimeException e) {
				httpGet.abort();
				throw e;
			} finally {
				try {
					is.close();
					fos.close();
					response.close();
				} catch (Exception ignore) {
				}
			}
		} catch (Exception e) {
			log.warn("Exception in http get, reason {}", e.getMessage());
		}

		return;
	}

	private CloseableHttpClient getHttpClient() {
		if (httpClient == null) {
			httpClient = HttpClientBuilder.create().build();
		}

		return httpClient;
	}

	private String getUrlByParams(String url, Map<String, String> params) {
		StringBuilder sb = new StringBuilder();
		sb.append(apiUrlPrefix);
		sb.append(url);
		sb.append("?");

		if (params != null && !params.isEmpty()) {
			params.forEach((key, value) -> {
				sb.append(key);
				sb.append("=");
				sb.append(value);
				sb.append("&");
			});
		}
		sb.deleteCharAt(sb.length() - 1);

		return sb.toString();
	}
}

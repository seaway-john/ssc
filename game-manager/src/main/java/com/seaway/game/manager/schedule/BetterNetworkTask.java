package com.seaway.game.manager.schedule;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

import lombok.extern.slf4j.Slf4j;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.seaway.game.common.entity.mysql.GameConfig;
import com.seaway.game.common.utils.TimeDuration;
import com.seaway.game.manager.entity.BetterNetworkEntity;

@Slf4j
@Component
public class BetterNetworkTask {

	private static String betterNetwork = null;

	private static Map<String, BetterNetworkEntity> networkDelayMap = new ConcurrentHashMap<>();

	@Autowired
	public BetterNetworkTask() {
	}

	@Async
	public void getNetworkTask(CloseableHttpClient httpClient,
			RequestConfig config, GameConfig gameConfig, CountDownLatch latch) {
		if (StringUtils.isEmpty(gameConfig.getValue())) {
			latch.countDown();
			return;
		}

		TimeDuration td = new TimeDuration();
		String network = null;
		try {
			network = gameConfig.getValue().toLowerCase();
			if (!network.startsWith("http")) {
				network = "http://" + network;
			}

			HttpGet httpGet = new HttpGet(network);
			httpGet.setConfig(config);

			CloseableHttpResponse response = httpClient.execute(httpGet);

			int status = response.getStatusLine().getStatusCode();
			long delay = td.stop();
			if (status == HttpStatus.OK.value()) {
				String responseEntity = EntityUtils.toString(
						response.getEntity(), "UTF-8");
				if (!StringUtils.isEmpty(responseEntity)
						&& responseEntity.contains("<title>LIPS</title>")) {
					BetterNetworkEntity entity = new BetterNetworkEntity();
					entity.setNetwork(network);
					entity.setDelay(delay);

					networkDelayMap.put(gameConfig.getAttribute(), entity);
				}
			}

			response.close();
		} catch (Exception e) {
		} finally {
			latch.countDown();
		}

	}

	public String getUrl(String suffix) {
		String network = StringUtils.isEmpty(betterNetwork) ? "http://iyx1h.4gg4.hy.0919999.net:894"
				: betterNetwork;

		return network + suffix;
	}

	public void generateBetter() {
		if (networkDelayMap.isEmpty()) {
			return;
		}

		BetterNetworkEntity betterEntity = null;
		for (BetterNetworkEntity entity : networkDelayMap.values()) {
			if (betterEntity == null
					|| betterEntity.getDelay() > entity.getDelay()) {
				betterEntity = entity;
			}
		}

		betterNetwork = (betterEntity == null) ? null : betterEntity
				.getNetwork();
		log.info("generateBetter network {}, delay {} ms", betterNetwork,
				(betterEntity == null) ? -1 : betterEntity.getDelay());
	}

	public String getDelayByAttribute(String attribute) {
		if (!networkDelayMap.containsKey(attribute)) {
			return "无法连接";
		}

		return networkDelayMap.get(attribute).getDelay() + "";
	}

	public void clearMap() {
		networkDelayMap.clear();
	}

}

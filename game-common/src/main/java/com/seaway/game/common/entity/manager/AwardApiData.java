package com.seaway.game.common.entity.manager;

import com.google.gson.annotations.SerializedName;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AwardApiData {

	@SerializedName("expect")
	private String expect;
	
	@SerializedName("opencode")
	private String openCode;

	@SerializedName("opentime")
	private String openTime;

	@SerializedName("opentimestamp")
	private long openTimestamp;

}

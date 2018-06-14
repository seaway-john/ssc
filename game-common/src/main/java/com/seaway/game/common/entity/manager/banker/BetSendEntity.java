package com.seaway.game.common.entity.manager.banker;

import java.util.List;
import java.util.Map;

import com.google.gson.annotations.SerializedName;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BetSendEntity {

	@SerializedName("BetAmt")
	private int betAmt;

	@SerializedName("BetNumber")
	private Map<String, List<String>> betNumber;

}

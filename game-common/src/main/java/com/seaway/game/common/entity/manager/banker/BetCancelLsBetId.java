package com.seaway.game.common.entity.manager.banker;

import com.google.gson.annotations.SerializedName;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BetCancelLsBetId {

	@SerializedName("BetInfoID")
	private int betInfoId;

	@SerializedName("BetNumber")
	private String betNumber;

}

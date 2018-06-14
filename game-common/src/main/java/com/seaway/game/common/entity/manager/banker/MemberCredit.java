package com.seaway.game.common.entity.manager.banker;

import java.util.List;

import com.google.gson.annotations.SerializedName;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemberCredit {

	@SerializedName("UserName")
	private String userName;

	@SerializedName("Credit")
	private float credit;

	@SerializedName("UsedCredit")
	private float usedCredit;

	@SerializedName("CancelBet")
	private int cancelBet;

	@SerializedName("SecondStopEarly")
	private String secondStopEarly;

	@SerializedName("BetInfo")
	private List<String> betInfo;

	@SerializedName("C")
	private boolean c;

}

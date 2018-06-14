package com.seaway.game.common.entity.manager.banker;

import java.util.Map;

import com.google.gson.annotations.SerializedName;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LastPeriods {

	@SerializedName("CompanyID")
	private String companyID;

	@SerializedName("ID")
	private String id;

	@SerializedName("CloseDt")
	private String closeDt;

	@SerializedName("DrawDt")
	private String drawDt;

	@SerializedName("OpenDt")
	private String openDt;

	@SerializedName("PeriodsStatus")
	private int periodsStatus;

	private boolean status;

	@SerializedName("PeriodsNumber")
	private String periodsNumber;

	private String info;

	private String nowDateTime;

	private Map<String, String> dw;

	private boolean isCheckOnline;

}

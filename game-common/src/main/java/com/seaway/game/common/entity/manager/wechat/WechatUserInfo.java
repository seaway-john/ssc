package com.seaway.game.common.entity.manager.wechat;

import java.util.List;

import com.google.gson.annotations.SerializedName;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WechatUserInfo {

	@SerializedName("openid")
	private String openId;

	@SerializedName("nickname")
	private String nickName;

	private int sex;

	private String language;

	private String province;

	private String city;

	private String country;

	@SerializedName("headimgurl")
	private String headImgUrl;

	private List<String> privilege;

	@SerializedName("unionid")
	private String unionId;

}

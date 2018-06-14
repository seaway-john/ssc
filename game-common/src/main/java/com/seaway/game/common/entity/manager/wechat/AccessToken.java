package com.seaway.game.common.entity.manager.wechat;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccessToken {

	@SerializedName("access_token")
	private String accessToken;

	@SerializedName("expires_in")
	private String expiresIn;

	@SerializedName("refresh_token")
	private String refreshToken;

	@SerializedName("openid")
	private String openId;

	private String scope;

	@SerializedName("unionid")
	private String unionId;

}

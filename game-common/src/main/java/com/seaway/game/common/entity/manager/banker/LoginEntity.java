package com.seaway.game.common.entity.manager.banker;

import com.google.gson.annotations.SerializedName;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginEntity {

	@SerializedName("Username")
	private String username;

	@SerializedName("Password")
	private String password;

}

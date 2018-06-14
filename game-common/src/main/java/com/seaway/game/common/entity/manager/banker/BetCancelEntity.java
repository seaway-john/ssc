package com.seaway.game.common.entity.manager.banker;

import java.util.List;
import com.google.gson.annotations.SerializedName;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BetCancelEntity {

	@SerializedName("LsBetIds")
	private List<BetCancelLsBetId> lsBetIds;

}

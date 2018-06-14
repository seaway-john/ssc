package com.seaway.game.common.entity.manager;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AwardApiInfo {
    
    private int rows;
    
    private String code;
    
    private String remain;
    
    public List<AwardApiData> data;

}

package com.seaway.game.common.entity.system;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ProcessInfo {

    private String name;

    private List<ProcessInfoUsage> info;

}

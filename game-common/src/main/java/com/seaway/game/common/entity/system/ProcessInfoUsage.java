package com.seaway.game.common.entity.system;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProcessInfoUsage {

    private int pid;

    private long cpuRate;

    private long ramRate;

    private int ram;

}

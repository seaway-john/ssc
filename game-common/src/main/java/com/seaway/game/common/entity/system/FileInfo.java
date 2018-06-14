package com.seaway.game.common.entity.system;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FileInfo {

	private String fileName;

	private long lastModifiedTime;

	private long size;

}

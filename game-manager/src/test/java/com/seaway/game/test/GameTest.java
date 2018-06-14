package com.seaway.game.test;

public class GameTest {

	public static void main(String[] args) {
		String token = "seaway^$^123456";

		System.out.println(token.contains("^$^"));
		
		System.out.println(token.indexOf("^$^"));
		
		System.out.println(token.split("\\^\\$\\^")[0]);
		
	}

}

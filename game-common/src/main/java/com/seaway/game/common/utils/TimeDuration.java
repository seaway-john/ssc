package com.seaway.game.common.utils;

public class TimeDuration {

	private long ts;

	public TimeDuration() {
		reset();
	}

	public void reset() {
		ts = getCurrentTs();
	}

	public long cost() {
		return getCurrentTs() - ts;
	}

	public long stop() {
		long cost = cost();

		reset();

		return cost;
	}

	private long getCurrentTs() {
		return System.currentTimeMillis();
	}

}

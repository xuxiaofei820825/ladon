package com.github.ladon.core.comm;

import lombok.Setter;

public abstract class AbstractCommPayloadHandler implements CommPayloadHandler {

	@Setter
	private CommPayloadHandler next;

	@Override
	public CommPayloadHandler next() {
		return this.next;
	}
}

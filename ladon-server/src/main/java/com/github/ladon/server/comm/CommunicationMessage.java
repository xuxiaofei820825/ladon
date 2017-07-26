package com.github.ladon.server.comm;

import lombok.Data;

/**
 * 该类是对通讯消息的抽象
 * 
 * @author xiaofei.xu
 *
 */
@Data
public class CommunicationMessage {

	/** 消息类型 */
	private String type;

	/** 消息的有效荷载 */
	private byte[] payload;

	public CommunicationMessage() {
	}

	public CommunicationMessage( final String type, final byte[] payload ) {
		this.type = type;
		this.payload = payload;
	}
}

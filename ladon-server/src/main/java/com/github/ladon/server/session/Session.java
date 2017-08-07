package com.github.ladon.server.session;

import io.netty.util.AttributeKey;
import lombok.Data;

@Data
public class Session {

	/** 会话键值 */
	public static final AttributeKey<Session> SessionKey = AttributeKey.newInstance( "Client_Session" );

	/** client identifier */
	private String clientId;

}

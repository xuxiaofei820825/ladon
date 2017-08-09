package com.github.ladon.server.session;

import java.util.Set;

import io.netty.util.AttributeKey;
import lombok.Data;

@Data
public class Session {

	/** 会话键值 */
	public static final AttributeKey<Session> SessionKey = AttributeKey.newInstance( "Client_Session" );
	
	public static final AttributeKey<Set<String>> TopicsKey = AttributeKey.newInstance( "Topic" );

	/** client identifier */
	private String clientId;

}

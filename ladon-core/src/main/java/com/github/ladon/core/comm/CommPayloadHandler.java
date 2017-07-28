package com.github.ladon.core.comm;

import io.netty.channel.ChannelHandlerContext;

public interface CommPayloadHandler {

	void handle( ChannelHandlerContext ctx, CommMessage msg );

	CommPayloadHandler next();

}

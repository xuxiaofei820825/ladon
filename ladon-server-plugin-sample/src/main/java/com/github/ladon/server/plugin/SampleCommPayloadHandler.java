package com.github.ladon.server.plugin;

import com.github.ladon.core.comm.AbstractCommPayloadHandler;
import com.github.ladon.core.comm.CommMessage;

import io.netty.channel.ChannelHandlerContext;

public class SampleCommPayloadHandler extends AbstractCommPayloadHandler {

	@Override
	public void handle( ChannelHandlerContext ctx, CommMessage msg ) {
		if ( this.next() != null )
			this.next().handle( ctx, msg );
	}
}

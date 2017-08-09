package com.github.ladon.client.channel.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.mqtt.MqttPublishMessage;

@Component
@Sharable
public class MqttPublishMessageHandler extends SimpleChannelInboundHandler<MqttPublishMessage> {

	/** logger */
	private final Logger logger = LoggerFactory.getLogger( MqttPublishMessageHandler.class );

	@Override
	protected void channelRead0( ChannelHandlerContext ctx, MqttPublishMessage msg ) throws Exception {

		// info log
		logger.info( "A publish message received. channel:{}, message:{}", ctx.channel(), msg );

		// 引用+1
		msg.retain();

	}

}

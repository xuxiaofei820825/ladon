package com.github.ladon.server.channel.handler;

import java.util.Set;

import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import com.github.ladon.server.TopicSubscriptionTable;
import com.github.ladon.server.common.Utils;
import com.github.ladon.server.session.Session;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttMessageType;

@Component
@Sharable
public class MqttDisconnectMessageHandler extends SimpleChannelInboundHandler<MqttMessage> {

	/** logger */
	private final Logger logger = Utils.getLogger();

	public void channelInactive( ChannelHandlerContext ctx ) throws Exception {
		final Set<String> topics = ctx.channel().attr( Session.TopicsKey ).get();

		if ( topics != null && topics.size() > 0 ) {
			topics.stream().forEach( topic -> {
				TopicSubscriptionTable.remove( topic, ctx.channel() );
			} );
		}

		ctx.fireChannelInactive();
	}

	@Override
	protected void channelRead0( ChannelHandlerContext ctx, MqttMessage msg ) throws Exception {

		if ( msg.fixedHeader().messageType() != MqttMessageType.DISCONNECT ) {
			ctx.fireChannelRead( msg );
			return;
		}

		// info log
		logger.info( "Received a disconnect message. channel:{}", ctx.channel() );

		// 关闭通道(异步)
		ChannelFuture future = ctx.channel().close();

		// 设置回调函数
		future.addListener( new ChannelFutureListener() {
			@Override
			public void operationComplete( ChannelFuture future ) throws Exception {
				if ( future.isSuccess() ) {
					logger.info( "Succeed to close channel of terminal. channel:{}", future.channel() );
				}
			}
		} );
	}

}

package com.github.ladon.client.channel.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.mqtt.MqttFixedHeader;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttMessageFactory;
import io.netty.handler.codec.mqtt.MqttMessageType;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

public class MqttPingMessageHandler extends SimpleChannelInboundHandler<MqttMessage> {

	/** logger */
	private static final Logger logger = LoggerFactory.getLogger( MqttPingMessageHandler.class );

	@Override
	public void userEventTriggered( ChannelHandlerContext ctx, Object evt )
			throws Exception {

		// 判定是否为写空闲
		// 如果是写空闲，则立即发送一条心跳消息维持TCP连接
		if ( evt instanceof IdleStateEvent ) {
			IdleStateEvent e = (IdleStateEvent) evt;

			if ( e.state() == IdleState.WRITER_IDLE ) {

				// debug
				logger.info( "Channel writer is idle, send a ping message." );

				MqttFixedHeader mqttFixedHeader = new MqttFixedHeader( MqttMessageType.PINGREQ,
						false, MqttQoS.AT_LEAST_ONCE, false, 0 );
				MqttMessage pingMsg = MqttMessageFactory.newMessage( mqttFixedHeader, null, null );

				// 发送一个无消息体的数据包
				// 这里必须调用writeAndFlush方法，立即发送一条消息
				ctx.channel().writeAndFlush( pingMsg );
			}
		}
	}

	@Override
	protected void channelRead0( ChannelHandlerContext ctx, MqttMessage msg ) throws Exception {

		if ( msg.fixedHeader().messageType() != MqttMessageType.PINGRESP ) {
			ctx.fireChannelRead( msg );
			return;
		}

		// info log
		logger.info( "Received a ping response message. channel:{}", ctx.channel() );
	}
}

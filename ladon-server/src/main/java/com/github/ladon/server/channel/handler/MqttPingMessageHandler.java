package com.github.ladon.server.channel.handler;

import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import com.github.ladon.server.common.Utils;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.mqtt.MqttFixedHeader;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttMessageFactory;
import io.netty.handler.codec.mqtt.MqttMessageType;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

@Component
@Sharable
public class MqttPingMessageHandler extends SimpleChannelInboundHandler<MqttMessage> {

	/** logger */
	private final Logger logger = Utils.getLogger();

	@Override
	public void userEventTriggered( ChannelHandlerContext ctx, Object evt )
			throws Exception {

		// 指定时间间隔内未收到心跳消息时，关闭连接。
		if ( evt instanceof IdleStateEvent ) {
			// 判断当前事件是否为IdleStateEvent

			IdleStateEvent event = (IdleStateEvent) evt;

			// 通道(Channel)上读空闲，终端未发送心跳包维持连接，或者可能物理链路已断开
			if ( event.state().equals( IdleState.READER_IDLE ) ) {

				// info log
				logger.info( "The channel{} is idle, closing the channel......", ctx.channel() );

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

			// 终止处理
			return;
		}

		// 转发事件
		super.userEventTriggered( ctx, evt );
	}

	@Override
	protected void channelRead0( ChannelHandlerContext ctx, MqttMessage msg ) throws Exception {

		if ( msg.fixedHeader().messageType() != MqttMessageType.PINGREQ ) {
			ctx.fireChannelRead( msg );
			return;
		}

		// info log
		logger.info( "Received a ping request message. channel:{}", ctx.channel() );

		MqttFixedHeader mqttFixedHeader = new MqttFixedHeader( MqttMessageType.PINGRESP,
				false, MqttQoS.AT_LEAST_ONCE, false, 0 );
		MqttMessage pingMsg = MqttMessageFactory.newMessage( mqttFixedHeader, null, null );

		if ( ctx.channel().isActive() )
			ctx.writeAndFlush( pingMsg );
	}

}

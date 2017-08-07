package com.github.ladon.server.channel.handler;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;

import com.github.ladon.server.common.Utils;
import com.github.ladon.server.session.Session;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.mqtt.MqttConnAckMessage;
import io.netty.handler.codec.mqtt.MqttConnectMessage;
import io.netty.handler.codec.mqtt.MqttConnectReturnCode;
import io.netty.handler.codec.mqtt.MqttMessageBuilders;
import io.netty.handler.timeout.IdleStateHandler;

public class MqttConnectMessageHandler extends SimpleChannelInboundHandler<MqttConnectMessage> {

	/** logger */
	private final static Logger logger = Utils.getLogger();

	@Override
	protected void channelRead0( ChannelHandlerContext ctx, MqttConnectMessage msg ) throws Exception {

		// info log
		logger.info( "Received a connect message. channel:{}, connect message:{}", ctx.channel(), msg );

		// 如果设置了KeepAlive时间(>0)
		// 则使用 KeepAlive * 1.5 作为最大心跳间隔时间
		resetHeartbeatIntervalIfNecessary( ctx, msg );

		final String clientId = msg.payload().clientIdentifier();

		Session session = new Session();
		session.setClientId( clientId );

		// 设置会话
		ctx.channel()
				.attr( Session.SessionKey )
				.set( session );

		// 发送CONNECTACK报文
		MqttConnAckMessage connectAck = MqttMessageBuilders.connAck()
				.returnCode( MqttConnectReturnCode.CONNECTION_ACCEPTED )
				.build();
		ctx.writeAndFlush( connectAck );
	}

	/*
	 * 重置心跳间隔时间
	 */
	private void resetHeartbeatIntervalIfNecessary( ChannelHandlerContext ctx, MqttConnectMessage msg ) {
		final int keepAliveTimeSeconds = (int) ( msg.variableHeader().keepAliveTimeSeconds() * 1.5 );
		if ( keepAliveTimeSeconds > 0 ) {
			IdleStateHandler idleStateHandler = new IdleStateHandler(
					keepAliveTimeSeconds, 0, 0, TimeUnit.SECONDS );
			ctx.channel().pipeline()
					.replace( "default_idle_handler", "client_idle_handler", idleStateHandler );
		}
	}

}

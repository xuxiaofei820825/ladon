package com.github.ladon.server.channel.handler;

import org.apache.kafka.common.utils.Bytes;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import com.github.ladon.server.common.Utils;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.mqtt.MqttPublishMessage;

@Component
@Sharable
public class MqttPublishMessageHandler extends SimpleChannelInboundHandler<MqttPublishMessage> {

	/** logger */
	private final Logger logger = Utils.getLogger();

	@Autowired
	private KafkaTemplate<String, Bytes> template;

	@Override
	protected void channelRead0( ChannelHandlerContext ctx, MqttPublishMessage msg ) throws Exception {

		// info log
		logger.info( "A publish message received. channel:{}, message:{}", ctx.channel(), msg );

		// 引用+1
		msg.retain();

		// 获取topic名
		final String topicName = msg.variableHeader().topicName();
		final int packetId = msg.variableHeader().packetId();

		byte[] payload = new byte[msg.payload().readableBytes()];
		msg.payload().readBytes( payload );

		ListenableFuture<SendResult<String, Bytes>> rs = template.send( topicName, new Bytes( payload ) );

		rs.addCallback( new ListenableFutureCallback<Object>() {

			@Override
			public void onSuccess( Object result ) {
				// info log
				logger.info( "Succeed to send message to topic:{}, packet_id:{}", topicName, packetId );
			}

			@Override
			public void onFailure( Throwable ex ) {
				// info log
				logger.info( "Failed to send message to topic:{}, packet_id:{}", topicName, packetId );
			}
		} );

	}

}

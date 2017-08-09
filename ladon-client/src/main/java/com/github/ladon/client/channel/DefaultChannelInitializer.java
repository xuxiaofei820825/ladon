package com.github.ladon.client.channel;

import java.util.concurrent.TimeUnit;

import com.github.ladon.client.channel.handler.MqttPingMessageHandler;
import com.github.ladon.client.channel.handler.MqttPublishMessageHandler;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.mqtt.MqttDecoder;
import io.netty.handler.codec.mqtt.MqttEncoder;
import io.netty.handler.timeout.IdleStateHandler;

/**
 * 实现一个默认的客户端通道初始化器
 * 
 * @author xiaofei.xu
 * 
 */
public class DefaultChannelInitializer extends ChannelInitializer<SocketChannel> {

	@Override
	protected void initChannel( SocketChannel ch ) throws Exception {
		ChannelPipeline pipeline = ch.pipeline();

		// MQTT编/解码器
		pipeline.addLast( "mqtt_decoder", new MqttDecoder() );
		pipeline.addLast( "mqtt_encoder", MqttEncoder.INSTANCE );

		// 心跳保活
		pipeline.addLast( new IdleStateHandler( 0, 60, 0, TimeUnit.SECONDS ) )
				.addLast( "heartbeat", new MqttPingMessageHandler() );

		pipeline.addLast( "mqtt_publish_message_handler", new MqttPublishMessageHandler() );
	}

}

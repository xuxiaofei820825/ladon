package com.github.ladon.server.channel;

import java.util.Set;

import org.apache.kafka.common.utils.Bytes;
import org.slf4j.Logger;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import com.github.ladon.server.TopicSubscriptionTable;
import com.github.ladon.server.common.Utils;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.MqttMessageBuilders;
import io.netty.handler.codec.mqtt.MqttPublishMessage;
import io.netty.handler.codec.mqtt.MqttQoS;

@Component
public class MqttPublishMessageConsumer {

	/** logger */
	private final Logger logger = Utils.getLogger();

	@KafkaListener(topicPattern = "App[0-9A-Z]*")
	public void onReceiving( @Header(KafkaHeaders.OFFSET) Integer offset,
			@Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition,
			@Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
			@Payload Bytes data ) {

		// info log
		logger.info( "Processing topic = {}, partition = {}, offset = {}",
				topic, partition, offset );

		final Set<Channel> channels = TopicSubscriptionTable.get( topic );

		if ( channels != null && channels.size() > 0 )
			channels.stream().forEach( channel -> {

				MqttPublishMessage publishMsg = MqttMessageBuilders.publish()
						.qos( MqttQoS.AT_LEAST_ONCE )
						.topicName( topic )
						.payload( Unpooled.wrappedBuffer( data.get() ) )
						.messageId( 1000 )
						.build();

				if ( channel.isActive() )
					channel.writeAndFlush( publishMsg );
			} );
	}

}

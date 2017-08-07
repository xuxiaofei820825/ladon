package com.github.ladon.server.channel;

import org.slf4j.Logger;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import com.github.ladon.server.common.Utils;

@Component
public class MqttPublishMessageConsumer {

	/** logger */
	private final Logger logger = Utils.getLogger();

	@KafkaListener(topicPattern = "App[0-9A-Z]*")
	public void onReceiving( @Header(KafkaHeaders.OFFSET) Integer offset,
			@Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition,
			@Header(KafkaHeaders.RECEIVED_TOPIC) String topic ) {

		// info log
		logger.info( "Processing topic = {}, partition = {}, offset = {}",
				topic, partition, offset );
	}

}

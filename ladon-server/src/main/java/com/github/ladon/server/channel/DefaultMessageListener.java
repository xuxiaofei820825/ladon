package com.github.ladon.server.channel;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.utils.Bytes;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.stereotype.Component;

@Component
public class DefaultMessageListener implements MessageListener<String, Bytes> {

	@Override
	public void onMessage( ConsumerRecord<String, Bytes> data ) {
		
		System.out.println( "KKKKKKKKKKKKKKKKKKKKKKK................................" );

	}
}

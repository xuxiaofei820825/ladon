package com.github.ladon.server.channel.handler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.github.ladon.core.comm.TopicFilter;
import com.github.ladon.server.common.Utils;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.mqtt.MqttSubscribeMessage;

@Component
@Sharable
public class MqttSubscribeMessageHandler extends SimpleChannelInboundHandler<MqttSubscribeMessage> {
	
	/** KAFKA服务器集群 */
	@Value("${spring.kafka.bootstrapServers}")
	private String bootstrapServers;

	/** logger */
	private final Logger logger = Utils.getLogger();
	
	@Override
	protected void channelRead0( ChannelHandlerContext ctx, MqttSubscribeMessage msg ) throws Exception {

		// info log
		logger.info( "A subscribe message received. channel:{}, message:{}", ctx.channel(), msg );

		final List<TopicFilter> filters = new ArrayList<TopicFilter>();

		msg.payload().topicSubscriptions().stream().forEach( sub -> {
			final String name = sub.topicName();
			try {
				filters.add( TopicFilter.valueOf( name ) );
			}
			catch ( Exception ex ) {
				logger.warn( "Failed to convert filter string:{}", name );
			}
		} );

		ExecutorService executors = Executors.newFixedThreadPool( 1 );
		executors.execute( new Runnable() {
			@Override
			public void run() {
				
				Properties props = new Properties();
				props.put( "bootstrap.servers", bootstrapServers );
				props.put( "group.id", "test" );
				props.put( "enable.auto.commit", "false" );
				props.put( "auto.commit.interval.ms", "1000" );
				props.put( "session.timeout.ms", "30000" );
				props.put( "key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer" );
				props.put( "value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer" );

				KafkaConsumer<String, String> consumer = new KafkaConsumer<>( props );

				try {

					filters.stream().forEach( filter -> {
						TopicPartition partition = new TopicPartition( filter.getTopicName(), 0 );
						consumer.assign( Arrays.asList( partition ) );
						consumer.seek( partition, filter.getOffset() );
					} );

					final int minBatchSize = 200;
					List<ConsumerRecord<String, String>> buffer = new ArrayList<>();
					ConsumerRecords<String, String> records = consumer.poll( 100000 );
					for ( ConsumerRecord<String, String> record : records ) {

						// info log
						logger.info( "History message. topic:{}, offset:{}", record.topic(), record.offset() );

						buffer.add( record );
					}
					if ( buffer.size() >= minBatchSize ) {
						consumer.commitSync();
						buffer.clear();
					}
				}
				finally {
					if ( consumer != null )
						consumer.close();
				}
			}
		} );

		// 关闭线程池
		executors.shutdown();
	}

}

package com.github.ladon.client;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.github.ladon.core.comm.TopicFilter;

/**
 * Application Launcher
 */
@SpringBootApplication
public class AppLauncher implements CommandLineRunner {

	private static final String TOPIC = "AppA";

	/**
	 * 启动入口
	 * 
	 * @param args
	 *          启动参数
	 * @throws Exception
	 */
	public static void main( String[] args ) throws Exception {
		SpringApplication.run( AppLauncher.class, args );
	}

	@Override
	public void run( String... args ) throws Exception {

		//DefaultClientBootstrap bootstrap = new DefaultClientBootstrap( "localhost", 2392 );
		//bootstrap.connect();

		LadonClient client = DefaultLadonClient.builder()
				.host( "localhost" )
				.port( 2392 )
				.keepAlive( 60 )
				.clientId( "D00001" )
				.build();
		client.connect();
		
		// subscribe
		TopicFilter topicFilter = new TopicFilter();
		topicFilter.setOffset( 0 );
		topicFilter.setTopicName( TOPIC );
		client.subscribe( topicFilter );

		// publish message
		client.publish( TOPIC, new String( "Hello world" ).getBytes() );
	}
}

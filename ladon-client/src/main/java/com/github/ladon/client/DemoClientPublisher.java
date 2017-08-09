package com.github.ladon.client;

import java.util.UUID;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DemoClientPublisher implements CommandLineRunner {

	private static final String TOPIC = "AppA";

	/**
	 * 启动入口
	 * 
	 * @param args
	 *          启动参数
	 * @throws Exception
	 */
	public static void main( String[] args ) throws Exception {
		SpringApplication.run( DemoClientPublisher.class, args );
	}

	@Override
	public void run( String... args ) throws Exception {

		LadonClient client = DefaultLadonClient.builder()
				.host( "localhost" )
				.port( 2392 )
				.keepAlive( 60 )
				.clientId( UUID.randomUUID().toString().replaceAll( "-", "" ) )
				.build();

		// connect to server
		client.connect();

		// publish message
		client.publish( TOPIC, new String( "Hello world" ).getBytes() );
	}
}

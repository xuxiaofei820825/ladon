package com.github.ladon.client;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.ladon.client.channel.DefaultChannelInitializer;
import com.github.ladon.core.comm.TopicFilter;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.mqtt.MqttConnectMessage;
import io.netty.handler.codec.mqtt.MqttMessageBuilders;
import io.netty.handler.codec.mqtt.MqttPublishMessage;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.netty.handler.codec.mqtt.MqttSubscribeMessage;

public class DefaultClientBootstrap {

	/** logger */
	private static final Logger logger = LoggerFactory.getLogger( DefaultClientBootstrap.class );

	/** 服务端地址,端口号 */
	private final String host;
	private final int port;

	/** IO线程组 */
	private final Bootstrap boss;
	private final EventLoopGroup group;

	/** 当前用户终端连接通道 */
	private Channel channel;

	/**
	 * 私有构造函数
	 * 
	 * @param host
	 *          服务端地址
	 * @param port
	 *          端口号
	 */
	public DefaultClientBootstrap( final String host, final int port ) {
		this.host = host;
		this.port = port;

		// 建立主线程与IO线程
		group = new NioEventLoopGroup( 1 );
		boss = new Bootstrap();

		// 设置通道初始化器
		DefaultChannelInitializer channelInitializer = new DefaultChannelInitializer();

		boss.group( group )
				.channel( NioSocketChannel.class )
				.option( ChannelOption.SO_KEEPALIVE, true )
				.remoteAddress( this.host, this.port )
				.handler( channelInitializer );
	}

	/**
	 * 与服务端建立连接
	 */
	public void connect() {

		// 连接到服务器(异步请求)
		ChannelFuture future = boss.connect();

		// info
		logger.info( "Connecting to ladon server. host:{}, port:{}", this.host, this.port );

		// 设置监听器
		future.addListener( new ChannelFutureListener() {

			@Override
			public void operationComplete( ChannelFuture future ) throws Exception {

				// 连接被取消
				if ( future.isCancelled() ) {
					throw new RuntimeException( "Connection operation is cancelled." );
				}

				// 连接未成功
				if ( !future.isSuccess() ) {
					// error log
					if ( logger.isErrorEnabled() )
						logger.error( "Failed to connect ladon server. " + future.cause().getMessage(), future.cause() );
				}
				else {
					// log
					logger.info( "Succeed to connect ladon server. host:{}, port:{}", host, port );

					// 缓存已建立的Channel
					channel = future.channel();

					// Connect
					MqttConnectMessage connectMsg = MqttMessageBuilders.connect()
							.keepAlive( 60 )
							.cleanSession( true )
							.clientId( UUID.randomUUID().toString().replaceAll( "-", "" ) )
							.build();

					channel.writeAndFlush( connectMsg );

					// Subscribe
					TopicFilter topicFilter = new TopicFilter();
					topicFilter.setOffset( 3 );
					topicFilter.setTopicName( "AppA" );
					
					MqttSubscribeMessage subMsg = MqttMessageBuilders.subscribe()
							.messageId( 1000 )
							.addSubscription( MqttQoS.AT_MOST_ONCE, topicFilter.string() )
							.build();
					channel.writeAndFlush( subMsg );

					// Publish message
					ByteBuf payload = ByteBufAllocator.DEFAULT.buffer( 4 );
					payload.writeInt( 10 );

					MqttPublishMessage publishMsg = MqttMessageBuilders.publish()
							.qos( MqttQoS.AT_MOST_ONCE )
							.topicName( "AppA" )
							.payload( payload )
							.messageId( 1000 )
							.build();

					channel.writeAndFlush( publishMsg );
				}
			}
		} );
	}

}

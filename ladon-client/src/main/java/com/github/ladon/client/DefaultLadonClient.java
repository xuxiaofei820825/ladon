package com.github.ladon.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.ladon.client.channel.DefaultChannelInitializer;
import com.github.ladon.core.comm.TopicFilter;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.mqtt.MqttConnAckMessage;
import io.netty.handler.codec.mqtt.MqttConnectMessage;
import io.netty.handler.codec.mqtt.MqttMessageBuilders;
import io.netty.handler.codec.mqtt.MqttPublishMessage;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.netty.handler.codec.mqtt.MqttSubscribeMessage;
import lombok.Setter;

public class DefaultLadonClient implements LadonClient {

	/** logger */
	private static final Logger logger = LoggerFactory.getLogger( DefaultLadonClient.class );

	/** 服务端地址,端口号 */
	private final String host;
	private final int port;

	@Setter
	private int keepAlive;

	@Setter
	private String clientId;

	/** IO线程组 */
	private final Bootstrap boss;
	private final EventLoopGroup group;

	/** 当前用户终端连接通道 */
	private Channel channel;

	/** 线程锁 */
	private final Object lock = new Object();

	/**
	 * 私有构造函数
	 * 
	 * @param host
	 *          服务端地址
	 * @param port
	 *          端口号
	 */
	public DefaultLadonClient( final String host, final int port ) {
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
	@Override
	public void connect() {

		// info
		logger.info( "Connecting to ladon server. host:{}, port:{}", this.host, this.port );

		// 连接到服务器(异步请求)
		ChannelFuture future = boss.connect();

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

					channel.pipeline()
							.addLast( "connect_ack_handler", new SimpleChannelInboundHandler<MqttConnAckMessage>() {
								@Override
								protected void channelRead0( ChannelHandlerContext ctx, MqttConnAckMessage msg ) throws Exception {

									// log
									logger.info( "Succeed to get connect ack from ladon server, connection completed." );

									// 唤醒主线程
									synchronized ( lock ) {
										lock.notifyAll();
									}
								}
							} );

					// Send Connect message
					MqttConnectMessage connectMsg = MqttMessageBuilders.connect()
							.keepAlive( keepAlive )
							.cleanSession( true )
							.clientId( clientId )
							.build();

					channel.writeAndFlush( connectMsg );
				}
			}
		} );

		// 主线程等待连接结果
		synchronized ( lock ) {

			try {
				// info log
				logger.info( "Waiting for connect to ladon server......" );
				// wait
				lock.wait();
			}
			catch ( InterruptedException e ) {
				// ignore

				// error
				logger.error( "Error occured.", e );
			}
		}
	}

	@Override
	public void subscribe( TopicFilter topicFilter ) throws Exception {

		// Subscribe
		MqttSubscribeMessage subMsg = MqttMessageBuilders.subscribe()
				.messageId( 1000 )
				.addSubscription( MqttQoS.AT_MOST_ONCE, topicFilter.string() )
				.build();

		if ( channel != null )
			channel.writeAndFlush( subMsg );
	}

	@Override
	public void publish( String topic, byte[] payload ) throws Exception {

		// wrap byte[]
		ByteBuf payloadBuf = Unpooled.wrappedBuffer( payload );

		MqttPublishMessage publishMsg = MqttMessageBuilders.publish()
				.qos( MqttQoS.AT_MOST_ONCE )
				.topicName( topic )
				.payload( payloadBuf )
				.messageId( 1000 )
				.build();

		if ( channel != null )
			channel.writeAndFlush( publishMsg );
	}

	/**
	 * Create a builder instance
	 * 
	 * @return builder instance
	 */
	public static DefaultLadonClientBuilder builder() {
		return new DefaultLadonClientBuilder();
	}
}

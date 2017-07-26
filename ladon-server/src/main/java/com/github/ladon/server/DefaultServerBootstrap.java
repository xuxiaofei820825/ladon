package com.github.ladon.server;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.github.ladon.server.common.Utils;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

@Component
public class DefaultServerBootstrap {

	/** logger */
	private final static Logger logger = Utils.getLogger();

	/** Acceptor Reactor */
	private final EventLoopGroup bossGroup;

	/** Event Reactor */
	private EventLoopGroup workerGroup;

	@Autowired
	private ServerConfig config;

	/** 通道初始化器 */
	@Autowired
	private ChannelInitializer<SocketChannel> channelInitializer;

	public DefaultServerBootstrap() {
		this.bossGroup = new NioEventLoopGroup();
	}

	public void start() throws Exception {

		// 创建工作线程池
		final int nthread = config.getWorker().getNthread();
		if ( nthread < 0 )
			this.workerGroup = new NioEventLoopGroup();
		else
			this.workerGroup = new NioEventLoopGroup( nthread );

		// info log
		logger.info( "Starting the ladon server......" );

		try {

			ServerBootstrap bootstrap = new ServerBootstrap();
			bootstrap.group( bossGroup, workerGroup )
					.channel( NioServerSocketChannel.class )
					.handler( new LoggingHandler( LogLevel.INFO ) )
					.childHandler( channelInitializer );

			// 绑定监听端口
			ChannelFuture future = bootstrap.bind( config.getPort() ).sync();
			future.addListener( new ChannelFutureListener() {
				@Override
				public void operationComplete( ChannelFuture future ) throws Exception {
					if ( future.isSuccess() ) {
						// info log
						logger.info( "Succeed to start ladon server. listening on port: {}", config.getPort() );
					}
				}
			} );

			// 等待关闭
			future.channel()
					.closeFuture()
					.sync();
		}
		finally {
			// 关闭
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
		}
	}

}

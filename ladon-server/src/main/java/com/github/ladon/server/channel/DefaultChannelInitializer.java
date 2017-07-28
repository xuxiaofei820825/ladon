package com.github.ladon.server.channel;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLException;

import org.slf4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

import com.github.ladon.server.ServerConfig;
import com.github.ladon.server.channel.codec.CommMessageCodec;
import com.github.ladon.server.channel.handler.DefaultCommPayloadHandler;
import com.github.ladon.server.channel.handler.HeartbeatHandler;
import com.github.ladon.server.common.Utils;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.timeout.IdleStateHandler;

@Component
public class DefaultChannelInitializer extends ChannelInitializer<SocketChannel> implements InitializingBean {

	/** logger */
	private final Logger logger = Utils.getLogger();

	@Autowired
	private ServerConfig config;

	/** SSL Context */
	private SslContext sslContext;

	@Autowired
	private HeartbeatHandler heartbeatHandler;

	@Autowired
	private DefaultCommPayloadHandler commPayloadHandler;

	public void afterPropertiesSet() throws Exception {
		// 设置SSLContext
		setSSLContext();
	}

	@Override
	protected void initChannel( SocketChannel channel ) throws Exception {
		// log
		logger.info( "Initialing the channel......" );

		ChannelPipeline pipeline = channel.pipeline();

		// #1.设置SSL
		// 是否使用SSL
		final boolean isSSLEnabled = config.getSsl().isEnabled();
		if ( isSSLEnabled )
			pipeline.addLast( sslContext.newHandler( channel.alloc() ) );

		// #2.设置通讯包编/解码器(进、出)
		pipeline.addLast( "comm_codec", new CommMessageCodec() );

		// #3.心跳检测处理器
		IdleStateHandler idleStateHandler = new IdleStateHandler(
				config.getHeartbeat(), 0, 0, TimeUnit.SECONDS );
		pipeline.addLast( "idle", idleStateHandler );
		pipeline.addLast( "heartbeat_handler", heartbeatHandler );

		// #4.设置消息处理器
		pipeline.addLast( "comm_payload_handler", commPayloadHandler );
	}

	// ===========================================================================
	// private functions

	private void setSSLContext() throws FileNotFoundException, SSLException {
		// 是否使用SSL
		final boolean isSslEnabled = config.getSsl().isEnabled();

		if ( isSslEnabled ) {

			// info log
			logger.info( "Starting to set the SSL context......" );

			// 加载证书和秘钥文件
			File crtFile = ResourceUtils.getFile( config.getSsl().getCrtFile() );
			File pkFile = ResourceUtils.getFile( config.getSsl().getPkFile() );

			sslContext = SslContextBuilder.forServer( crtFile, pkFile,
					config.getSsl().getKeyPassword() )
					.build();

			// info log
			logger.info( "Succeed to set the SSL context." );
		}
	}
}

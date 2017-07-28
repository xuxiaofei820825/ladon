package com.github.ladon.server.channel.handler;

import java.util.Set;

import org.joor.Reflect;
import org.slf4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import com.github.ladon.core.comm.AbstractCommPayloadHandler;
import com.github.ladon.core.comm.CommMessage;
import com.github.ladon.core.comm.CommPayloadHandler;
import com.github.ladon.server.common.PackageUtils;
import com.github.ladon.server.common.Utils;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * 
 * @author xiaofei.xu
 * 
 */
@Component
@Sharable
public class DefaultCommPayloadHandler extends SimpleChannelInboundHandler<CommMessage> implements InitializingBean {

	/** logger */
	private final static Logger logger = Utils.getLogger();

	/** 有效荷载处理器 */
	private CommPayloadHandler commPayloadHandler;

	@Override
	public void afterPropertiesSet() throws Exception {

		// 注册通讯荷载处理器
		commPayloadHandler = registerCommPayloadHandler();
	}

	@Override
	protected void channelRead0( ChannelHandlerContext ctx, CommMessage msg ) throws Exception {
		commPayloadHandler.handle( ctx, msg );
	}

	// ========================================================================
	// private functions

	/*
	 * 构建一个处理器链
	 */
	private CommPayloadHandler registerCommPayloadHandler() {
		// info log
		logger.info( "Starting to register communication payload handler......" );

		Set<String> clazzes = PackageUtils.findPackageClass( "com.github.ladon.server.plugin" );

		AbstractCommPayloadHandler chain = null,
				pre_handler = null;

		if ( clazzes.size() == 0 )
			return new TailCommPayloadHandler();

		int idx = 0;
		for ( String clazz : clazzes ) {

			// create instance
			Object instance = Reflect.on( clazz ).create().get();

			if ( instance instanceof AbstractCommPayloadHandler ) {
				// info log
				logger.info( "register handler: {}", clazz );

				AbstractCommPayloadHandler handler = (AbstractCommPayloadHandler) instance;

				if ( idx == 0 )
					chain = handler;

				// 创建处理器链
				if ( pre_handler != null )
					pre_handler.setNext( handler );

				pre_handler = handler;
				idx++;
			}
		}

		// 添加一个尾部处理器，输出日志
		if ( pre_handler != null )
			pre_handler.setNext( new TailCommPayloadHandler() );

		// info log
		logger.info( "Succeed to register communication payload handler." );

		return chain;
	}

	class TailCommPayloadHandler extends AbstractCommPayloadHandler {

		@Override
		public void handle( ChannelHandlerContext ctx, CommMessage msg ) {
			// info log
			logger.info( "No communication payload handler process the message." );
		}
	}
}

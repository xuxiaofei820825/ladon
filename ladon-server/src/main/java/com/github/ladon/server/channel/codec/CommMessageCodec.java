package com.github.ladon.server.channel.codec;

import java.nio.charset.Charset;
import java.util.List;

import org.slf4j.Logger;

import com.github.ladon.core.comm.CommMessage;
import com.github.ladon.server.common.Utils;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;

/**
 * 该类实现通讯包的编解码操作。<br/>
 * 一个完整的通讯包的构成协议如下:<br/>
 * ------------------------------------------------------------------------------<br/>
 * | 荷载类型字节数 | 荷载属性字节数 | 荷载字节数 | 荷载类型 | 荷载属性 | 有效荷载 |<br/>
 * ------------------------------------------------------------------------------<br/>
 * 
 * @author xiaofei.xu
 * 
 */
public class CommMessageCodec extends ByteToMessageCodec<CommMessage> {

	/** logger */
	private final Logger logger = Utils.getLogger();

	/**
	 * 报文解析的当前状态，初始化为INIT
	 */
	private State currentState = State.INIT;

	/** 通讯包各部分字节数 */
	private int typeLen = 0;
	private int payloadLen = 0;

	/** 通讯包有效荷载 */
	private CommMessage commPayload;

	/**
	 * 报文解析状态
	 */
	private enum State {
		INIT, PAYLOAD_TYPE, PAYLOAD_PROPERTIES, PAYLOAD_BODY
	}

	@Override
	protected void encode( ChannelHandlerContext ctx, CommMessage msg, ByteBuf out ) throws Exception {
		byte[] bytesType = msg.getType()
				.getBytes( Charset.forName( "UTF-8" ) );

		// debug log
		logger.debug( "type:{}, payload:{} bytes", msg.getType(), msg.getPayload().length );

		// 输出各部分的长度
		out.writeShort( bytesType.length );
		// out.writeShort( msg.getProperties().length );
		out.writeShort( msg.getPayload().length );

		// 输出各部分
		out.writeBytes( bytesType );
		// out.writeBytes( msg.getProperties() );
		out.writeBytes( msg.getPayload() );
	}

	@Override
	protected void decode( ChannelHandlerContext ctx, ByteBuf in, List<Object> out ) throws Exception {

		// 根据当前的解码状态做处理
		switch ( currentState ) {
		case INIT:

			// 如果ByteBuf中可读字节数 < 6，则等待下次通道可读时继续解析
			if ( in.readableBytes() < 6 )
				return;

			// 获取通讯包各部分的字节数
			this.typeLen = in.readShort();
			// this.propertiesLen = in.readShort();
			this.payloadLen = in.readShort();

			// 否则状态迁移到解析PAYLOAD_TYPE
			this.currentState = State.PAYLOAD_TYPE;
		case PAYLOAD_TYPE:

			// 如果Buffer中的可读字节数不够读取Type，则等待下次通道可读时继续解析
			if ( in.readableBytes() < this.typeLen )
				return;

			// 读取Type
			byte[] bytesType = new byte[this.typeLen];
			in.readBytes( bytesType );

			// 转化为String
			String type = new String( bytesType, Charset.forName( "UTF-8" ) );

			// debug
			logger.debug( "Type of communication payload: {}", type );

			// 创建对象
			this.commPayload = new CommMessage();
			this.commPayload.setType( type );

			// 状态迁移到PAYLOAD_BODY
			this.currentState = State.PAYLOAD_BODY;
		case PAYLOAD_BODY:

			// 如果Buffer中的可读字节数不够读取包体，则等待下次通道可读时继续解析
			if ( in.readableBytes() < this.payloadLen )
				return;

			// 读取包体
			byte[] bytesBody = new byte[this.payloadLen];
			in.readBytes( bytesBody );

			this.commPayload.setPayload( bytesBody );

			// 通讯包读取结束
			out.add( this.commPayload );

			// 报文解析完毕，初始化状态，等待解析下一段报文
			init();
		default:
			break;
		}
	}

	/**
	 * 初始化解码状态
	 */
	private void init() {
		// 恢复初始状态
		this.currentState = State.INIT;

		this.typeLen = 0;
		this.payloadLen = 0;
	}
}

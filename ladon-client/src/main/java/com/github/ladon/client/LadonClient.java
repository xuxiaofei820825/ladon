package com.github.ladon.client;

import com.github.ladon.core.comm.TopicFilter;

public interface LadonClient {

	/**
	 * 建立与服务端的Socket连接
	 */
	void connect();

	/**
	 * 订阅主题
	 * 
	 * @param topicFilter
	 *          订阅主题的过滤器
	 */
	void subscribe( TopicFilter topicFilter ) throws Exception;

	/**
	 * 发布消息到主题
	 * 
	 * @param topic
	 *          主题名
	 * @param payload
	 *          有效荷载
	 */
	void publish( String topic, byte[] payload ) throws Exception;
}

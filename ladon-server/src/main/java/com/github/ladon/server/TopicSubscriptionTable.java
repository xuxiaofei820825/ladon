package com.github.ladon.server;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import io.netty.channel.Channel;

public abstract class TopicSubscriptionTable {

	private final static Map<String, Set<Channel>> topicSubscriptionTable = new ConcurrentHashMap<String, Set<Channel>>();

	public static void add( final String topic, final Channel channel ) {

		Set<Channel> channels = topicSubscriptionTable.get( topic );
		if ( channels == null ) {
			channels = new HashSet<Channel>();
			topicSubscriptionTable.put( topic, channels );
		}

		channels.add( channel );
	}

	public static boolean remove( final String topic, final Channel channel ) {
		Set<Channel> channels = topicSubscriptionTable.get( topic );
		return channels == null ? false : channels.remove( channel );
	}

	public static Set<Channel> get( final String topic ) {
		return topicSubscriptionTable.get( topic );
	}
}

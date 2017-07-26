package com.github.ladon.server.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Utils {

	private final static Logger logger = LoggerFactory.getLogger( "LadonServer" );

	public static Logger getLogger() {
		return logger;
	}

}

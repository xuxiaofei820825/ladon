package com.github.ladon.client;

public class DefaultLadonClientBuilder {

	/** 服务端地址,端口号 */
	private String host = "localhost";
	private int port = 2391;
	private int keepAlive = 60;
	private String clientId;

	public DefaultLadonClientBuilder host( final String host ) {
		this.host = host;
		return this;
	}

	public DefaultLadonClientBuilder port( final int port ) {
		this.port = port;
		return this;
	}

	public DefaultLadonClientBuilder keepAlive( final int keepAlive ) {
		this.keepAlive = keepAlive;
		return this;
	}

	public DefaultLadonClientBuilder clientId( final String clientId ) {
		this.clientId = clientId;
		return this;
	}

	public DefaultLadonClient build() {
		DefaultLadonClient client = new DefaultLadonClient( this.host, this.port );

		client.setKeepAlive( keepAlive );
		client.setClientId( clientId );

		return client;
	}

}

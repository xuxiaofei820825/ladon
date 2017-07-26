package com.github.ladon.server;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Configuration
@ConfigurationProperties(prefix = "server")
@Data
public class ServerConfig {

	/** 服务监听端口(默认2391) */
	private int port = 2391;

	/** 心跳包间隔时间(单位：秒，默认60) */
	private int heartbeat = 60;

	/** SSL相关配置 */
	private final SSL ssl = new SSL();

	/** NETTY4工作线程配置 */
	private final Worker worker = new Worker();

	@Data
	public static class Worker {
		private int nthread = -1;
	}

	@Data
	public static class SSL {

		/** 是否使用加密传输 */
		private boolean enabled = false;

		/** 秘钥文件加密密码 */
		private String keyPassword;

		/** 秘钥文件 */
		private String pkFile;

		/** 证书文件 */
		private String crtFile;

	}
}

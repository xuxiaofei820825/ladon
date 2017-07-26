package com.github.ladon.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Application Launcher
 */
@SpringBootApplication
public class AppLauncher implements CommandLineRunner {

	@Autowired
	private DefaultServerBootstrap bootstrap;

	/**
	 * 启动入口
	 * 
	 * @param args
	 *          启动参数
	 * @throws Exception
	 */
	public static void main( String[] args ) throws Exception {
		SpringApplication.run( AppLauncher.class, args );
	}

	@Override
	public void run( String... args ) throws Exception {
		bootstrap.start();
	}
}

package com.github.ladon;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 应用入口
 */
@SpringBootApplication
public class AppLauncher {
	public static void main( String[] args ) {
		// start application
		SpringApplication.run( AppLauncher.class, args );
	}
}

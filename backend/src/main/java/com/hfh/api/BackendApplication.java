package com.hfh.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.scheduling.annotation.EnableScheduling;

@Slf4j
@SpringBootApplication
@EnableScheduling
public class BackendApplication {

    public static void main(String[] args) {
        ConfigurableEnvironment environment = SpringApplication.run(BackendApplication.class, args).getEnvironment();

        String applicationName = environment.getProperty("spring.application.name");
        String serverPort = environment.getProperty("server.port");

        log.info("""
                
                +----------------------------------------------------------------+
                 Application: '{}' is running Success!
                 Document:     http://localhost:{}/doc.html
                +----------------------------------------------------------------+
                """, applicationName, serverPort);

    }

}

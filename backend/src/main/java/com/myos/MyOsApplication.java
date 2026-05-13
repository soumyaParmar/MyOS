package com.myos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * The main entry point for the MyOS Spring Boot application.
 *
 * WHAT IS @SpringBootApplication?
 * This is a convenience annotation that combines three annotations:
 *   1. @Configuration    — Marks this class as a source of bean definitions (app config).
 *   2. @EnableAutoConfiguration — Tells Spring Boot to automatically configure beans
 *                                  based on the dependencies in your pom.xml
 *                                  (e.g., if you have spring-boot-starter-web, it auto-configures
 *                                  an embedded Tomcat server).
 *   3. @ComponentScan    — Tells Spring to scan this package and all sub-packages
 *                          (com.myos.*) to find classes annotated with @Component, @Service,
 *                          @Repository, @Controller, etc., and register them as Spring beans.
 *
 * Think of this class as the "ignition key" of the application — everything starts here.
 */
@SpringBootApplication
public class MyOsApplication {

    /**
     * The standard Java main() method — this is where the JVM starts execution.
     *
     * SpringApplication.run() does the heavy lifting:
     *   1. Creates the Spring ApplicationContext (the container that holds all beans).
     *   2. Starts the embedded web server (Tomcat by default).
     *   3. Runs Flyway database migrations.
     *   4. Initializes all beans (@Service, @Repository, @Controller, @Configuration, etc.).
     *
     * @param args command-line arguments passed when starting the app (e.g., --server.port=9090)
     */
    public static void main(String[] args) {
        SpringApplication.run(MyOsApplication.class, args);
    }

}

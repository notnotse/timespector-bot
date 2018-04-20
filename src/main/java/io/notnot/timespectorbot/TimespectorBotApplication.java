package io.notnot.timespectorbot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication(scanBasePackages = {"me.ramswaroop.jbot", "io.notnot.timespectorbot"})
public class TimespectorBotApplication {
    
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    } 

    /**
     * Entry point of the application. Run this method to start the sample bots,
     * but don't forget to add the correct tokens in application.properties file.
     *
     * @param args
     */
    public static void main(String[] args) {
        SpringApplication.run(TimespectorBotApplication.class, args);
    }
}

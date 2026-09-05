package com.kaiwa;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class KaiwaApplication {

    public static void main(String[] args) {
        SpringApplication.run(KaiwaApplication.class, args);
    }

    @Bean
    CommandLineRunner mongoUriCheck(org.springframework.core.env.Environment env) {
        return args -> {
            String mongoUri = env.getProperty("spring.data.mongodb.uri");
            if (mongoUri == null || mongoUri.isBlank() || mongoUri.contains("${MONGO_URI}")) {
                throw new IllegalStateException(
                        "MONGO_URI environment variable is not set. "
                                + "Set MONGO_URI (e.g. mongodb+srv://user:password@cluster0.xxxxx.mongodb.net/?retryWrites=true&w=majority) "
                                + "before starting the Kaiwa application.");
            }
        };
    }
}

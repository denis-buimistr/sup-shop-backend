package com.supshop.sup_shop_backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sesv2.SesV2Client;

// клиент нужен только на проде, локально письма уходят в Mailpit
@Configuration
@Profile("prod")
public class SesConfig {

    @Bean
    public SesV2Client sesClient(@Value("${aws.ses.region}") String region) {
        return SesV2Client.builder()
                .region(Region.of(region))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }
}

package com.example.jampot.global.config;

import com.mongodb.client.MongoClient;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;


@Configuration
@RequiredArgsConstructor
@EnableMongoAuditing
@EnableMongoRepositories(
        basePackages = "com.example.jampot.domain.chat.privateChat.mongodbRepository",// MongoDB 전용 Repository 패키지
        mongoTemplateRef = "mongoTemplate"
)
public class MongoConfig {
    private final MongoProperties mongoProperties;


    @Bean
    public MongoDatabaseFactory mongoDatabaseFactory() {
        return new SimpleMongoClientDatabaseFactory(mongoProperties.getUri());
    }

    @Bean
    public MongoTemplate mongoTemplate(MongoDatabaseFactory mongoDatabaseFactory) {
        return new MongoTemplate(mongoDatabaseFactory);
    }
}

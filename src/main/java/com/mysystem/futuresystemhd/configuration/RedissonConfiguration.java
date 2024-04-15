package com.mysystem.futuresystemhd.configuration;

import lombok.Data;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "spring.redis")
@Data
public class RedissonConfiguration {

    private String port;

    private String host;

    @Bean
    public RedissonClient redissonClient(){
        Config config = new Config();
        String redisAddress = String.format("redis://%s:%s",host,port);
        config.useSingleServer().setAddress(redisAddress).setDatabase(4);

        RedissonClient redissonClient = Redisson.create(config);
        return redissonClient;
    }


}

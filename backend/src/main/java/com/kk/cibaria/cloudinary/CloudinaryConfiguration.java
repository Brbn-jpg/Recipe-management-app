package com.kk.cibaria.cloudinary;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.cloudinary.Cloudinary;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class CloudinaryConfiguration {

    @Value("${API_KEY}")
    private String apiKey;

    @Value("${API_SECRET_KEY}")
    private String secretKey;

    @Value("${CLOUD_NAME}")
    private String cloudName;


    @Bean
    public Cloudinary cloudinary(){
        Map<String,Object> config = new HashMap<>();
        config.put("api_key", apiKey);
        config.put("api_secret",secretKey);
        config.put("cloud_name",cloudName);

        return new Cloudinary(config);
    }
}

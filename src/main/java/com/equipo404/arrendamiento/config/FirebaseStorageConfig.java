package com.equipo404.arrendamiento.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Bucket;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.StorageClient;
import java.io.IOException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.util.StringUtils;

@Configuration(proxyBeanMethods = false)
@Conditional(FirebaseStorageConfig.BucketConfigurado.class)
public class FirebaseStorageConfig {

    @Bean(destroyMethod = "delete")
    FirebaseApp firebaseStorageApp(AlmacenamientoProperties propiedades) throws IOException {
        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.getApplicationDefault())
                .setStorageBucket(propiedades.bucket().trim())
                .build();
        return FirebaseApp.initializeApp(options, "habitacheck-storage");
    }

    @Bean
    Bucket firebaseStorageBucket(FirebaseApp firebaseStorageApp) {
        return StorageClient.getInstance(firebaseStorageApp).bucket();
    }

    public static class BucketConfigurado implements Condition {
        @Override
        public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
            return StringUtils.hasText(context.getEnvironment().getProperty("habitacheck.almacenamiento.bucket"));
        }
    }
}

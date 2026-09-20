package com.assic.muni.infrastructure.config;

import feign.Client;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

@Configuration
@EnableFeignClients(basePackages = "com.assic.muni.infrastructure.client")
public class FeignClientConfig {

    @Value("${keycloak.ssl.disable-trust-manager:false}")
    private boolean disableTrustManager;

    @Bean
    public Client feignClient() throws NoSuchAlgorithmException, KeyManagementException {
        if (disableTrustManager) {
            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(X509Certificate[] certs, String authType) {}

                        @Override
                        public void checkServerTrusted(X509Certificate[] certs, String authType) {}

                        @Override
                        public X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[0];
                        }
                    }
            };

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCerts, new SecureRandom());

            HostnameVerifier permissiveHostnameVerifier = (hostname, session) -> true;

            return new Client.Default(sslContext.getSocketFactory(), permissiveHostnameVerifier);
        }
        return new Client.Default(null, null);
    }
}

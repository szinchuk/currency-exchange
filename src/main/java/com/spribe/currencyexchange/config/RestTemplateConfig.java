package com.spribe.currencyexchange.config;

import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.SSLContext;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.http.io.SocketConfig;
import org.apache.hc.core5.pool.PoolConcurrencyPolicy;
import org.apache.hc.core5.pool.PoolReusePolicy;
import org.apache.hc.core5.util.Timeout;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

  @Value("${application.template.max.conn.total}")
  private int maxConnTotal;

  @Value("${application.template.max.conn.per.route}")
  private int maxConnPerRoute;

  @Value("${application.template.connection.timeout}")
  private int connectionTimeout;

  @Value("${application.template.read.timeout}")
  private int readTimeout;

  @Bean
  public RestTemplate restTemplate() throws NoSuchAlgorithmException {
    SSLContext sslContext = SSLContext.getDefault();
    SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext);

    HttpClientConnectionManager cm = PoolingHttpClientConnectionManagerBuilder.create()
        .setSSLSocketFactory(csf)
        .setDefaultSocketConfig(SocketConfig.custom()
            .setSoTimeout(Timeout.of(readTimeout, TimeUnit.MILLISECONDS))
            .build())
        .setPoolConcurrencyPolicy(PoolConcurrencyPolicy.STRICT)
        .setConnPoolPolicy(PoolReusePolicy.LIFO)
        .setMaxConnTotal(maxConnTotal)
        .setMaxConnPerRoute(maxConnPerRoute)
        .build();

    CloseableHttpClient httpClient = HttpClients.custom()
        .setConnectionManager(cm)
        .build();

    HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
    requestFactory.setConnectTimeout(connectionTimeout);
    requestFactory.setConnectionRequestTimeout(readTimeout);

    return new RestTemplate(requestFactory);
  }
}

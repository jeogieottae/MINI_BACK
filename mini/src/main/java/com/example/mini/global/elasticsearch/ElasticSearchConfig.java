package com.example.mini.global.elasticsearch;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.TransportUtils;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

import javax.net.ssl.*;

@Configuration
@EnableElasticsearchRepositories
public class ElasticSearchConfig {

    @Value("${spring.data.elasticsearch.username}")
    String username;

    @Value("${spring.data.elasticsearch.password}")
    String password;

    @Value("${spring.data.elasticsearch.host}")
    String host;

    @Value("${spring.data.elasticsearch.port}")
    int port;

    @Value("${spring.data.elasticsearch.fingerprint}")
    String fingerprint;

    @Bean
    public RestClient restClient() {
        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));

        SSLContext sslContext = TransportUtils
            .sslContextFromCaFingerprint(fingerprint);

        return RestClient.builder(new HttpHost(host, port, "https"))
            .setHttpClientConfigCallback(httpClientBuilder -> httpClientBuilder
                .setSSLContext(sslContext)
                .setDefaultCredentialsProvider(credentialsProvider)
                .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE))
            .build();
    }

    @Bean
    public ElasticsearchClient elasticsearchClient(RestClient restClient) {
        ElasticsearchTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());
        return new ElasticsearchClient(transport);
    }
}
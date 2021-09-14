package de.ersatzhero.ticketregionalpipe.configuration;

import de.ersatzhero.ticketregionalpipe.api.OSM;
import de.ersatzhero.ticketregionalpipe.client.GeolocationClient;
import de.ersatzhero.ticketregionalpipe.client.OSMGeolocationClient;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

@Configuration
public class ClientsConfiguration {
    @Bean(destroyMethod = "close")
    public RestHighLevelClient elasticSearchRestClient(@Value("#{environment.ELASTICSEARCH_HOST}") String host,
                                                       @Value("#{environment.ELASTICSEARCH_PORT}") int port,
                                                       @Value("#{environment.ELASTICSEARCH_SCHEME}") String scheme) {
        return new RestHighLevelClient(RestClient.builder(
                new HttpHost(host, port, scheme)
        ));
    }

    @Bean
    public GeolocationClient osmGeolocationClient(OSM osmClient, @Value("#{environment.CLIENT_GEOLOCATION_COUNTRIES}") String countries, @Value("#{environment.CLIENT_GEOLOCATION_CALLEREMAIL}") String callerEmail) {
        return new OSMGeolocationClient(osmClient, countries, callerEmail);
    }
}

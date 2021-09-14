package de.ersatzhero.ticketregionalpipe.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class DatabaseConfiguration {
    @Bean
    public DataSource dataSource(@Value("#{environment.DB_PASSWORD") String password, @Value("#{environment.DB_SCHEMA") String schema) {
        DataSourceBuilder dataSourceBuilder = DataSourceBuilder.create();
        dataSourceBuilder.driverClassName("org.mariadb.jdbc.Driver");
        dataSourceBuilder.url("jdbc:mariadb://mariadb:3306/" + schema);
        dataSourceBuilder.username("root");
        dataSourceBuilder.password(password);
        return dataSourceBuilder.build();
    }
}

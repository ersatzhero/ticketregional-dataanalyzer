package de.ersatzhero.ticketregionalpipe.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class DatabaseConfiguration {
    @Bean
    public DataSource dataSource(@Value("#{environment.DB_PASSWORD}") String password) {
        DataSourceBuilder dataSourceBuilder = DataSourceBuilder.create();
        dataSourceBuilder.driverClassName("org.mariadb.jdbc.Driver");
        dataSourceBuilder.url("jdbc:mariadb://database01:3306/");
        dataSourceBuilder.username("root");
        dataSourceBuilder.password(password);
        return dataSourceBuilder.build();
    }
}

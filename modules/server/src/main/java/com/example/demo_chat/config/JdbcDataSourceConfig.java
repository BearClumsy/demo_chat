package com.example.demo_chat.config;

import javax.sql.DataSource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Boot's own {@code DataSourceAutoConfiguration} backs off whenever an R2DBC {@code
 * ConnectionFactory} bean is present (it assumes one persistence style per app), which this app
 * already has for {@code users}. Flyway's own autoconfiguration builds its own internal {@link
 * DataSource} from {@code spring.datasource.*} independently of this bean, so it is unaffected;
 * this bean exists only to give {@code PgVectorStoreAutoConfiguration} and {@link
 * SemanticCacheVectorStoreConfig} the plain JDBC {@link JdbcTemplate} they require — a second,
 * intentionally separate connection style to the same Postgres instance.
 */
@Configuration
public class JdbcDataSourceConfig {

  @Bean
  @ConfigurationProperties("spring.datasource")
  public DataSourceProperties dataSourceProperties() {
    return new DataSourceProperties();
  }

  @Bean
  @ConditionalOnMissingBean(DataSource.class)
  @ConfigurationProperties("spring.datasource.hikari")
  public DataSource dataSource(DataSourceProperties properties) {
    return properties.initializeDataSourceBuilder().build();
  }

  @Bean
  @ConditionalOnMissingBean(JdbcTemplate.class)
  public JdbcTemplate jdbcTemplate(DataSource dataSource) {
    return new JdbcTemplate(dataSource);
  }
}

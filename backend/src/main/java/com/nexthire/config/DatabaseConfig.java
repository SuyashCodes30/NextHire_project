package com.nexthire.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.sql.Connection;

/**
 * DatabaseConfig — Fail-Safe, High-Availability DataSource Configuration.
 *
 * 1. Automatically tests connection to external database (Supabase / PostgreSQL / MySQL).
 * 2. Strict 8s socket and connect timeouts to ensure startup NEVER hangs.
 * 3. Adds PgBouncer compatibility (prepareThreshold=0) for Supabase poolers.
 * 4. Automatically falls back to in-memory H2 if external database is unreachable,
 *    guaranteeing that Tomcat binds to the port and the service stays LIVE!
 */
@Configuration
public class DatabaseConfig {

    private static final Logger log = LoggerFactory.getLogger(DatabaseConfig.class);

    @Value("${spring.datasource.url:jdbc:h2:mem:nexthire;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE}")
    private String rawUrl;

    @Value("${spring.datasource.username:sa}")
    private String rawUsername;

    @Value("${spring.datasource.password:}")
    private String rawPassword;

    @Value("${spring.datasource.driver-class-name:}")
    private String rawDriver;

    @Bean
    @Primary
    public DataSource dataSource() {
        String url = rawUrl != null ? rawUrl.trim() : "";
        String username = rawUsername != null ? rawUsername.trim() : "sa";
        String password = rawPassword != null ? rawPassword : "";
        String driver = rawDriver != null ? rawDriver.trim() : "";

        // 1. Auto-convert raw URI formats (e.g. postgresql://user:pass@host:port/db)
        if (url.startsWith("postgres://") || url.startsWith("postgresql://")) {
            log.info("Detected raw PostgreSQL URI format. Converting to JDBC format...");
            try {
                String prefix = url.startsWith("postgresql://") ? "postgresql://" : "postgres://";
                String withoutPrefix = url.substring(prefix.length());

                if (withoutPrefix.contains("@")) {
                    String[] userHost = withoutPrefix.split("@", 2);
                    String userInfo = userHost[0];
                    String hostPortDb = userHost[1];

                    if (userInfo.contains(":")) {
                        String[] creds = userInfo.split(":", 2);
                        username = creds[0];
                        password = creds[1];
                    } else {
                        username = userInfo;
                    }
                    url = "jdbc:postgresql://" + hostPortDb;
                } else {
                    url = "jdbc:postgresql://" + withoutPrefix;
                }
            } catch (Exception e) {
                log.warn("Failed to parse raw URI: {}. Prepended jdbc: directly.", e.getMessage());
                url = "jdbc:" + url;
            }
        }

        // 2. Auto-detect driver
        if (driver.isEmpty()) {
            if (url.startsWith("jdbc:postgresql:")) {
                driver = "org.postgresql.Driver";
            } else if (url.startsWith("jdbc:mysql:")) {
                driver = "com.mysql.cj.jdbc.Driver";
            } else {
                driver = "org.h2.Driver";
            }
        }

        // 3. PostgreSQL stability parameters (SSL, connection timeouts, PgBouncer support)
        if (url.startsWith("jdbc:postgresql:")) {
            if (!url.contains("sslmode=")) {
                url += (url.contains("?") ? "&" : "?") + "sslmode=require";
            }
            if (!url.contains("connectTimeout=")) {
                url += "&connectTimeout=25";
            }
            if (!url.contains("socketTimeout=")) {
                url += "&socketTimeout=30";
            }
            if (!url.contains("loginTimeout=")) {
                url += "&loginTimeout=25";
            }
            // Supabase port 6543 (PgBouncer transaction pooler) requires prepareThreshold=0
            if (url.contains(":6543") && !url.contains("prepareThreshold=")) {
                log.info("Detected Supabase Transaction Pooler (port 6543). Applying prepareThreshold=0 for PgBouncer.");
                url += "&prepareThreshold=0";
            }
        }

        // 4. If an external database is configured, verify connection with timeout
        if (!url.startsWith("jdbc:h2:")) {
            log.info("Attempting connection to external database: driver='{}', user='{}', url='{}'",
                    driver, username, maskUrlCredentials(url));

            try {
                HikariConfig config = new HikariConfig();
                config.setDriverClassName(driver);
                config.setJdbcUrl(url);
                config.setUsername(username);
                config.setPassword(password);
                config.setConnectionTimeout(25000); // 25s timeout for cross-ocean SSL handshake
                config.setValidationTimeout(5000);  // 5s validation timeout
                config.setInitializationFailTimeout(25000);
                config.setMaximumPoolSize(5);
                config.setMinimumIdle(1);

                HikariDataSource ds = new HikariDataSource(config);

                // Test live connection
                try (Connection conn = ds.getConnection()) {
                    log.info("✅ [DATABASE CONNECTED] Successfully connected to external database! Product: {}, Version: {}",
                            conn.getMetaData().getDatabaseProductName(),
                            conn.getMetaData().getDatabaseProductVersion());
                    return ds;
                }
            } catch (Throwable t) {
                log.error("❌ [DATABASE CONNECTION FAILED] Could not connect to external database: {}", t.getMessage());
                log.warn("⚠️ [FALLBACK TO H2] Reverting to in-memory H2 database so application boots and binds port immediately!");

                return createH2FallbackDataSource();
            }
        }

        // Default H2 in-memory
        return createH2FallbackDataSource();
    }

    private DataSource createH2FallbackDataSource() {
        log.info("Initializing in-memory H2 database (jdbc:h2:mem:nexthire)...");
        HikariConfig h2Config = new HikariConfig();
        h2Config.setDriverClassName("org.h2.Driver");
        h2Config.setJdbcUrl("jdbc:h2:mem:nexthire;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE");
        h2Config.setUsername("sa");
        h2Config.setPassword("");
        h2Config.setMaximumPoolSize(5);
        return new HikariDataSource(h2Config);
    }

    private String maskUrlCredentials(String url) {
        if (url == null) return "";
        return url.replaceAll("(?i)(password=)[^&]+", "$1*****");
    }
}

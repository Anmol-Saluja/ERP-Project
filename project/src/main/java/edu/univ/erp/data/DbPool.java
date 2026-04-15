package edu.univ.erp.data;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.InputStream;
import java.util.Properties;

public class DbPool {
    private static HikariDataSource authDataSource;
    private static HikariDataSource erpDataSource;
    static {
        try (InputStream input = DbPool.class.getClassLoader().getResourceAsStream("app.properties")) {
            if (input == null) {
                throw new RuntimeException("Could not find app.properties in resources folder!");
            }
            Properties props = new Properties();
            props.load(input);
            String authUrl = props.getProperty("auth.jdbcUrl");
            String authUser = props.getProperty("auth.username");
            String authPass = props.getProperty("auth.password");
            if (authUrl == null || authUser == null) {
                throw new RuntimeException("Missing auth_db configuration in app.properties!");
            }
            HikariConfig authConfig = new HikariConfig();
            authConfig.setJdbcUrl(authUrl);
            authConfig.setUsername(authUser);
            authConfig.setPassword(authPass);
            authConfig.setPoolName("AuthDBPool");
            authDataSource = new HikariDataSource(authConfig);
            String erpUrl = props.getProperty("erp.jdbcUrl");
            String erpUser = props.getProperty("erp.username");
            String erpPass = props.getProperty("erp.password");
            if (erpUrl == null || erpUser == null) {
                throw new RuntimeException("Missing erp_db configuration in app.properties!");
            }
            HikariConfig erpConfig = new HikariConfig();
            erpConfig.setJdbcUrl(erpUrl);
            erpConfig.setUsername(erpUser);
            erpConfig.setPassword(erpPass);
            erpConfig.setPoolName("ErpDBPool");
            erpDataSource = new HikariDataSource(erpConfig);
        } catch (Exception e) {
            System.err.println("Failed to initialize database pools: " + e.getMessage());
            e.printStackTrace();
        }
    }
    public static HikariDataSource getAuthDataSource() {
        return authDataSource;
    }
    public static HikariDataSource getErpDataSource() {
        return erpDataSource;
    }
}

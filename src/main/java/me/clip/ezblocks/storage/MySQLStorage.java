package me.clip.ezblocks.storage;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class MySQLStorage extends SQLStorage {

    private static final String DB_HOST = "localhost";
    private static final String DB_PORT = "3306";
    private static final String DB_NAME = "ezblocks";  // todo config
    private static final String DB_USER = "ezblocks";
    private static final String DB_PASSWORD = "Welcome01!";

    private static final HikariConfig CONFIG = new HikariConfig();

    static {
        CONFIG.setJdbcUrl(String.format("jdbc:mysql://%s:%s/%s?useUnicode=true&characterEncoding=utf8", DB_HOST, DB_PORT, DB_NAME));
        CONFIG.setUsername(DB_USER);
        CONFIG.setPassword(DB_PASSWORD);
        CONFIG.setDriverClassName("com.mysql.cj.jdbc.Driver");
    }

    @Override
    public void initialize() {
        hikari = new HikariDataSource(CONFIG);

        createTables();
    }

    @Override
    public void close() {
        hikari.close();
    }
}

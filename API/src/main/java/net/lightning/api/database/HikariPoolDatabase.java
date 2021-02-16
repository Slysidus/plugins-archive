package net.lightning.api.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import net.lightning.api.database.providers.SQLDatabaseProvider;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Consumer;

public class HikariPoolDatabase extends SQLDatabaseProvider {

    private final HikariDataSource hikariDataSource;

    public HikariPoolDatabase(String connectionUrl, Consumer<HikariConfig> configure, String... params) {
        super(connectionUrl, params);

        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(super.connectionUrl);

        // Default settings
        hikariConfig.setMaximumPoolSize(10);
//        hikariConfig.setIdleTimeout(300000);
        hikariConfig.setConnectionTimeout(5000);
        hikariConfig.setLeakDetectionThreshold(300000);

        if (configure != null) {
            configure.accept(hikariConfig);
        }
        this.hikariDataSource = new HikariDataSource(hikariConfig);
    }

    @Override
    public void login() {
    }

    @Override
    public void disconnect() {
        hikariDataSource.close();
    }

    @Override
    public Connection getConnection() throws SQLException {
        return hikariDataSource.getConnection();
    }

}

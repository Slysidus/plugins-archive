package net.lightning.api.database;

import com.zaxxer.hikari.HikariConfig;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.function.Consumer;

public class MySQLHikaryDatabase extends HikariPoolDatabase {

    public MySQLHikaryDatabase(String host, int port, String dbName, String username, String password,
                               Consumer<HikariConfig> configure, String... params) {
        super("jdbc:mysql://" + host + ":" + port + "/" + dbName, config -> {
            config.setUsername(username);
            config.setPassword(password);

            if (configure != null) {
                configure.accept(config);
            }
        }, params);
    }

    public MySQLHikaryDatabase(String host, int port, String dbName, String username, String password, String... params) {
        this(host, port, dbName, username, password, null, params);
    }

    @Override
    public void postRequest(Connection connection, PreparedStatement preparedStatement) throws SQLException {
        super.postRequest(connection, preparedStatement);
        if (connection != null) {
            connection.close();
        }
    }

}

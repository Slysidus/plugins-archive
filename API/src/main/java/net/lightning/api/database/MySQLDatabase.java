package net.lightning.api.database;

import lombok.Getter;
import net.lightning.api.database.providers.SQLDatabaseProvider;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MySQLDatabase extends SQLDatabaseProvider {

    private final String username, password;

    @Getter
    private Connection connection;

    public MySQLDatabase(String host, int port, String dbName, String username, String password, String... params) {
        super("jdbc:mysql://" + host + ":" + port + "/" + dbName, params);
        this.username = username;
        this.password = password;
    }

    @Override
    public void login() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            return;
        }

        this.connection = DriverManager.getConnection(connectionUrl, username, password);
    }

    @Override
    public void disconnect() throws SQLException {
        if (connection != null) {
            connection.close();
            connection = null;
        }
    }

}

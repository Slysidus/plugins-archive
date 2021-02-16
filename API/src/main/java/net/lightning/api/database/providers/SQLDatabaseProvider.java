package net.lightning.api.database.providers;

import lombok.Setter;
import net.lightning.api.database.model.ModelAccessor;
import net.lightning.api.database.model.ModelField;
import net.lightning.api.database.model.ModelTypeAccessor;
import net.lightning.api.database.query.SelectionQuery;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class SQLDatabaseProvider implements DatabaseProvider {

    protected final String connectionUrl;

    @Setter
    private boolean logQueries;

    protected SQLDatabaseProvider(String connectionURI, String... params) {
        if (params.length > 0) {
            connectionURI += "?" + String.join("&", params);
        }
        this.connectionUrl = connectionURI;
    }

    public abstract Connection getConnection() throws SQLException;

    private void logQuery(String query) {
        if (logQueries) {
            System.out.println("SQL Query >> " + query);
        }
    }

    @Override
    public boolean createTable(ModelAccessor<?> modelAccessor, boolean force) throws SQLException {
        String query = (force ? "CREATE OR REPLACE TABLE" : "CREATE TABLE IF NOT EXISTS") +
                " " +
                modelAccessor.getTableName() + " (" +
                modelAccessor.getFields().values().stream()
                        .map(typeAccessor -> {
                            ModelField options = typeAccessor.getOptions();
                            StringBuilder fieldBuilder = new StringBuilder(typeAccessor.getFieldName())
                                    .append(" ").append(typeAccessor.getTypeName());

                            if (options.length() > 0) {
                                fieldBuilder.append("(").append(options.length()).append(")");
                            }
                            else if (typeAccessor.getDefaultLength() > 0) {
                                fieldBuilder.append("(").append(typeAccessor.getDefaultLength()).append(")");
                            }
                            else if (typeAccessor.getDefaultLength() == -2) {
                                fieldBuilder.append("(MAX)");
                            }

                            if (options.unique()) {
                                fieldBuilder.append(" UNIQUE");
                            }
                            if (options.primaryKey()) {
                                fieldBuilder.append(" PRIMARY KEY");
                            }
                            if (options.autoIncrement()) {
                                fieldBuilder.append(" AUTO_INCREMENT");
                            }
                            if (!options.nullable()) {
                                fieldBuilder.append(" NOT NULL");
                            }

                            return fieldBuilder.toString();
                        }).collect(Collectors.joining(", ")) +
                ");";
        logQuery(query);

        Connection connection = getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        final boolean success = preparedStatement.execute();
        postRequest(connection, preparedStatement);
        return success;
    }

    @Override
    public <T> T get(ModelAccessor<T> modelAccessor, SelectionQuery selectionQuery)
            throws SQLException, IllegalAccessException, InvocationTargetException, InstantiationException {
        selectionQuery.validate(modelAccessor);

        StringBuilder queryBuilder = new StringBuilder("SELECT ")
                .append(String.join(", ", modelAccessor.getFieldsByName().keySet()))
                .append(" FROM ")
                .append(modelAccessor.getTableName())
                .append(" WHERE ");

        if (selectionQuery.getWhere().isEmpty()) {
            queryBuilder.append("1");
        }
        else {
            queryBuilder.append(selectionQuery.getWhere().keySet().stream()
                    .map(field -> field + " = ?")
                    .collect(Collectors.joining(" AND ")));
        }

        queryBuilder.append(";");
        final String query = queryBuilder.toString();
        logQuery(query);

        Connection connection = getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        int index = 1;
        for (Map.Entry<String, Object> entry : selectionQuery.getWhere().entrySet()) {
            ModelTypeAccessor<?> typeAccessor = modelAccessor.getFields().get(modelAccessor.getFieldsByName().get(entry.getKey()));
            typeAccessor.castSet(preparedStatement, index++, entry.getValue());
        }

        T instance = null;
        ResultSet resultSet = preparedStatement.executeQuery();
        if (resultSet.next()) {
            Object[] objects = new Object[modelAccessor.getFields().size()];

            index = 0;
            for (ModelTypeAccessor<?> typeAccessor : modelAccessor.getFields().values()) {
                objects[index++] = typeAccessor.get(resultSet, typeAccessor.getFieldName());
            }

            instance = modelAccessor.getConstructor().newInstance(objects);
        }

        resultSet.close();
        postRequest(connection, preparedStatement);
        return instance;
    }

    @Override
    public <T> Collection<T> select(ModelAccessor<T> modelAccessor, SelectionQuery selectionQuery) throws SQLException {
        return null;
    }

    @Override
    public <T> boolean insert(ModelAccessor<T> modelAccessor, T instance) throws Exception {
        StringBuilder queryBuilder = new StringBuilder("INSERT INTO ")
                .append(modelAccessor.getTableName()).append(" (")
                .append(String.join(", ", modelAccessor.getFieldsByName().keySet()))
                .append(") VALUES (")
                .append(String.join(", ", Collections.nCopies(modelAccessor.getFields().size(), "?")))
                .append(");");
        final String query = queryBuilder.toString();
        logQuery(query);

        Connection connection = getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        int index = 1;
        for (Map.Entry<Field, ModelTypeAccessor<?>> entry : modelAccessor.getFields().entrySet()) {
            entry.getValue().castSet(preparedStatement, index++, entry.getKey().get(instance));
        }

        final boolean success = preparedStatement.executeUpdate() == 1;
        postRequest(connection, preparedStatement);
        return success;
    }

    @Override
    public <T> boolean insertOrUpdate(ModelAccessor<T> modelAccessor, T instance) throws Exception {
        StringBuilder queryBuilder = new StringBuilder("INSERT INTO ")
                .append(modelAccessor.getTableName()).append(" (")
                .append(String.join(", ", modelAccessor.getFieldsByName().keySet()))
                .append(") VALUES (")
                .append(String.join(", ", Collections.nCopies(modelAccessor.getFields().size(), "?")))
                .append(") ON DUPLICATE KEY UPDATE ")
                .append(modelAccessor.getFieldsByName().keySet().stream()
                        .map(field -> field + " = ?")
                        .collect(Collectors.joining(", "))
                )
                .append(";");
        final String query = queryBuilder.toString();
        logQuery(query);

        Connection connection = getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        int index = 1;
        final int offset = modelAccessor.getFields().size();
        for (Map.Entry<Field, ModelTypeAccessor<?>> entry : modelAccessor.getFields().entrySet()) {
            ModelTypeAccessor<?> typeAccessor = entry.getValue();
            Object value = entry.getKey().get(instance);

            typeAccessor.castSet(preparedStatement, index, value);
            typeAccessor.castSet(preparedStatement, index + offset, value);
            index++;
        }

        final boolean success = preparedStatement.executeUpdate() > 0;
        postRequest(connection, preparedStatement);
        return success;
    }

    @Override
    public <T> boolean update(ModelAccessor<T> modelAccessor, T instance, SelectionQuery selectionQuery) throws Exception {
        StringBuilder queryBuilder = new StringBuilder("UPDATE ")
                .append(modelAccessor.getTableName()).append(" SET ")
                .append(modelAccessor.getFieldsByName().keySet().stream()
                        .map(field -> field + " = ?")
                        .collect(Collectors.joining(", "))
                )
                .append(" WHERE ")
                .append(selectionQuery.getWhere().isEmpty() ? "1" :
                        selectionQuery.getWhere().keySet().stream()
                                .map(field -> field + " = ?")
                                .collect(Collectors.joining(" AND ")))
                .append(";");
        final String query = queryBuilder.toString();
        logQuery(query);

        Connection connection = getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        int index = 1;
        final int offset = modelAccessor.getFields().size();
        for (Map.Entry<Field, ModelTypeAccessor<?>> entry : modelAccessor.getFields().entrySet()) {
            ModelTypeAccessor<?> typeAccessor = entry.getValue();
            Object value = entry.getKey().get(instance);
            typeAccessor.castSet(preparedStatement, index, value);
            index++;
        }

        for (Map.Entry<String, Object> entry : selectionQuery.getWhere().entrySet()) {
            ModelTypeAccessor<?> typeAccessor = modelAccessor.getFields().get(modelAccessor.getFieldsByName().get(entry.getKey()));
            typeAccessor.castSet(preparedStatement, index++, entry.getValue());
        }

        final boolean success = preparedStatement.executeUpdate() > 0;
        postRequest(connection, preparedStatement);
        return success;
    }

    public void postRequest(Connection connection, PreparedStatement preparedStatement) throws SQLException {
        if (preparedStatement != null) {
            preparedStatement.close();
        }
    }

}

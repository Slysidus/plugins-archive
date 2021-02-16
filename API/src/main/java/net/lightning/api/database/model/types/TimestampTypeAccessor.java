package net.lightning.api.database.model.types;

import net.lightning.api.database.model.ModelField;
import net.lightning.api.database.model.ModelTypeAccessor;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class TimestampTypeAccessor extends ModelTypeAccessor<Timestamp> {

    public TimestampTypeAccessor(ModelField options) {
        super("TIMESTAMP", options);
    }

    @Override
    public boolean validate(Object object) {
        return object instanceof Timestamp;
    }

    @Override
    public Timestamp get(ResultSet resultSet, String column) throws SQLException {
        return resultSet.getTimestamp(column);
    }

    @Override
    public void set(PreparedStatement preparedStatement, int index, Timestamp value) throws SQLException {
        preparedStatement.setTimestamp(index, value);
    }

}

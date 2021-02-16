package net.lightning.api.database.model.types;

import net.lightning.api.database.model.ModelField;
import net.lightning.api.database.model.ModelTypeAccessor;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LongTypeAccessor extends ModelTypeAccessor<Long> {

    public LongTypeAccessor(ModelField options) {
        super("BIGINT", options);
    }

    @Override
    public boolean validate(Object object) {
        return object instanceof Long;
    }

    @Override
    public Long get(ResultSet resultSet, String column) throws SQLException {
        return resultSet.getLong(column);
    }

    @Override
    public void set(PreparedStatement preparedStatement, int index, Long value) throws SQLException {
        preparedStatement.setLong(index, value);
    }

}

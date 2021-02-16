package net.lightning.api.database.model.types;

import net.lightning.api.database.model.ModelField;
import net.lightning.api.database.model.ModelTypeAccessor;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class IntTypeAccessor extends ModelTypeAccessor<Integer> {

    public IntTypeAccessor(ModelField options) {
        super("INTEGER", options);
    }

    @Override
    public boolean validate(Object object) {
        return object instanceof Integer;
    }

    @Override
    public Integer get(ResultSet resultSet, String column) throws SQLException {
        return resultSet.getInt(column);
    }

    @Override
    public void set(PreparedStatement preparedStatement, int index, Integer value) throws SQLException {
        preparedStatement.setInt(index, value);
    }

}

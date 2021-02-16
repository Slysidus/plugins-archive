package net.lightning.api.database.model.types;

import net.lightning.api.database.model.ModelField;
import net.lightning.api.database.model.ModelTypeAccessor;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class VarcharTypeAccessor extends ModelTypeAccessor<String> {

    public VarcharTypeAccessor(ModelField options) {
        super("VARCHAR", 255, options);
    }

    @Override
    public boolean validate(Object object) {
        return object instanceof String;
    }

    @Override
    public String get(ResultSet resultSet, String column) throws SQLException {
        return resultSet.getString(column);
    }

    @Override
    public void set(PreparedStatement preparedStatement, int index, String value) throws SQLException {
        preparedStatement.setString(index, value);
    }

}

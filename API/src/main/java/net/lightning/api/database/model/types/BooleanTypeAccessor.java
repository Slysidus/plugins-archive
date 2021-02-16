package net.lightning.api.database.model.types;

import net.lightning.api.database.model.ModelField;
import net.lightning.api.database.model.ModelTypeAccessor;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class BooleanTypeAccessor extends ModelTypeAccessor<Boolean> {

    public BooleanTypeAccessor(ModelField options) {
        super("TINYINT", options);
    }

    @Override
    public boolean validate(Object object) {
        return object instanceof Boolean;
    }

    @Override
    public Boolean get(ResultSet resultSet, String column) throws SQLException {
        return resultSet.getInt(column) == 1;
    }

    @Override
    public void set(PreparedStatement preparedStatement, int index, Boolean value) throws SQLException {
        preparedStatement.setInt(index, value ? 1 : 0);
    }

}

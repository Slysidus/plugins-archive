package net.lightning.api.database.model.types;

import net.lightning.api.database.model.ModelField;
import net.lightning.api.database.model.ModelTypeAccessor;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class UUIDTypeAccessor extends ModelTypeAccessor<UUID> {

    public UUIDTypeAccessor(ModelField options) {
        super("VARCHAR", 36, options);
    }

    @Override
    public boolean validate(Object object) {
        return object instanceof UUID;
    }

    @Override
    public UUID get(ResultSet resultSet, String column) throws SQLException {
        return UUID.fromString(resultSet.getString(column));
    }

    @Override
    public void set(PreparedStatement preparedStatement, int index, UUID value) throws SQLException {
        preparedStatement.setString(index, value.toString());
    }

}

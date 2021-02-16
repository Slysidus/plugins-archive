package net.lightning.common.models.types;

import net.lightning.api.database.model.ModelField;
import net.lightning.api.database.model.ModelTypeAccessor;
import net.lightning.common.SanctionType;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SanctionTypeAccessor extends ModelTypeAccessor<SanctionType> {

    public SanctionTypeAccessor(ModelField options) {
        super("VARCHAR", 10, options);
    }

    @Override
    public boolean validate(Object object) {
        return object instanceof SanctionType;
    }

    @Override
    public SanctionType get(ResultSet resultSet, String column) throws SQLException {
        return SanctionType.valueOf(resultSet.getString(column));
    }

    @Override
    public void set(PreparedStatement preparedStatement, int index, SanctionType value) throws SQLException {
        preparedStatement.setString(index, value.name());
    }

}

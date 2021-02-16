package net.lightning.common.models.types;

import net.lightning.api.database.model.ModelField;
import net.lightning.api.database.model.ModelTypeAccessor;
import net.lightning.common.Rank;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class RankTypeAccessor extends ModelTypeAccessor<Rank> {

    public RankTypeAccessor(ModelField options) {
        super("VARCHAR", 30, options);
    }

    @Override
    public boolean validate(Object object) {
        return object instanceof Rank;
    }

    @Override
    public Rank get(ResultSet resultSet, String column) throws SQLException {
        return Rank.getRank(resultSet.getString(column));
    }

    @Override
    public void set(PreparedStatement preparedStatement, int index, Rank value) throws SQLException {
        preparedStatement.setString(index, value.name());
    }

}

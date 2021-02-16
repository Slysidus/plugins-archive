package net.lightning.api.database.model;

import lombok.Getter;
import net.lightning.api.util.StringUtil;

import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Getter
public abstract class ModelTypeAccessor<T> {

    private final String typeName;
    private final int defaultLength;

    private final ModelField options;
    private String fieldName;

    public ModelTypeAccessor(String typeName, ModelField options) {
        this(typeName, -1, options);
    }

    public ModelTypeAccessor(String typeName, int defaultLength, ModelField options) {
        this.typeName = typeName;
        this.defaultLength = defaultLength;
        this.options = options;
    }

    public void updateFieldName(Field field) {
        this.fieldName = options.fieldName().isEmpty()
                ? StringUtil.decapitalize(field.getName()).replaceAll("([A-Z])", "_$1").toLowerCase()
                : options.fieldName();
    }

    public abstract boolean validate(Object object);

    /*
    SQL Accessors
     */

    public abstract T get(ResultSet resultSet, String column) throws SQLException;

    public abstract void set(PreparedStatement preparedStatement, int index, T value) throws SQLException;

    @SuppressWarnings("unchecked")
    public void castSet(PreparedStatement preparedStatement, int index, Object value) throws SQLException {
        set(preparedStatement, index, (T) value);
    }

}

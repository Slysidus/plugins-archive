package net.lightning.api.database.providers;

import net.lightning.api.database.model.ModelAccessor;
import net.lightning.api.database.query.SelectionQuery;

import java.util.Collection;

public interface DatabaseProvider {

    void login() throws Exception;

    void disconnect() throws Exception;

    /*
    Database operations
     */

    boolean createTable(ModelAccessor<?> modelAccessor, boolean force) throws Exception;

    <T> T get(ModelAccessor<T> modelAccessor, SelectionQuery selectionQuery) throws Exception;

    <T> Collection<T> select(ModelAccessor<T> modelAccessor, SelectionQuery selectionQuery) throws Exception;

    <T> boolean insert(ModelAccessor<T> modelAccessor, T instance) throws Exception;

    <T> boolean insertOrUpdate(ModelAccessor<T> modelAccessor, T instance) throws Exception;

    <T> boolean update(ModelAccessor<T> modelAccessor, T instance, SelectionQuery selectionQuery) throws Exception;

}

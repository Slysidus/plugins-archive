package net.lightning.api.database.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.lightning.api.database.providers.DatabaseProvider;
import net.lightning.api.database.query.SelectionQuery;
import org.redisson.api.RBatch;
import org.redisson.api.RBucket;
import org.redisson.api.RFuture;
import org.redisson.api.RedissonClient;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

@Getter
public class ModelAccessor<T> {

    private final Constructor<T> constructor;

    private final String tableName;

    private final Map<Field, ModelTypeAccessor<?>> fields;

    private final Map<String, Field> fieldsByName;

    public ModelAccessor(Constructor<T> constructor, String tableName, Map<Field, ModelTypeAccessor<?>> fields) {
        this.constructor = constructor;
        this.tableName = tableName;
        this.fields = fields;

        this.fieldsByName = new LinkedHashMap<>();
        for (Map.Entry<Field, ModelTypeAccessor<?>> entry : fields.entrySet()) {
            fieldsByName.put(entry.getValue().getFieldName(), entry.getKey());
        }
    }

    @AllArgsConstructor
    public class AccessorContext {

        private final DatabaseProvider databaseProvider;

        public boolean createTable(boolean force) throws Exception {
            return databaseProvider.createTable(ModelAccessor.this, force);
        }

        public T get(SelectionQuery selectionQuery) throws Exception {
            return databaseProvider.get(ModelAccessor.this, selectionQuery);
        }

        public T get(String where, Object equals) throws Exception {
            return databaseProvider.get(ModelAccessor.this, new SelectionQuery()
                    .where(where, equals));
        }

        public Collection<T> select(SelectionQuery selectionQuery) throws Exception {
            return databaseProvider.select(ModelAccessor.this, selectionQuery);
        }

        public boolean insert(T instance) throws Exception {
            return databaseProvider.insert(ModelAccessor.this, instance);
        }

        public boolean insertOrUpdate(T instance) throws Exception {
            return databaseProvider.insertOrUpdate(ModelAccessor.this, instance);
        }

        public boolean update(T instance, SelectionQuery selectionQuery) throws Exception {
            return databaseProvider.update(ModelAccessor.this, instance, selectionQuery);
        }

        public boolean update(T instance, String where, Object equals) throws Exception {
            return databaseProvider.update(ModelAccessor.this, instance, new SelectionQuery()
                    .where(where, equals));
        }

    }

    @AllArgsConstructor
    public class RedisAccessorContext {

        private final RedissonClient redissonClient;

        public RBucket<T> getBucket(String key) {
            return redissonClient.getBucket(tableName + ":" + key);
        }

        public T get(String key) {
            return getBucket(key).get();
        }

        public T get(Serializable object) {
            return get(object.toString());
        }

        public void put(T value) {
            if (value instanceof CachableModel) {
                set(((CachableModel) value).getCacheKey(), value);
            }
            else {
                throw new IllegalArgumentException("Cannot use put when value is not a CachableModel!");
            }
        }

        public void set(String key, T value) {
            getBucket(key).set(value);
        }

        public void delete(T value) {
            if (value instanceof CachableModel) {
                delete(((CachableModel) value).getCacheKey());
            }
            else {
                throw new IllegalArgumentException("Cannot use delete by value when value is not a CachableModel!");
            }
        }

        public void delete(String key) {
            getBucket(key).delete();
        }

        public void delete(Serializable object) {
            delete(object.toString());
        }

        /*
        Batch
         */

        public RBucket<T> getBucket(RBatch batch, String key) {
            return (RBucket<T>) batch.<T>getBucket(tableName + ":" + key);
        }

        public RFuture<T> get(RBatch batch, String key) {
            return getBucket(batch, key).getAsync();
        }

        public void put(RBatch batch, T value) {
            if (value instanceof CachableModel) {
                set(batch, ((CachableModel) value).getCacheKey(), value);
            }
            else {
                throw new IllegalArgumentException("Cannot use put when value is not a CachableModel!");
            }
        }

        public void set(RBatch batch, String key, T value) {
            getBucket(batch, key).setAsync(value);
        }

        public void delete(RBatch batch, T value) {
            if (value instanceof CachableModel) {
                delete(batch, ((CachableModel) value).getCacheKey());
            }
            else {
                throw new IllegalArgumentException("Cannot use delete by value when value is not a CachableModel!");
            }
        }

        public void delete(RBatch batch, String key) {
            getBucket(batch, key).deleteAsync();
        }

        public void delete(RBatch batch, Serializable object) {
            delete(object.toString());
        }

    }

}

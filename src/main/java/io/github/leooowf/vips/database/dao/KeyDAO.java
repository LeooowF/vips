package io.github.leooowf.vips.database.dao;

import com.henryfabio.sqlprovider.executor.SQLExecutor;
import io.github.leooowf.vips.VipsPlugin;
import io.github.leooowf.vips.database.dao.adapter.KeyAdapter;
import io.github.leooowf.vips.model.Key;

import java.util.Set;

public class KeyDAO {

    private static final String TABLE = "keys_vip";

    public void createTable() {
        this.executor().updateQuery(
                "CREATE TABLE IF NOT EXISTS " + TABLE + "(" +
                        "id CHAR(13) NOT NULL PRIMARY KEY," +
                        "vip VARCHAR(255) NOT NULL," +
                        "generated BIGINT DEFAULT 0," +
                        "end BIGINT DEFAULT 0" +
                        ");"
        );
    }

    public void insertOne(Key key) {
        this.executor().updateQuery(
                "INSERT INTO " + TABLE + " VALUES (?,?,?,?);",
                simpleStatement -> {
                    simpleStatement.set(1, key.getId());
                    simpleStatement.set(2, key.getVip());
                    simpleStatement.set(3, key.getGenerated());
                    simpleStatement.set(4, key.getEnd());
                }
        );
    }

    public void deleteOne(String id) {
        this.executor().updateQuery(
                "DELETE FROM " + TABLE + " WHERE id = ?",
                simpleStatement -> simpleStatement.set(1, id)
        );
    }

    public Set<Key> selectAll() {
        return this.executor().resultManyQuery(
                "SELECT * FROM " + TABLE,
                simpleStatement -> {
                },
                KeyAdapter.class
        );
    }
    private SQLExecutor executor() {
        return new SQLExecutor(VipsPlugin.getInstance().getSqlConnector());
    }
}
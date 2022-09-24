package io.github.leooowf.vips.database.dao.adapter;

import com.henryfabio.sqlprovider.executor.adapter.SQLResultAdapter;
import com.henryfabio.sqlprovider.executor.result.SimpleResultSet;
import io.github.leooowf.vips.model.Key;

public class KeyAdapter implements SQLResultAdapter<Key> {

    @Override
    public Key adaptResult(SimpleResultSet resultSet) {
        return new Key(
                resultSet.get("id"),
                resultSet.get("vip"),
                resultSet.get("generated"),
                resultSet.get("end")
        );
    }
}
/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.janusgraph.diskstorage.rdbms.dao;


import org.janusgraph.diskstorage.StaticBuffer;
import org.janusgraph.diskstorage.rdbms.JanusColumnValue;
import org.janusgraph.diskstorage.rdbms.RdbmsTransaction;
import org.janusgraph.diskstorage.rdbms.entity.JanusColumn;
import org.janusgraph.diskstorage.util.StaticArrayBuffer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * DAO to access Column entities stored in RDBMS
 *
 * @author Madhan Neethiraj &lt;madhan@apache.org&gt;
 */
public class JanusColumnDao extends BaseDao<JanusColumn> {
    public JanusColumnDao(RdbmsTransaction trx) {
        super(trx);
    }

    public void addOrUpdate(long keyId, byte[] name, byte[] val) {
        int updateCount = this.update(keyId, name, val);

        if (updateCount < 1) {
            super.create(new JanusColumn(keyId, name, val));
        }
    }

    public int update(long keyId, byte[] name, byte[] val) {
        int ret = em.createNamedQuery("JanusColumn.updateByKeyIdAndName")
                    .setParameter("keyId", keyId)
                    .setParameter("name", name)
                    .setParameter("val", val)
                    .executeUpdate();

        return ret;
    }

    public int remove(long keyId, byte[] name) {
        int ret = em.createNamedQuery("JanusColumn.deleteByKeyIdAndName")
                    .setParameter("keyId", keyId)
                    .setParameter("name", name)
                    .executeUpdate();

        return ret;
    }

    public List<JanusColumnValue> getColumns(long keyId, byte[] startColumn, byte[] endColumn, int limit) {
        List<Object[]> result = em.createNamedQuery("JanusColumn.getColumnsByKeyIdStartNameEndName")
                                    .setParameter("keyId", keyId)
                                    .setParameter("startName", startColumn)
                                    .setParameter("endName", endColumn)
                                    .setMaxResults(limit)
                                    .getResultList();

        return toColumnList(result);
    }

    public Map<StaticBuffer, List<JanusColumnValue>> getKeysByColumnRange(long storeId, byte[] startColumn, byte[] endColumn, int limit) {
        List<Object[]> result = em.createNamedQuery("JanusColumn.getColumnsByStoreIdColumnRange")
                                  .setParameter("storeId", storeId)
                                  .setParameter("startName", startColumn)
                                  .setParameter("endName", endColumn)
                                  .setMaxResults(limit)
                                  .getResultList();

        return toKeyColumns(result);
    }

    public Map<StaticBuffer, List<JanusColumnValue>> getKeysByKeyAndColumnRange(long storeId, byte[] startKey, byte[] endKey, byte[] startColumn, byte[] endColumn, int limit) {
        List<Object[]> result = em.createNamedQuery("JanusColumn.getColumnsByStoreIdKeyRangeColumnRange")
                                  .setParameter("storeId", storeId)
                                  .setParameter("startKey", startKey)
                                  .setParameter("endKey", endKey)
                                  .setParameter("startName", startColumn)
                                  .setParameter("endName", endColumn)
                                  .setMaxResults(limit)
                                  .getResultList();

        return toKeyColumns(result);
    }

    private List<JanusColumnValue> toColumnList(List<Object[]> result) {
        List<JanusColumnValue> ret = null;

        if (result != null && !result.isEmpty()) {
            ret = new ArrayList<>(result.size());

            for (Object[] row : result) {
                byte[] name = toByteArray(row[0]);
                byte[] val  = toByteArray(row[1]);

                ret.add(new JanusColumnValue(name, val));
            }
        }

        return ret;
    }

    private Map<StaticBuffer, List<JanusColumnValue>> toKeyColumns(List<Object[]> result) {
        Map<StaticBuffer, List<JanusColumnValue>> ret = null;

        if (result != null && !result.isEmpty()) {
            Long                   currKeyId   = null;
            StaticBuffer           currKey     = null;
            List<JanusColumnValue> currColumns = null;

            ret = new HashMap<>();

            for (Object[] row : result) {
                Long keyId = toLong(row[0]);

                if (currKeyId == null || !currKeyId.equals(keyId)) {
                    if (currKey != null && !currColumns.isEmpty()) {
                        ret.put(currKey, currColumns);
                    }

                    currKeyId   = keyId;
                    currKey     = StaticArrayBuffer.of(toByteArray(row[1]));
                    currColumns = new ArrayList<>();
                }

                currColumns.add(new JanusColumnValue(toByteArray(row[2]), toByteArray(row[3])));
            }

            if (currKey != null && !currColumns.isEmpty()) {
                ret.put(currKey, currColumns);
            }
        }

        return ret;
    }
}
/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.janusgraph.diskstorage.rdbms;

import org.janusgraph.diskstorage.BackendException;
import org.janusgraph.diskstorage.BaseTransactionConfig;
import org.janusgraph.diskstorage.StaticBuffer;
import org.janusgraph.diskstorage.StoreMetaData;
import org.janusgraph.diskstorage.common.AbstractStoreManager;
import org.janusgraph.diskstorage.configuration.ConfigNamespace;
import org.janusgraph.diskstorage.configuration.Configuration;
import org.janusgraph.diskstorage.keycolumnvalue.KCVMutation;
import org.janusgraph.diskstorage.keycolumnvalue.KeyColumnValueStoreManager;
import org.janusgraph.diskstorage.keycolumnvalue.KeyColumnValueStore;
import org.janusgraph.diskstorage.keycolumnvalue.KeyRange;
import org.janusgraph.diskstorage.keycolumnvalue.StandardStoreFeatures;
import org.janusgraph.diskstorage.keycolumnvalue.StoreFeatures;
import org.janusgraph.diskstorage.keycolumnvalue.StoreTransaction;
import org.janusgraph.diskstorage.rdbms.dao.DaoManager;
import org.janusgraph.graphdb.configuration.GraphDatabaseConfiguration;
import org.janusgraph.graphdb.configuration.PreInitializeConfigOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Storage Manager for RDBMS
 *
 * @author Madhan Neethiraj &lt;madhan@apache.org&gt;
 */
@PreInitializeConfigOptions
public class RdbmsStoreManager extends AbstractStoreManager implements KeyColumnValueStoreManager {
    private static final Logger LOG = LoggerFactory.getLogger(RdbmsStoreManager.class);

    private static final String NAME = "rdbms";

    public static final ConfigNamespace RDBMS_NS =
        new ConfigNamespace(GraphDatabaseConfiguration.STORAGE_NS, NAME, "RDBMS configuration options");

    public static final ConfigNamespace JPA_CONFIG_NS =
        new ConfigNamespace(RDBMS_NS, "jpa", "JPA configurations", true);

    private final StandardStoreFeatures   features;
    private final Map<String, RdbmsStore> stores;
    private final DaoManager              daoManager;

    public RdbmsStoreManager(Configuration config) {
        super(config);

        features = new StandardStoreFeatures.Builder()
                                                    .orderedScan(true)
                                                    .unorderedScan(true)
                                                    .multiQuery(true)
                                                    .transactional(true)
                                                    .keyConsistent(GraphDatabaseConfiguration.buildGraphConfiguration())
                                                    .keyOrdered(true)
                                                    .batchMutation(true)
                                                    .build();
        stores     = new HashMap<>();
        daoManager = new DaoManager(config.getSubset(JPA_CONFIG_NS));

        LOG.info("RdbmsStoreManager()");
    }

    public DaoManager getDaoManager() { return daoManager; }

    @Override
    public KeyColumnValueStore openDatabase(String name, StoreMetaData.Container container) throws BackendException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("==> RdbmsStoreManager.openDatabase(name={})", name);
        }

        RdbmsStore ret = stores.get(name);

        if (ret == null) {
            synchronized (this) {
                ret = stores.get(name);

                if (ret == null) {
                    ret = new RdbmsStore(name, this);

                    stores.put(name, ret);
                }
            }
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("<== RdbmsStoreManager.openDatabase(name={})", name);
        }

        return ret;
    }

    @Override
    public void mutateMany(Map<String, Map<StaticBuffer, KCVMutation>> map, StoreTransaction trx) throws BackendException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("==> RdbmsStoreManager.mutateMany(storeCount={})", map.size());
        }

        for (Map.Entry<String, Map<StaticBuffer, KCVMutation>> storeEntry : map.entrySet()) {
            String              storeName = storeEntry.getKey();
            KeyColumnValueStore store     = this.openDatabase(storeName);

            for (Map.Entry<StaticBuffer, KCVMutation> keyEntry : storeEntry.getValue().entrySet()) {
                StaticBuffer key      = keyEntry.getKey();
                KCVMutation  mutation = keyEntry.getValue();

                store.mutate(key, mutation.getAdditions(), mutation.getDeletions(), trx);
            }

        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("<== RdbmsStoreManager.mutateMany(storeCount={})", map.size());
        }
    }

    @Override
    public StoreTransaction beginTransaction(BaseTransactionConfig baseTransactionConfig) throws BackendException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("==> RdbmsStoreManager.beginTransaction()");
        }

        StoreTransaction ret = new RdbmsTransaction(baseTransactionConfig, this.daoManager);

        if (LOG.isDebugEnabled()) {
            LOG.debug("<== RdbmsStoreManager.beginTransaction()");
        }

        return ret;
    }

    @Override
    public void close() throws BackendException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("==> RdbmsStoreManager.close()");
        }

        synchronized (this) {
            for (RdbmsStore store : stores.values()) {
                store.close();
            }

            stores.clear();
            daoManager.close();
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("<== RdbmsStoreManager.close()");
        }
    }

    @Override
    public void clearStorage() throws BackendException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean exists() throws BackendException {
        return true;
    }

    @Override
    public StoreFeatures getFeatures() {
        return features;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public List<KeyRange> getLocalKeyPartition() throws BackendException {
        throw new UnsupportedOperationException();
    }
}

package org.nuxeo.transientstore;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.nuxeo.runtime.model.ComponentContext;
import org.nuxeo.runtime.model.ComponentInstance;
import org.nuxeo.runtime.model.DefaultComponent;
import org.nuxeo.transientstore.api.TransientStore;
import org.nuxeo.transientstore.api.TransientStoreConfig;
import org.nuxeo.transientstore.api.TransientStoreService;

public class TransientStorageComponent extends DefaultComponent implements TransientStoreService {

    protected Map<String, TransientStoreConfig> configs = new HashMap<String, TransientStoreConfig>();
    protected Map<String, TransientStore> stores = new HashMap<String, TransientStore>();

    public static final String EP_STORE = "store";

    @Override
    public TransientStore getStore(String name) {
        return stores.get(name);
    }

    @Override
    public TransientStoreConfig getStoreConfig(String name) throws IOException {
        TransientStore store = getStore(name);
        if (store!=null) {
            return store.getConfig();
        }
        return null;
    }

    public TransientStore registerStore(TransientStoreConfig config) {
        TransientStore store = null;
        if (config.isCluster()) {
            store = new ClusterAwareTransientStore(config);
        } else {
            store = new SimpleTransientStore(config);
        }
        stores.put(config.getName(), store);
        return store;
    }

    @Override
    public void registerContribution(Object contribution, String extensionPoint, ComponentInstance contributor) {
        if (EP_STORE.equals(extensionPoint)) {
            TransientStoreConfig config = (TransientStoreConfig) contribution;
            configs.put(config.getName(), config);
        }
    }

    @Override
    public void applicationStarted(ComponentContext context) {
       for (TransientStoreConfig config : configs.values()) {
           registerStore(config);
       }
    }
}

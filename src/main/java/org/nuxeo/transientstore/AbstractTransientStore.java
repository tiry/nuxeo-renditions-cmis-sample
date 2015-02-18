package org.nuxeo.transientstore;

import java.io.IOException;

import org.nuxeo.ecm.core.cache.Cache;
import org.nuxeo.transientstore.api.StorageEntry;
import org.nuxeo.transientstore.api.TransientStore;
import org.nuxeo.transientstore.api.TransientStoreConfig;

public abstract class AbstractTransientStore implements TransientStore {

    protected final TransientStoreConfig config;

    AbstractTransientStore(TransientStoreConfig config) {
        this.config = config;
    }

    protected abstract void incrementStorageSize(StorageEntry entry);

    protected abstract void decrementStorageSize(StorageEntry entry);

    protected abstract int getStorageSizeMB();

    protected abstract Cache getL1Cache();

    protected abstract Cache getL2Cache();

    @Override
    public void put(StorageEntry entry) {


    }

    @Override
    public StorageEntry get(String key) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void remove(String key) throws IOException {
        StorageEntry entry = (StorageEntry)getL1Cache().get(key);
        if (entry==null) {
            entry = (StorageEntry)getL2Cache().get(key);
            getL2Cache().invalidate(key);
        } else {
            getL1Cache().invalidate(key);
        }
        if (entry!=null) {
            entry.beforeRemove();
        }
    }

    @Override
    public void canDelete(String key) throws IOException {
        StorageEntry entry = (StorageEntry)getL1Cache().get(key);
        if (entry!=null) {
            getL1Cache().invalidate(key);
            // XXX if size > target => remove !
            getL2Cache().put(key, entry);
        }
    }

    @Override
    public void removeAll() {
        // TODO Auto-generated method stub

    }

    @Override
    public TransientStoreConfig getConfig() {
        // TODO Auto-generated method stub
        return null;
    }

}

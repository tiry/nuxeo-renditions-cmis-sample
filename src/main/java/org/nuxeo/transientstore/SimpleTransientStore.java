package org.nuxeo.transientstore;

import org.nuxeo.ecm.core.cache.Cache;
import org.nuxeo.transientstore.api.StorageEntry;
import org.nuxeo.transientstore.api.TransientStoreConfig;

public class SimpleTransientStore extends AbstractTransientStore {

    SimpleTransientStore(TransientStoreConfig config) {
        super(config);
    }

    @Override
    protected void incrementStorageSize(StorageEntry entry) {
        // TODO Auto-generated method stub

    }

    @Override
    protected void decrementStorageSize(StorageEntry entry) {
        // TODO Auto-generated method stub

    }

    @Override
    protected int getStorageSizeMB() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    protected Cache getL1Cache() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected Cache getL2Cache() {
        // TODO Auto-generated method stub
        return null;
    }

}
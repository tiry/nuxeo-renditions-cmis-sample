package org.nuxeo.transientstore;

import java.util.concurrent.atomic.AtomicLong;

import org.nuxeo.ecm.core.cache.Cache;
import org.nuxeo.transientstore.api.StorageEntry;
import org.nuxeo.transientstore.api.TransientStoreConfig;

public class SimpleTransientStore extends AbstractTransientStore {

    protected AtomicLong storageSizeKB = new AtomicLong(0);

    SimpleTransientStore(TransientStoreConfig config) {
        super(config);
    }

    @Override
    protected void incrementStorageSize(StorageEntry entry) {
        storageSizeKB.addAndGet(entry.getSizeInKB());
    }

    @Override
    protected void decrementStorageSize(StorageEntry entry) {
        storageSizeKB.addAndGet(-entry.getSizeInKB());
    }

    @Override
    public int getStorageSizeMB() {
        return (int) storageSizeKB.get()/1024;
    }

}

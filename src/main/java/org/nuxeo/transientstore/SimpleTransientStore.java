package org.nuxeo.transientstore;

import java.util.concurrent.atomic.AtomicLong;

import org.nuxeo.transientstore.api.StorageEntry;
import org.nuxeo.transientstore.api.TransientStoreConfig;

public class SimpleTransientStore extends AbstractTransientStore {

    protected AtomicLong storageSize = new AtomicLong(0);

    SimpleTransientStore(TransientStoreConfig config) {
        super(config);
    }

    @Override
    protected void incrementStorageSize(StorageEntry entry) {
        storageSize.addAndGet(entry.getSize());
    }

    @Override
    protected void decrementStorageSize(StorageEntry entry) {
        storageSize.addAndGet(-entry.getSize());
    }

    @Override
    protected long getStorageSize() {
        return (int) storageSize.get();
    }

    @Override
    public int getStorageSizeMB() {
        return (int) getStorageSize()/(1024*1024);
    }

}

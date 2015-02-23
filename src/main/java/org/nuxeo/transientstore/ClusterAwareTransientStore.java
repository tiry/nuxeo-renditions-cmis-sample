package org.nuxeo.transientstore;

import org.nuxeo.transientstore.api.StorageEntry;
import org.nuxeo.transientstore.api.TransientStoreConfig;

public class ClusterAwareTransientStore extends AbstractTransientStore {

    ClusterAwareTransientStore(TransientStoreConfig config) {
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
    protected  long getStorageSize() {
        // TODO Auto-generated method stub
        return 0;
    }

}

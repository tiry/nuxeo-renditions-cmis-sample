package org.nuxeo.transientstore.api;

import java.io.IOException;

public interface TransientStore {

    /**
     * Add a new entry in the Store
     *
     * @param entry
     */
    void put(StorageEntry entry) throws IOException;

    /**
     * Retrieve a new entry inside the Store
     *
     * @param key
     * @return
     */
    StorageEntry get(String key) throws IOException;

    /**
     * Remove an entry from the Store
     *
     * @param key
     */
    void remove(String key) throws IOException;

    /**
     * Informs the Store that the entry can be deleted if TTL or GC parameters requires to do some cleanup
     *
     * @param key
     */
    void canDelete(String key) throws IOException;

    void removeAll() throws IOException;

    TransientStoreConfig getConfig() throws IOException;
}

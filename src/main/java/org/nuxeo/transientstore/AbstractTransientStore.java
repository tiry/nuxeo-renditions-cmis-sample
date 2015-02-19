package org.nuxeo.transientstore;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.nuxeo.common.Environment;
import org.nuxeo.ecm.core.cache.Cache;
import org.nuxeo.ecm.core.cache.CacheDescriptor;
import org.nuxeo.ecm.core.cache.CacheService;
import org.nuxeo.ecm.core.cache.CacheServiceImpl;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.model.impl.ExtensionImpl;
import org.nuxeo.transientstore.api.StorageEntry;
import org.nuxeo.transientstore.api.TransientStore;
import org.nuxeo.transientstore.api.TransientStoreConfig;

public abstract class AbstractTransientStore implements TransientStore {

    protected final TransientStoreConfig config;

    protected File cacheDir;

    protected Cache l1Cache;

    protected Cache l2Cache;

    AbstractTransientStore(TransientStoreConfig config) {
        this.config = config;
        initCaches();
    }

    protected void initCaches() {
        CacheService cs = Framework.getService(CacheService.class);
        if (cs!=null) {
            // register the caches
            //
            // temporary until we have a clean API
            CacheDescriptor l1cd = config.getL1CacheConfig();
            CacheDescriptor l2cd = config.getL2CacheConfig();
            ExtensionImpl ext = new ExtensionImpl();
            ext.setContributions(new Object[]{l1cd, l2cd});
            ((CacheServiceImpl)cs).registerExtension(ext);

            // get caches
            l1Cache = cs.getCache(l1cd.name);
            l2Cache = cs.getCache(l2cd.name);
        }
    }

    protected abstract void incrementStorageSize(StorageEntry entry);

    protected abstract void decrementStorageSize(StorageEntry entry);

    protected abstract int getStorageSizeMB();


    protected Cache getL1Cache() {
        return l1Cache;
    }

    protected Cache getL2Cache() {
        return l2Cache;
    }

    @Override
    public void put(StorageEntry entry) throws IOException {
        if (getStorageSizeMB() < config.getAbsoluteMaxSizeMB()) {
            entry = persistEntry(entry);
            incrementStorageSize(entry);
            getL1Cache().put(entry.getId(), entry);
        } else {
            throw new IOException("Maximum Transient Space Exceeded");
        }
    }

    protected StorageEntry persistEntry(StorageEntry entry) throws IOException {
        entry.persist(getCachingDirectory(entry.getId()));
        return entry;
    }

    @Override
    public StorageEntry get(String key) throws IOException {
        StorageEntry entry = (StorageEntry)getL1Cache().get(key);
        if (entry==null) {
            entry = (StorageEntry)getL2Cache().get(key);
        }
        entry.load(getCachingDirectory(key));
        return entry;
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
            decrementStorageSize(entry);
            entry.beforeRemove();
        }
    }

    @Override
    public void canDelete(String key) throws IOException {
        StorageEntry entry = (StorageEntry)getL1Cache().get(key);
        if (entry!=null) {
            getL1Cache().invalidate(key);
            if (getStorageSizeMB() < config.getTargetMaxSizeMB()) {
                getL2Cache().put(key, entry);
            }
        }
    }

    @Override
    public void removeAll() throws IOException {
        getL1Cache().invalidateAll();
        getL2Cache().invalidateAll();
        doGC();
    }

    @Override
    public TransientStoreConfig getConfig() {
        return config;
    }

    protected File getCachingDirectory(String  key) {
        File cachingDir = new File(getCachingDirectory(), key);
        if (!cachingDir.exists()) {
            cachingDir.mkdir();
        }
        return cachingDir;
    }
    protected File getCachingDirectory() {
        if (cacheDir==null) {
            File data = new File(Environment.getDefault().getData(), config.getName());
            if (data.exists()) {
                try {
                    FileUtils.deleteDirectory(data);
                } catch (IOException cause) {
                    throw new RuntimeException("Cannot create cache dir " + data, cause);
                }
            }
            data.mkdirs();
            return cacheDir = data.getAbsoluteFile();
        }
        return cacheDir;
    }

    protected void doGC() {
        // XXX
    }

}

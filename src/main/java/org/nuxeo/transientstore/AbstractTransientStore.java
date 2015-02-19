package org.nuxeo.transientstore;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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

    protected static final Log log = LogFactory.getLog(AbstractTransientStore.class);

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

    public abstract int getStorageSizeMB();


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

    protected String getCachingDirName(String key) {
        return key;
    }

    protected String getKeyCachingDirName(String dir) {
        return dir;
    }


    protected File getCachingDirectory(String  key) {
        File cachingDir = new File(getCachingDirectory(), getCachingDirName(key));
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

    public void doGC() {
        File dir = getCachingDirectory();
        try {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(dir.getAbsolutePath()))) {
                for (Path entry: stream) {
                    String key = getKeyCachingDirName(entry.getName(-1).toString());
                    try {
                        if (getL1Cache().get(key)!=null) {
                            continue;
                        }
                        if (getL2Cache().get(key)!=null) {
                            continue;
                        }
                        FileUtils.deleteDirectory(entry.toFile());
                    } catch (IOException e) {
                        log.error("Error while performing GC", e);
                    }

                }
            }
        } catch (IOException e) {
            log.error("Error while performing GC", e);
        }

/*
        for (String loc : dir.list()) {
            File entryDir = new File(dir, loc);
            if (entryDir.isDirectory()) {
                String key = getKeyCachingDirName(loc);
                try {
                    if (getL1Cache().get(key)!=null) {
                        continue;
                    }
                    if (getL2Cache().get(key)!=null) {
                        continue;
                    }
                    FileUtils.deleteDirectory(entryDir);
                } catch (IOException e) {
                    log.error("Error while performing GC", e);
                }
            }
        }*/
    }
}

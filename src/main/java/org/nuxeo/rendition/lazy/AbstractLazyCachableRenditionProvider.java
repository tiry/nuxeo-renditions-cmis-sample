package org.nuxeo.rendition.lazy;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.impl.blob.StringBlob;
import org.nuxeo.ecm.core.api.local.ClientLoginModule;
import org.nuxeo.ecm.core.cache.Cache;
import org.nuxeo.ecm.core.cache.CacheService;
import org.nuxeo.ecm.core.work.AbstractWork;
import org.nuxeo.ecm.core.work.api.Work;
import org.nuxeo.ecm.core.work.api.WorkManager;
import org.nuxeo.ecm.platform.rendition.RenditionException;
import org.nuxeo.ecm.platform.rendition.extension.RenditionProvider;
import org.nuxeo.ecm.platform.rendition.service.RenditionDefinition;
import org.nuxeo.runtime.api.Framework;

public abstract class AbstractLazyCachableRenditionProvider implements RenditionProvider {

    public static final String CACHE_NAME ="LazyRenditionCache";

    protected static Log log = LogFactory.getLog(AbstractLazyCachableRenditionProvider.class);

    @Override
    public boolean isAvailable(DocumentModel doc, RenditionDefinition def) {
        return true;
    }

    @Override
    public List<Blob> render(DocumentModel doc, RenditionDefinition def) throws RenditionException {

        // build the key
        String key = buildRenditionKey(doc, def);

        // see if rendition is already in process
        CacheService cs = Framework.getService(CacheService.class);
        Cache cache = cs.getCache(CACHE_NAME);

        if (cache==null) {
            log.error("Unable to find cache " + CACHE_NAME);
            return null;
        }

        CachedRenditionResult cached = null;
        try {
            cached = (CachedRenditionResult) cache.get(key);
        } catch (IOException e) {
           throw new RenditionException("Unable to read from cache", e);
        }

        if (cached==null) {
            Work work = getRenditionWork(key, doc, def);
            WorkManager wm = Framework.getService(WorkManager.class);
            CachedRenditionResult result = new CachedRenditionResult(key);
            result.setWorkerId(work.getId());
            try {
                cache.put(key, result);
            } catch (IOException e) {
                throw new RenditionException("Unable to write to cache", e);
            }
            wm.schedule(work);
        } else {
            if (cached.isCompleted()) {
                return cached.getBlobs();
            }
        }
        // return an empty Blob
        List<Blob> blobs = new ArrayList<Blob>();
        StringBlob emptyBlob = new StringBlob("");
        emptyBlob.setFilename("inprogress");
        emptyBlob.setMimeType("text/plain;empty=true");
        blobs.add(emptyBlob);
        return blobs;
    }

    protected Work getRenditionWork(final String key, final DocumentModel doc, final RenditionDefinition def) {

        return  new AbstractWork() {

            private static final long serialVersionUID = 1L;

            @Override
            public String getId() {
                return "rendition:" + key;
            }

            @Override
            public String getTitle() {
                return "Lazy Rendition for " + def.getName() + " on " + doc.getId();
            }

            @Override
            public void work() {
                List<Blob> blobs = doComputeRendition(initSession(),doc, def);
                CacheService cs = Framework.getService(CacheService.class);
                Cache cache = cs.getCache(CACHE_NAME);
                try {
                    CachedRenditionResult cached = (CachedRenditionResult) cache.get(key);
                    cached.setBlobs(blobs);
                } catch (IOException e) {
                    log.error(e);
                }
            }
        };

    }

    protected abstract List<Blob> doComputeRendition(CoreSession session, DocumentModel doc, RenditionDefinition def);

    protected abstract boolean perUserRendition();

    protected String buildRenditionKey(DocumentModel doc, RenditionDefinition def) {

        StringBuffer sb = new StringBuffer(doc.getId());
        sb.append("::");
        Calendar modif = (Calendar) doc.getPropertyValue("dc:modified");
        if (modif!=null) {
            sb.append(modif.getTimeInMillis());
            sb.append("::");
        }
        if (perUserRendition()) {
            sb.append(doc.getCoreSession().getPrincipal().getName());
            sb.append("::");
        }
        sb.append(def.getName());

        return getDigest(sb.toString());
    }

    protected String getDigest(String key) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            return key;
        }
        byte[] buf = digest.digest(key.getBytes());
        return toHexString(buf);
    }

    private static final char[] HEX_DIGITS = "0123456789abcdef".toCharArray();

    protected String toHexString(byte[] data) {
        StringBuilder buf = new StringBuilder(2 * data.length);
        for (byte b : data) {
            buf.append(HEX_DIGITS[(0xF0 & b) >> 4]);
            buf.append(HEX_DIGITS[0x0F & b]);
        }
        return buf.toString();
    }
}

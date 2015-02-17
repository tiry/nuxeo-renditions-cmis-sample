package org.nuxeo.rendition.lazy;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.nuxeo.common.Environment;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.impl.blob.FileBlob;

public class CachedRenditionResult implements Serializable {

    private static final long serialVersionUID = 1L;

    protected static File cacheDir;

    public static final String CACHING_DIRECTORY = "renditionCache";

    protected String workerId;

    protected final String renditionKey;

    protected List<Map<String, String>> cachedBlobs;

    protected boolean completed = false;

    public CachedRenditionResult(String renditionKey) {
        this.renditionKey=renditionKey;
    }

    public void setBlobs(List<Blob> blobs) throws IOException {
        cachedBlobs = new ArrayList<Map<String,String>>();
        File dir = new File (getCachingDirectory(), renditionKey);
        dir.mkdir();
        dir.deleteOnExit();
        for (Blob blob : blobs) {
            Map<String, String> cached = new HashMap<String, String>();
            File cachedFile = new File (dir,blob.getFilename());
            blob.transferTo(cachedFile);
            cachedFile.deleteOnExit();
            cached.put("file", cachedFile.getAbsolutePath());
            cached.put("filename", blob.getFilename());
            cached.put("encoding", blob.getEncoding());
            cached.put("mimetype", blob.getMimeType());
            cachedBlobs.add(cached);
        }
        completed=true;
    }

    public List<Blob> getBlobs() {
        List<Blob> blobs = new ArrayList<Blob>();
        for (Map<String, String> info : cachedBlobs) {
            File cachedFile = new File (info.get("file"));
            Blob blob = new FileBlob(cachedFile);
            blob.setEncoding(info.get("encoding"));
            blob.setMimeType(info.get("mimetype"));
            blob.setFilename(info.get("filename"));
            blobs.add(blob);
        }
        return blobs;
    }

    protected static File getCachingDirectory() {
        if (cacheDir==null) {
            File data = new File(Environment.getDefault().getData(), CACHING_DIRECTORY);
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

    public String getWorkerId() {
        return workerId;
    }

    public void setWorkerId(String workerId) {
        this.workerId = workerId;
    }

    public boolean isCompleted() {
        return completed;
    }

    // XXX
    // TMP workaround until we have a way to hook inside the cache invalidation system
    @Override
    protected void finalize() throws Throwable {

        try {
            File dir = new File (getCachingDirectory(), renditionKey);
            if (dir.exists()) {
                FileUtils.deleteDirectory(dir);
            }
        } finally {
            super.finalize();
        }
    }



}

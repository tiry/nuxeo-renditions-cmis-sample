package org.nuxeo.transientstore.api;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import org.nuxeo.ecm.core.api.Blob;

public interface StorageEntry extends Serializable {

    String getId();

    void setBlobs(List<Blob> blobs) throws IOException;

    List<Blob> getBlobs();

    void put(String key, Serializable value);

    Serializable get(String key);

    void beforeRemove();
}

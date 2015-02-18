package org.nuxeo.transientstore.api;

import java.io.IOException;

public interface TransientStoreService {

    TransientStore getStore(String name);

    TransientStoreConfig getStoreConfig(String name) throws IOException;

    TransientStore registerStore(TransientStoreConfig config);
}

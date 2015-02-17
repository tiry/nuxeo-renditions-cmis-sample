package org.nuxeo.rendition.cmis.test;

import java.util.ArrayList;
import java.util.List;

import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.impl.blob.StringBlob;
import org.nuxeo.ecm.platform.rendition.service.RenditionDefinition;
import org.nuxeo.rendition.lazy.AbstractLazyCachableRenditionProvider;

public class LazyRenditionProviderSample extends AbstractLazyCachableRenditionProvider {

    @Override
    protected List<Blob> doComputeRendition(CoreSession session, DocumentModel doc, RenditionDefinition def) {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        List<Blob> blobs = new ArrayList<Blob>();
        StringBlob blob = new StringBlob("I am really lazy");
        blob.setFilename("LazyBoy.txt");
        blobs.add(blob);
        return blobs;
    }

}

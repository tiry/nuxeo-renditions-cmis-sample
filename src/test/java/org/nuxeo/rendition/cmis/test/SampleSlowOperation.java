package org.nuxeo.rendition.cmis.test;

import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.impl.blob.StringBlob;
import org.nuxeo.ecm.core.convert.api.ConversionService;

@Operation(id = SampleSlowOperation.ID, category = Constants.CAT_CONVERSION, label = "Do nothing but slowly", description = "Do nothing but slowly !")
public class SampleSlowOperation {

    public static final String ID = "SampleSlow.Operation";

    @Context
    protected ConversionService service;

    @OperationMethod
    public Blob run(DocumentModel doc) throws Exception {

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        StringBlob blob = new StringBlob("I am really lazy");
        blob.setFilename("LazyBoy.txt");

        return blob;
    }

}

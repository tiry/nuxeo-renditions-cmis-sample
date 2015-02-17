package org.nuxeo.rendition.cmis.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.Serializable;
import java.util.List;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.OperationContext;
import org.apache.chemistry.opencmis.client.api.Rendition;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.impl.blob.StringBlob;
import org.nuxeo.ecm.core.event.EventService;
import org.nuxeo.ecm.core.opencmis.impl.CmisFeature;
import org.nuxeo.ecm.core.opencmis.impl.CmisFeatureSessionBrowser;
import org.nuxeo.ecm.platform.rendition.service.RenditionService;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;
import org.nuxeo.runtime.test.runner.LocalDeploy;
import org.nuxeo.runtime.test.runner.RuntimeHarness;
import org.nuxeo.runtime.transaction.TransactionHelper;

import com.google.inject.Inject;

@RunWith(FeaturesRunner.class)
@Features({ CmisFeature.class, CmisFeatureSessionBrowser.class })
@Deploy({ "org.nuxeo.ecm.core.cache", "org.nuxeo.renditons.cmis.sample" })
@LocalDeploy({ "org.nuxeo.renditons.cmis.sample:core-type-contrib.xml",
        "org.nuxeo.renditons.cmis.sample:automation-contrib.xml",
        "org.nuxeo.renditons.cmis.sample:renditions-automation-test-contrib.xml" })
/**
 *
 * @author tiry
 *
 */
public class TestAutomationRenditions {

    @Inject
    RenditionService rs;

    @Inject
    CoreSession coreSession;

    @Inject
    protected RuntimeHarness harness;

    @Inject
    protected EventService eventService;

    @Inject
    protected Session session;

    @Test
    public void verifyAccessAlternateViaRendition() throws Exception {

        // create a Document with 2 Blobs
        DocumentModel doubleBlobDoc = coreSession.createDocumentModel("/", "double", "DocWith2Blobs");
        doubleBlobDoc.setPropertyValue("dc:title", "Double Blob");
        Blob blob1 = new StringBlob("PrimaryContent", "text/plain", "UTF-8", "File1.txt");
        Blob blob2 = new StringBlob("<html>SecondaryContent</html>", "text/html", "UTF-8", "File2.html");
        doubleBlobDoc.setPropertyValue("file:content", (Serializable) blob1);
        doubleBlobDoc.setPropertyValue("alternate:secondaryContent", (Serializable) blob2);
        doubleBlobDoc = coreSession.createDocument(doubleBlobDoc);

        // Ensure we flush and commit before accessing from the CMIS Client
        coreSession.save();
        TransactionHelper.commitOrRollbackTransaction();
        TransactionHelper.startTransaction();

        // configure CMIS context to fetch the Renditions
        OperationContext oc = session.createOperationContext();
        oc.setRenditionFilterString("*");

        // get the Double Blob guy
        CmisObject dBlobDoc = session.getObjectByPath("/double", oc);

        // Check the main content
        ContentStream main = session.getContentStream(session.createObjectId(dBlobDoc.getId()));
        assertEquals("text/plain", main.getMimeType());
        String mainContent = IOUtils.toString(main.getStream());
        assertEquals("PrimaryContent", mainContent);

        // get renditions
        List<Rendition> renditions = dBlobDoc.getRenditions();

        Rendition alternate = null;
        for (Rendition rendition : renditions) {
            if ("nuxeo:rendition:alternate".equals(rendition.getStreamId())) {
                alternate = rendition;
            }
        }
        assertNotNull(alternate);

        // use rendition to access secondary content
        ContentStream alternateCS = alternate.getContentStream();
        assertEquals("text/html", alternateCS.getMimeType());
        String alternateContent = IOUtils.toString(alternateCS.getStream());
        assertEquals("<html>SecondaryContent</html>", alternateContent);

    }

}


package org.nuxeo.rendition.cmis.test;

import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.OperationContext;
import org.apache.chemistry.opencmis.client.api.Rendition;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.event.EventService;
import org.nuxeo.ecm.core.opencmis.impl.CmisFeature;
import org.nuxeo.ecm.core.test.CoreFeature;
import org.nuxeo.ecm.core.test.TransactionalFeature;
import org.nuxeo.ecm.platform.audit.AuditFeature;
import org.nuxeo.ecm.platform.rendition.service.RenditionService;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;
import org.nuxeo.runtime.test.runner.LocalDeploy;
import org.nuxeo.runtime.test.runner.RuntimeHarness;

import com.google.inject.Inject;


@RunWith(FeaturesRunner.class)
@Features(CmisFeature.class)
@Deploy({ "org.nuxeo.ecm.core.cache","org.nuxeo.renditons.cmis.sample"})
@LocalDeploy("org.nuxeo.renditons.cmis.sample:renditions-test-contrib.xml")
public class TestCMISWithLazyRenditions {


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
    public void testRenditions() throws Exception {

        Assert.assertNotNull(rs);
        org.nuxeo.ecm.platform.rendition.Rendition nxrendition = rs.getRendition(coreSession.getRootDocument(), "iamlazy");
        Assert.assertNotNull(nxrendition);

        DocumentModel doc = coreSession.createDocumentModel("/", "test", "File");
        doc.setPropertyValue("dc:title", "File");
        doc = coreSession.createDocument(doc);

        coreSession.save();

        Folder root = session.getRootFolder();
        assertNotNull(root);
        assertNotNull(root.getId());

        session.clear();
        OperationContext oc = session.createOperationContext();
        oc.setRenditionFilterString("*");
        CmisObject rootWithRenditions = session.getObject(session.createObjectId(root.getId()), oc);

        List<Rendition> renditions = rootWithRenditions.getRenditions();
        Assert.assertEquals(1, renditions.size());

        System.out.println(renditions.get(0).getKind());
        System.out.println(renditions.get(0).getStreamId());
        System.out.println(renditions.get(0).getContentStream().getFileName());

        System.out.println(renditions.get(0).getContentStream().getLength());
        Assert.assertEquals("nuxeo:rendition:iamlazy", renditions.get(0).getStreamId());
        Assert.assertEquals(-1, renditions.get(0).getLength());

        String content = IOUtils.toString(renditions.get(0).getContentStream().getStream());
        Assert.assertEquals(0, content.length());

        eventService.waitForAsyncCompletion(2000);

        session.clear();
        rootWithRenditions = session.getObject(session.createObjectId(root.getId()), oc);
        renditions = rootWithRenditions.getRenditions();
        Assert.assertEquals(1, renditions.size());

        System.out.println(renditions.get(0).getKind());
        System.out.println(renditions.get(0).getStreamId());
        System.out.println(renditions.get(0).getContentStream().getFileName());
        System.out.println(renditions.get(0).getContentStream().getLength());

        ContentStream stream = renditions.get(0).getContentStream();

        content = IOUtils.toString(stream.getStream());
        Assert.assertTrue(content.length()>0);
        Assert.assertEquals("I am really lazy", content);


    }

}

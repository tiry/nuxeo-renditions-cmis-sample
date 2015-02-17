
package org.nuxeo.rendition.cmis.test;

import java.util.List;

import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.OperationContext;
import org.apache.chemistry.opencmis.client.api.Rendition;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.event.EventService;
import org.nuxeo.ecm.core.opencmis.impl.CmisFeature;
import org.nuxeo.ecm.platform.rendition.service.RenditionService;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;
import org.nuxeo.runtime.test.runner.LocalDeploy;
import org.nuxeo.runtime.test.runner.RuntimeHarness;

import com.google.inject.Inject;


/**
 *
 *
 *
 * @author <a href="mailto:tdelprat@nuxeo.com">Tiry</a>
 *
 */
@RunWith(FeaturesRunner.class)
@Features(CmisFeature.class)
@Deploy({ "org.nuxeo.ecm.core.cache","org.nuxeo.renditons.cmis.sample"})
@LocalDeploy("org.nuxeo.renditons.cmis.sample:renditions-test-contrib.xml")
public class TestRenditionWithCMIS {


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
    public void verifyAccessToLazyRenditions() throws Exception {

        // configure CMIS context to fetch the Renditions
        OperationContext oc = session.createOperationContext();
        oc.setRenditionFilterString("*");

        // get the root Document
        Folder rootWithRenditions = session.getRootFolder(oc);

        // get renditions
        List<Rendition> renditions = rootWithRenditions.getRenditions();
        Assert.assertEquals(1, renditions.size());

        // try to access rendition and verify that it is not yet available
        Assert.assertEquals("nuxeo:rendition:iamlazy", renditions.get(0).getStreamId());
        ContentStream stream = renditions.get(0).getContentStream();
        Assert.assertTrue(renditions.get(0).getContentStream().getMimeType().contains("empty=true"));

        String content = IOUtils.toString(stream.getStream());
        Assert.assertEquals(0, content.length());

        // wait for async processing on the Nuxeo side
        eventService.waitForAsyncCompletion(2000);

        // fetch again the rendition stream
        stream = renditions.get(0).getContentStream();
        // check that is no lo,ger flagged as empty
        Assert.assertFalse(renditions.get(0).getContentStream().getMimeType().contains("empty=true"));

        // verify the content of the rendition stream
        content = IOUtils.toString(stream.getStream());
        Assert.assertTrue(content.length()>0);
        Assert.assertEquals("I am really lazy", content);

    }

}

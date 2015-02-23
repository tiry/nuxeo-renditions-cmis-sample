
package org.nuxeo.rendition.cmis.test;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.event.EventService;
import org.nuxeo.ecm.platform.rendition.Rendition;
import org.nuxeo.ecm.platform.rendition.service.RenditionService;
import org.nuxeo.ecm.platform.test.PlatformFeature;
import org.nuxeo.rendition.lazy.CachedRenditionResult;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;
import org.nuxeo.runtime.test.runner.LocalDeploy;

import com.google.inject.Inject;

@Deploy({ "org.nuxeo.ecm.core.cache","org.nuxeo.ecm.platform.rendition.api", "org.nuxeo.ecm.platform.rendition.core","org.nuxeo.renditons.cmis.sample"})
@RunWith(FeaturesRunner.class)
@Features(PlatformFeature.class)
@LocalDeploy("org.nuxeo.renditons.cmis.sample:renditions-test-contrib.xml")
/**
 *
 * @author tiry
 *
 */
public class TestLazyRenditions {

    @Inject
    RenditionService rs;

    @Inject
    CoreSession session;

    @AfterClass
    public static void cleanup() throws Exception {
        CachedRenditionResult.resetCache();
    }

    @Test
    public void testRenditions() throws Exception {

        Assert.assertNotNull(rs);

        Rendition rendition = rs.getRendition(session.getRootDocument(), "iamlazy");

        Assert.assertNotNull(rendition);

        Blob blob = rendition.getBlob();

        Assert.assertEquals(0, blob.getLength());

        Thread.sleep(1000);

        Framework.getService(EventService.class).waitForAsyncCompletion(5000);

        rendition = rs.getRendition(session.getRootDocument(), "iamlazy");

        blob = rendition.getBlob();

        String data = IOUtils.toString(blob.getStream());

        System.out.println(data);

        Assert.assertNotEquals(0, blob.getLength());


    }

}

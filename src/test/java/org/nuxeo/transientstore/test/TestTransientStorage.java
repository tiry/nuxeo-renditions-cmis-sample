package org.nuxeo.transientstore.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.core.opencmis.impl.CmisFeature;
import org.nuxeo.ecm.core.opencmis.impl.CmisFeatureSessionBrowser;
import org.nuxeo.ecm.core.test.CoreFeature;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;
import org.nuxeo.runtime.test.runner.LocalDeploy;
import org.nuxeo.transientstore.api.TransientStore;
import org.nuxeo.transientstore.api.TransientStoreService;



@RunWith(FeaturesRunner.class)
@Features({ CoreFeature.class })
@Deploy({ "org.nuxeo.ecm.core.cache", "org.nuxeo.renditons.cmis.sample" })
@LocalDeploy({ "org.nuxeo.renditons.cmis.sample:transientstore-contrib.xml"})
public class TestTransientStorage {

    @Test
    public void verifyAccessAlternateViaRendition() throws Exception {

        TransientStoreService tss = Framework.getService(TransientStoreService.class);
        assertNotNull(tss);

        TransientStore ts = tss.getStore("testStore");
        assertNotNull(ts);

    }



}

/*
 * (C) Copyright 2015 Nuxeo SAS (http://nuxeo.com/) and contributors.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * Contributors:
 * Nuxeo - initial API and implementation
 */

package org.nuxeo.rendition.cmis.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.OperationContext;
import org.apache.chemistry.opencmis.client.api.Rendition;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.commons.io.IOUtils;
import org.junit.AfterClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.event.EventService;
import org.nuxeo.ecm.core.opencmis.impl.CmisFeature;
import org.nuxeo.ecm.core.opencmis.impl.CmisFeatureSessionBrowser;
import org.nuxeo.ecm.core.transientstore.api.TransientStoreService;
import org.nuxeo.ecm.platform.rendition.service.RenditionService;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;
import org.nuxeo.runtime.test.runner.LocalDeploy;
import org.nuxeo.runtime.test.runner.RuntimeHarness;

import com.google.inject.Inject;

@RunWith(FeaturesRunner.class)
@Features({ CmisFeature.class, CmisFeatureSessionBrowser.class })
@Deploy({ "org.nuxeo.ecm.core.cache", "org.nuxeo.renditons.cmis.sample" })
@LocalDeploy("org.nuxeo.renditons.cmis.sample:renditions-test-contrib.xml")
/**
 * Access Lazy Rendition via CMIS
 *
 * @author <a href="mailto:tdelprat@nuxeo.com">Tiry</a>
 */
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

    @AfterClass
    public static void cleanup() throws Exception {
        Framework.getService(TransientStoreService.class).getStore("LazyRenditionCache").removeAll();
    }

    @Test
    public void verifyAccessToLazyRenditions() throws Exception {

        // configure CMIS context to fetch the Renditions
        OperationContext oc = session.createOperationContext();
        oc.setRenditionFilterString("*");

        // get the root Document
        Folder rootWithRenditions = session.getRootFolder(oc);

        // get renditions
        List<Rendition> renditions = rootWithRenditions.getRenditions();
        assertEquals(4, renditions.size());

        // try to access rendition and verify that it is not yet available
        assertEquals("nuxeo:rendition:iamlazy", renditions.get(3).getStreamId());
        ContentStream stream = renditions.get(3).getContentStream();
        assertTrue(renditions.get(3).getContentStream().getMimeType().contains("empty=true"));

        String content = IOUtils.toString(stream.getStream());
        assertEquals(0, content.length());

        // wait for async processing on the Nuxeo side
        eventService.waitForAsyncCompletion(2000);

        // fetch again the rendition stream
        stream = renditions.get(3).getContentStream();
        // check that is no lo,ger flagged as empty
        assertFalse(renditions.get(3).getContentStream().getMimeType().contains("empty=true"));

        // verify the content of the rendition stream
        content = IOUtils.toString(stream.getStream());
        assertTrue(content.length() > 0);
        assertEquals("I am really lazy", content);

    }

}

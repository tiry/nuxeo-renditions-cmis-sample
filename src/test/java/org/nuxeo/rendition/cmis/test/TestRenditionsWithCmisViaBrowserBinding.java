
package org.nuxeo.rendition.cmis.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite.SuiteClasses;
import org.nuxeo.ecm.core.opencmis.impl.CmisFeatureSessionBrowser;
import org.nuxeo.runtime.test.runner.ContributableFeaturesRunner;
import org.nuxeo.runtime.test.runner.Features;

/**
 * Run tests via Browser binding.
 *
 * @author <a href="mailto:tdelprat@nuxeo.com">Tiry</a>
 *
 */
@RunWith(ContributableFeaturesRunner.class)
@SuiteClasses(TestRenditionWithCMIS.class)
@Features(CmisFeatureSessionBrowser.class)
public class TestRenditionsWithCmisViaBrowserBinding {

}

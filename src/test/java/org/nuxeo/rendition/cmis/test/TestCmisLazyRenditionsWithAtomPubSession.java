/*
 * Copyright (c) 2006-2014 Nuxeo SA (http://nuxeo.com/) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Florent Guillaume
 */
package org.nuxeo.rendition.cmis.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite.SuiteClasses;
import org.nuxeo.ecm.core.opencmis.impl.CmisFeatureSessionAtomPub;
import org.nuxeo.runtime.test.runner.ContributableFeaturesRunner;
import org.nuxeo.runtime.test.runner.Features;

/**
 * Test the high-level session using AtomPub.
 */
@RunWith(ContributableFeaturesRunner.class)
@SuiteClasses(TestCMISWithLazyRenditions.class)
@Features(CmisFeatureSessionAtomPub.class)
public class TestCmisLazyRenditionsWithAtomPubSession {

}

package org.nuxeo.transientstore.api;

import java.io.IOException;

public class MaximumTransientSpaceExceeded extends IOException {

    private static final long serialVersionUID = 1L;

    public MaximumTransientSpaceExceeded() {
        super("Maximum Transient Space Exceeded");
    }

}

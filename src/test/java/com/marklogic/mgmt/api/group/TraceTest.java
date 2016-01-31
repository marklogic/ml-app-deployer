package com.marklogic.mgmt.api.group;

import org.junit.Test;

import com.marklogic.mgmt.api.AbstractApiTest;

public class TraceTest extends AbstractApiTest {

    /**
     * Tests configuring trace events on the default group via trace/untrace.
     */
    @Test
    public void traceAndUntrace() {
        final String event = "api-test";

        Group g = api.group();
        assertTrue(g.getEvent() == null || !g.getEvent().contains(event));

        api.trace(event);

        g = api.group();
        assertTrue(g.getEvent().contains(event));

        api.untrace(event);

        g = api.group();
        assertTrue(g.getEvent() == null || !g.getEvent().contains(event));
    }
}

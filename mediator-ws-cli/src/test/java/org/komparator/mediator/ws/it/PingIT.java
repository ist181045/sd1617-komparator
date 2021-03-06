package org.komparator.mediator.ws.it;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;


/**
 * Test suite
 */
public class PingIT extends BaseIT {

    private static final String INVALID_ARG = "Invalid ping argument!";

    @Test
    public void pingEmptyTest() {
        assertNotNull(mediatorClient.ping("test"));
    }

    @Test
    public void pingNullArg() {
        assertEquals(INVALID_ARG, mediatorClient.ping(null));
    }

    @Test
    public void pingEmptyArg() {
        assertEquals(INVALID_ARG, mediatorClient.ping(""));
    }

    @Test
    public void pingArgWithWhitespace() {
        assertEquals(INVALID_ARG, mediatorClient.ping("   "));
    }

    @Test
    public void pingArgWithHoriTab() {
        assertEquals(INVALID_ARG, mediatorClient.ping("\t"));
    }

    @Test
    public void pingArgWithNewline() {
        assertEquals(INVALID_ARG, mediatorClient.ping("\n"));
    }

    @Test
    public void pingArgWithCarriageReturn() {
        assertEquals(INVALID_ARG, mediatorClient.ping("\r"));
    }

    @Test
    public void pingWithMixedWhitespace() {
        assertEquals(INVALID_ARG, mediatorClient.ping("test\n\r\t test"));
    }

    @Test
    public void pingReplyFromMultipleSuppliers() {
        // Requires 2 suppliers running
        String response = mediatorClient.ping("test");

        assertEquals(2, response.split(System.lineSeparator()).length);
    }

    @Test
    public void pingAssertResponse() {
        // Thank you white-box testing (2 suppliers running)
        String response = mediatorClient.ping("test");
        String[] responses = response.split(System.lineSeparator());

        for (String res : responses) {
            assertNotNull(res);

            assertTrue(res.startsWith("Response from "));
            assertTrue(res.contains("test"));
        }
    }
}

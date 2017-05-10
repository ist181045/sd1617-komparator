package org.komparator.mediator.ws.it;

import java.util.Arrays;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import org.komparator.mediator.ws.EmptyCart_Exception;
import org.komparator.mediator.ws.InvalidCartId;
import org.komparator.mediator.ws.InvalidCartId_Exception;
import org.komparator.mediator.ws.InvalidCreditCard;
import org.komparator.mediator.ws.InvalidCreditCard_Exception;

@RunWith(Parameterized.class)
public class BuyCartArgsIT extends BaseIT {

    private static final String CC_NUMBER = "4012888888881881";
    private static final String CART_ID = "CID#000@2017-04-07T11:39:50.231";

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Parameter(0)
    public String cartId;

    @Parameter(1)
    public String creditCardNr;

    @Parameter(2) /* Unused */
    public String testName;

    @Parameters(name = "{2}")
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][] {
            { null,           CC_NUMBER, "Cart ID: Null"             },
            { "",             CC_NUMBER, "Cart ID: Empty String"     },
            { "  ",           CC_NUMBER, "Cart ID: Spaces"           },
            { "\n",           CC_NUMBER, "Cart ID: Linefeed"         },
            { "\t",           CC_NUMBER, "Cart ID: Horizontal Tab"   },
            { "\r",           CC_NUMBER, "Cart ID: Carriage Return"  },
            { "abc\r\n\tabc", CC_NUMBER, "Cart ID: Mixed Whitespace" },

            { CART_ID,           null, "CC Number: Null"              },
            { CART_ID,             "", "CC Number: Empty String"      },
            { CART_ID,           "  ", "CC Number: Spaces"            },
            { CART_ID,           "\n", "CC Number: Linefeed"          },
            { CART_ID,           "\t", "CC Number: Horizontal Tab"    },
            { CART_ID,           "\r", "CC Number: Carriage Return"   },
            { CART_ID, "abc\r\n\tabc", "CC Number: Mixed Whitespace"  },
            { CART_ID, "123456789012", "CC Number: Invalid Number"    },
            { CART_ID,      "4012888", "CC Number: Incomplete Number" },

            { CART_ID,      CC_NUMBER, "Cart ID: Doesn't Exist" }
        });
    }

    @Test
    public void testArgs() throws Throwable {
        if (creditCardNr == CC_NUMBER) { // It's OK! Don't panic
            thrown.expect(InvalidCartId_Exception.class);
        } else {
            thrown.expect(InvalidCreditCard_Exception.class);
        }

        mediatorClient.buyCart(cartId, creditCardNr);
    }
}

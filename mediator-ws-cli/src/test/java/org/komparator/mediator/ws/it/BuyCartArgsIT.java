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
import org.komparator.mediator.ws.InvalidCartId_Exception;
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

    @Parameter(2)
    public Class<Throwable> exception;

    @Parameter(3) /* Unused */
    public String testName;

    @Parameters(name = "{3}")
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][] {
            { null,           CC_NUMBER, InvalidCartId_Exception.class, "Cart ID: Null"             },
            { "",             CC_NUMBER, InvalidCartId_Exception.class, "Cart ID: Empty String"     },
            { "  ",           CC_NUMBER, InvalidCartId_Exception.class, "Cart ID: Spaces"           },
            { "\n",           CC_NUMBER, InvalidCartId_Exception.class, "Cart ID: Linefeed"         },
            { "\t",           CC_NUMBER, InvalidCartId_Exception.class, "Cart ID: Horizontal Tab"   },
            { "\r",           CC_NUMBER, InvalidCartId_Exception.class, "Cart ID: Carriage Return"  },
            { "abc\r\n\tabc", CC_NUMBER, InvalidCartId_Exception.class, "Cart ID: Mixed Whitespace" },

            { CART_ID,           null, InvalidCreditCard_Exception.class, "CC Number: Null"              },
            { CART_ID,             "", InvalidCreditCard_Exception.class, "CC Number: Empty String"      },
            { CART_ID,           "  ", InvalidCreditCard_Exception.class, "CC Number: Spaces"            },
            { CART_ID,           "\n", InvalidCreditCard_Exception.class, "CC Number: Linefeed"          },
            { CART_ID,           "\t", InvalidCreditCard_Exception.class, "CC Number: Horizontal Tab"    },
            { CART_ID,           "\r", InvalidCreditCard_Exception.class, "CC Number: Carriage Return"   },
            { CART_ID, "abc\r\n\tabc", InvalidCreditCard_Exception.class, "CC Number: Mixed Whitespace"  },
            { CART_ID, "123456789012", InvalidCreditCard_Exception.class, "CC Number: Invalid Number"    },
            { CART_ID,      "4012888", InvalidCreditCard_Exception.class, "CC Number: Incomplete Number" },

            { CART_ID,      CC_NUMBER, InvalidCartId_Exception.class, "Cart: Doesnt Exist" }
        });
    }

    @Test
    public void testArgs() throws InvalidCreditCard_Exception,
            EmptyCart_Exception, InvalidCartId_Exception {
        thrown.expect(exception);
        mediatorClient.buyCart(cartId, creditCardNr);
    }
}

package org.komparator.mediator.ws.it;

import java.util.Arrays;

import org.junit.AfterClass;
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
    public ExpectedException expectedException = ExpectedException.none();

    @Parameter
    public String cartId;

    @Parameter(value = 1)
    public String creditCardNr;

    @Parameter(value = 2)
    public Class<Throwable> exception;

    @Parameters
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {null,           CC_NUMBER, InvalidCartId_Exception.class},
                {"",             CC_NUMBER, InvalidCartId_Exception.class},
                {"  ",           CC_NUMBER, InvalidCartId_Exception.class},
                {"\n",           CC_NUMBER, InvalidCartId_Exception.class},
                {"\t",           CC_NUMBER, InvalidCartId_Exception.class},
                {"\r",           CC_NUMBER, InvalidCartId_Exception.class},
                {"abc\r\n\tabc", CC_NUMBER, InvalidCartId_Exception.class},

                {CART_ID,           null, InvalidCreditCard_Exception.class},
                {CART_ID,             "", InvalidCreditCard_Exception.class},
                {CART_ID,           "  ", InvalidCreditCard_Exception.class},
                {CART_ID,           "\n", InvalidCreditCard_Exception.class},
                {CART_ID,           "\t", InvalidCreditCard_Exception.class},
                {CART_ID,           "\r", InvalidCreditCard_Exception.class},
                {CART_ID, "abc\r\n\tabc", InvalidCreditCard_Exception.class},
                {CART_ID, "1234abcd9012", InvalidCreditCard_Exception.class},
                {CART_ID, "123456789012", InvalidCreditCard_Exception.class}
        });
    }

    @Test
    public void testArgs() throws InvalidCreditCard_Exception,
            EmptyCart_Exception, InvalidCartId_Exception {
        expectedException.expect(exception);

        mediatorClient.buyCart(cartId, creditCardNr);
    }
}

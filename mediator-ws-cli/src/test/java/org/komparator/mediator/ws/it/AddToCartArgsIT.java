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
import org.komparator.mediator.ws.InvalidItemId_Exception;
import org.komparator.mediator.ws.InvalidQuantity;
import org.komparator.mediator.ws.InvalidQuantity_Exception;
import org.komparator.mediator.ws.ItemIdView;
import org.komparator.mediator.ws.NotEnoughItems_Exception;

@RunWith(Parameterized.class)
public class AddToCartArgsIT extends BaseIT {

    private static final String CART_ID = "CID#000@2017-04-07T11:39:50.231";
    private static final String PID = "Product";
    private static final String SID = "Supplier";

    private static final ItemIdView IIV = newIIV(PID, SID);

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Parameter(0)
    public String cartId;

    @Parameter(1)
    public ItemIdView itemIdView;

    @Parameter(2)
    public int quantity;

    @Parameter(3)
    public Class<Throwable> exception;

    @Parameter(4) /* Unused */
    public String testName;

    @Parameters(name = "{4}")
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][] {
            // Cart ID tests
            {    null,  IIV, 1, InvalidCartId_Exception.class, "Cart ID: Null ID"  },
            {      "",  IIV, 1, InvalidCartId_Exception.class, "Cart ID: Empty ID" },
            {     " ",  IIV, 1, InvalidCartId_Exception.class, "Cart ID: Blank ID" },
            {    "\t",  IIV, 1, InvalidCartId_Exception.class, "Cart ID: HT in ID" },
            {    "\n",  IIV, 1, InvalidCartId_Exception.class, "Cart ID: LF in ID" },

            // ItemIdView tests
            { CART_ID,                   null, 1, InvalidItemId_Exception.class, "Item: Null item" },
            // Product ID
            { CART_ID, newIIV(null, SID), 1, InvalidItemId_Exception.class, "Item: Null pid"  },
            { CART_ID,   newIIV("", SID), 1, InvalidItemId_Exception.class, "Item: Empty pid" },
            { CART_ID,  newIIV(" ", SID), 1, InvalidItemId_Exception.class, "Item: Blank pid" },
            { CART_ID, newIIV("\t", SID), 1, InvalidItemId_Exception.class, "Item: HT in pid" },
            { CART_ID, newIIV("\n", SID), 1, InvalidItemId_Exception.class, "Item: LF in pid" },
            // Supplier ID
            { CART_ID, newIIV(PID, null), 1, InvalidItemId_Exception.class, "Item: Null sid"  },
            { CART_ID,   newIIV(PID, ""), 1, InvalidItemId_Exception.class, "Item: Empty sid" },
            { CART_ID,  newIIV(PID, " "), 1, InvalidItemId_Exception.class, "Item: Blank sid" },
            { CART_ID, newIIV(PID, "\t"), 1, InvalidItemId_Exception.class, "Item: HT in sid" },
            { CART_ID, newIIV(PID, "\n"), 1, InvalidItemId_Exception.class, "Item: LF in sid" },

            // Quantity tests
            { CART_ID, IIV, -1, InvalidQuantity_Exception.class, "Quantity: Negative" },
            { CART_ID, IIV,  0, InvalidQuantity_Exception.class, "Quantity: Zero"     }
        });
    }

    @Test
    public void testArgs()
            throws InvalidCreditCard_Exception, EmptyCart_Exception,
            InvalidCartId_Exception, InvalidQuantity_Exception,
            NotEnoughItems_Exception, InvalidItemId_Exception {
        thrown.expect(exception);
        mediatorClient.addToCart(cartId, itemIdView, quantity);
    }

    private static ItemIdView newIIV(String pid, String sid) {
        ItemIdView iiv = new ItemIdView();
        iiv.setProductId(pid);
        iiv.setSupplierId(sid);
        return iiv;
    }
}

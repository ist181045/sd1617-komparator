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

    @Parameter(3) /* Unused */
    public String testName;

    @Parameters(name = "{3}")
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][] {
            // Cart ID tests
            {    null,  IIV, 1, "Cart ID: Null ID"  },
            {      "",  IIV, 1, "Cart ID: Empty ID" },
            {     " ",  IIV, 1, "Cart ID: Blank ID" },
            {    "\t",  IIV, 1, "Cart ID: HT in ID" },
            {    "\n",  IIV, 1, "Cart ID: LF in ID" },

            // ItemIdView tests
            { CART_ID,                   null, 1, "Item: Null item" },
            // Product ID
            { CART_ID, newIIV(null, SID), 1, "Item: Null pid"  },
            { CART_ID,   newIIV("", SID), 1, "Item: Empty pid" },
            { CART_ID,  newIIV(" ", SID), 1, "Item: Blank pid" },
            { CART_ID, newIIV("\t", SID), 1, "Item: HT in pid" },
            { CART_ID, newIIV("\n", SID), 1, "Item: LF in pid" },
            // Supplier ID
            { CART_ID, newIIV(PID, null), 1, "Item: Null sid"  },
            { CART_ID,   newIIV(PID, ""), 1, "Item: Empty sid" },
            { CART_ID,  newIIV(PID, " "), 1, "Item: Blank sid" },
            { CART_ID, newIIV(PID, "\t"), 1, "Item: HT in sid" },
            { CART_ID, newIIV(PID, "\n"), 1, "Item: LF in sid" },

            // Quantity tests
            { CART_ID, IIV, -1, "Quantity: Negative" },
            { CART_ID, IIV,  0, "Quantity: Zero"     }
        });
    }

    @Test
    public void testArgs() throws Throwable {
        if (cartId == CART_ID) { // It's OK! Don't panic
            if (itemIdView == IIV) {
                thrown.expect(InvalidQuantity_Exception.class);
            } else {
                thrown.expect(InvalidItemId_Exception.class);
            }
        } else {
            thrown.expect(InvalidCartId_Exception.class);
        }

        mediatorClient.addToCart(cartId, itemIdView, quantity);
    }

    private static ItemIdView newIIV(String pid, String sid) {
        ItemIdView iiv = new ItemIdView();
        iiv.setProductId(pid);
        iiv.setSupplierId(sid);
        return iiv;
    }
}

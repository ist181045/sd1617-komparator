package org.komparator.mediator.ws.it;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import org.komparator.mediator.ws.CartItemView;
import org.komparator.mediator.ws.EmptyCart_Exception;
import org.komparator.mediator.ws.InvalidCartId_Exception;
import org.komparator.mediator.ws.InvalidCreditCard_Exception;
import org.komparator.mediator.ws.InvalidItemId_Exception;
import org.komparator.mediator.ws.InvalidQuantity_Exception;
import org.komparator.mediator.ws.ItemIdView;
import org.komparator.mediator.ws.NotEnoughItems_Exception;
import org.komparator.mediator.ws.Result;
import org.komparator.mediator.ws.ShoppingResultView;
import org.komparator.supplier.ws.BadProductId_Exception;
import org.komparator.supplier.ws.BadProduct_Exception;
import org.komparator.supplier.ws.BadQuantity_Exception;
import org.komparator.supplier.ws.InsufficientQuantity_Exception;
import org.komparator.supplier.ws.ProductView;
import org.komparator.supplier.ws.cli.SupplierClient;
import org.komparator.supplier.ws.cli.SupplierClientException;

public class BuyCartIT extends BaseIT {

    /* Supplier1's product 1 */
    private static final String SC1_P1_ID       = "I1202";
    private static final String SC1_P1_DESC     = "Fancy product description";
    private static final int    SC1_P1_PRICE    = 10;
    private static final int    SC1_P1_QUANTITY = 20;

    /* Supplier1's product 2 */
    private static final String SC1_P2_ID       = "I1420";
    private static final String SC1_P2_DESC     = "Zero quantity";
    private static final int    SC1_P2_PRICE    = 10;
    private static final int    SC1_P2_QUANTITY = 1;

    /* Supplier1's product 3 */
    private static final String SC1_P3_ID       = "I1203";
    private static final String SC1_P3_DESC     = "Fancy description";
    private static final int    SC1_P3_PRICE    = 10;
    private static final int    SC1_P3_QUANTITY = 1;

    /* Supplier2's product 1 */
    private static final String SC2_P1_ID       = "I1202";
    private static final String SC2_P1_DESC     = "Fancy";
    private static final int    SC2_P1_PRICE    = 10;
    private static final int    SC2_P1_QUANTITY = 20;

    /* Suppliers */
    private static SupplierClient sc1;
    private static SupplierClient sc2;


    /* Cart information */
    private static final String CART_ID   = "CID#000@2017-04-07T11:39:50.231";
    private static final String CC_NUMBER = "4012888888881881";

    @BeforeClass
    public static void oneTimeSetup() throws Exception {
        BaseIT.oneTimeSetup();

        String uddiURL = testProps.getProperty("uddi.url");
        try {
            sc1 = new SupplierClient(uddiURL, "A58_Supplier1");
            sc2 = new SupplierClient(uddiURL, "A58_Supplier2");
        } catch (SupplierClientException e) {
            fail(e.getMessage());
        }

    }

    @Before
    public void setUp() {
        try {
            sc1.createProduct(newProductView(SC1_P1_ID, SC1_P1_DESC,
                    SC1_P1_PRICE, SC1_P1_QUANTITY));

            sc1.createProduct(newProductView(SC1_P2_ID, SC1_P2_DESC,
                    SC1_P2_PRICE, SC1_P2_QUANTITY));

            sc1.createProduct(newProductView(SC1_P3_ID, SC1_P3_DESC,
                    SC1_P3_PRICE, SC1_P3_QUANTITY));

            sc2.createProduct(newProductView(SC2_P1_ID, SC2_P1_DESC,
                    SC2_P1_PRICE, SC2_P1_QUANTITY));
        } catch (BadProductId_Exception | BadProduct_Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void successFullPurchase() {
        try {
            String sid1 = sc1.getWsName();
            String sid2 = sc2.getWsName();

            mediatorClient.addToCart(CART_ID,
                    newItemIdView(SC1_P1_ID, sid1), SC1_P1_QUANTITY);

            mediatorClient.addToCart(CART_ID,
                    newItemIdView(SC1_P2_ID, sid1), SC1_P2_QUANTITY);

            mediatorClient.addToCart(CART_ID,
                    newItemIdView(SC1_P3_ID, sid1), SC1_P3_QUANTITY);

            mediatorClient.addToCart(CART_ID,
                    newItemIdView(SC2_P1_ID, sid2), SC2_P1_QUANTITY);

            ShoppingResultView srv = mediatorClient.buyCart(CART_ID, CC_NUMBER);

            assertNotNull(srv);
            assertNotNull(srv.getId());

            assertEquals(Result.COMPLETE, srv.getResult());
            assertEquals(4, srv.getPurchasedItems().size());
            assertEquals(0, srv.getDroppedItems().size());

            int price = SC1_P1_PRICE * SC1_P1_QUANTITY
                    + SC1_P2_PRICE * SC1_P2_QUANTITY
                    + SC1_P3_PRICE * SC1_P3_QUANTITY
                    + SC2_P1_PRICE * SC2_P1_QUANTITY;

            assertEquals(price, srv.getTotalPrice());

            List<CartItemView> purchased = srv.getPurchasedItems();
            for (CartItemView civ : purchased) {
                String pid = civ.getItem().getItemId().getProductId();
                String sid = civ.getItem().getItemId().getSupplierId();

                if (sid.equals(sid1))
                    assertEquals(0, sc1.getProduct(pid).getQuantity());
                else if (sid.equals(sid2))
                    assertEquals(0, sc2.getProduct(pid).getQuantity());


            }
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void emptyPurchase() {
        try {
            mediatorClient.addToCart(CART_ID,
                    newItemIdView(SC1_P1_ID, sc1.getWsName()), SC1_P1_QUANTITY);

            sc1.buyProduct(SC1_P1_ID, 1);

            ShoppingResultView srv = mediatorClient.buyCart(CART_ID, CC_NUMBER);

            assertNotNull(srv);

            assertEquals(Result.EMPTY, srv.getResult());
            assertEquals(0, srv.getPurchasedItems().size());
            assertEquals(1, srv.getDroppedItems().size());
            assertEquals(0, srv.getTotalPrice());
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void partialPurchase() {
        try {
            mediatorClient.addToCart(CART_ID,
                    newItemIdView(SC1_P1_ID, sc1.getWsName()), SC1_P1_QUANTITY);

            mediatorClient.addToCart(CART_ID,
                    newItemIdView(SC1_P2_ID, sc1.getWsName()), SC1_P2_QUANTITY);

            sc1.buyProduct(SC1_P2_ID, 1);

            ShoppingResultView srv = mediatorClient.buyCart(CART_ID, CC_NUMBER);

            assertNotNull(srv);

            assertEquals(Result.PARTIAL, srv.getResult());
            assertEquals(1, srv.getPurchasedItems().size());
            assertEquals(1, srv.getDroppedItems().size());
            assertEquals(SC1_P1_PRICE * SC1_P1_QUANTITY, srv.getTotalPrice());
        } catch (Exception e) {
            fail(e.getMessage());
        }

    }

    @After
    public void tearDown() {
        mediatorClient.clear();
    }

    /* Helper methods */

    private ProductView newProductView(String productId, String descText,
            int price, int quantity) {
        ProductView pv = new ProductView();

        pv.setId(productId);
        pv.setDesc(descText);
        pv.setPrice(price);
        pv.setQuantity(quantity);

        return pv;
    }

    private ItemIdView newItemIdView(String productId, String supplierId) {
        ItemIdView iiv = new ItemIdView();

        iiv.setProductId(productId);
        iiv.setSupplierId(supplierId);

        return iiv;
    }
}

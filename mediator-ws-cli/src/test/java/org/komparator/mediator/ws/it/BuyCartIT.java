package org.komparator.mediator.ws.it;

import static org.junit.Assert.fail;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import org.komparator.mediator.ws.EmptyCart_Exception;
import org.komparator.mediator.ws.InvalidCartId_Exception;
import org.komparator.mediator.ws.InvalidCreditCard_Exception;
import org.komparator.supplier.ws.BadProductId_Exception;
import org.komparator.supplier.ws.BadProduct_Exception;
import org.komparator.supplier.ws.ProductView;
import org.komparator.supplier.ws.cli.SupplierClient;
import org.komparator.supplier.ws.cli.SupplierClientException;

public class BuyCartIT extends BaseIT {

    private static final String CC_NUMBER = "4012888888881881";
    private static final String CART_ID = "CID#000@2017-04-07T11:39:50.231";

    private static SupplierClient sc1;
    private static SupplierClient sc2;

    @BeforeClass
    public static void oneTimeSetup() throws Exception {
        BaseIT.oneTimeSetup();
        try {
            sc1 = new SupplierClient(testProps.getProperty("uddi.url"), "A58_Supplier1");
            sc2 = new SupplierClient(testProps.getProperty("uddi.url"), "A58_Supplier2");

            // clear remote service state before all tests
            mediatorClient.clear();

            try {
                {
                    ProductView product = new ProductView();
                    product.setId("I1202");
                    product.setDesc("Fancy product description");
                    product.setPrice(10);
                    product.setQuantity(20);
                    sc1.createProduct(product);
                }
                {
                    ProductView product = new ProductView();
                    product.setId("I1420");
                    product.setDesc("Zero Quantity");
                    product.setPrice(10);
                    product.setQuantity(1);
                    sc1.createProduct(product);
                }
                {
                    ProductView product = new ProductView();
                    product.setId("I1203");
                    product.setDesc("Fancy description");
                    product.setPrice(10);
                    product.setQuantity(20);
                    sc1.createProduct(product);
                }
                {
                    ProductView product = new ProductView();
                    product.setId("I1202");
                    product.setDesc("Fancy");
                    product.setPrice(10);
                    product.setQuantity(20);
                    sc2.createProduct(product);
                }

            } catch (BadProductId_Exception | BadProduct_Exception e) {
                fail(e.getMessage());
            }

        } catch (SupplierClientException e) {
            fail(e.getMessage());
        }
    }

    // testing cartId argument

    @Test(expected = InvalidCartId_Exception.class)
    public void nullCartId() throws InvalidCreditCard_Exception,
            EmptyCart_Exception, InvalidCartId_Exception {
        mediatorClient.buyCart(null, CC_NUMBER);
    }

    @Test(expected = InvalidCartId_Exception.class)
    public void emptyCartId() throws InvalidCreditCard_Exception,
            EmptyCart_Exception, InvalidCartId_Exception {
        mediatorClient.buyCart("", CC_NUMBER);
    }

    @Test(expected = InvalidCartId_Exception.class)
    public void blankCartId() throws InvalidCreditCard_Exception,
            EmptyCart_Exception, InvalidCartId_Exception {
        mediatorClient.buyCart("  ", CC_NUMBER);
    }

    @Test(expected = InvalidCartId_Exception.class)
    public void newlineInCartId() throws InvalidCreditCard_Exception,
            EmptyCart_Exception, InvalidCartId_Exception {
        mediatorClient.buyCart("\n", CC_NUMBER);
    }

    @Test(expected = InvalidCartId_Exception.class)
    public void horiTabInCartId() throws InvalidCreditCard_Exception,
            EmptyCart_Exception, InvalidCartId_Exception {
        mediatorClient.buyCart("\t", CC_NUMBER);
    }

    @Test(expected = InvalidCartId_Exception.class)
    public void carriageReturnInCartId() throws InvalidCreditCard_Exception,
            EmptyCart_Exception, InvalidCartId_Exception {
        mediatorClient.buyCart("\r", CC_NUMBER);
    }

    @Test(expected = InvalidCartId_Exception.class)
    public void mixedWhitespaceInCartId() throws InvalidCreditCard_Exception,
            EmptyCart_Exception, InvalidCartId_Exception {
        mediatorClient.buyCart("id\r\n\tdi", CC_NUMBER);
    }


    // testing creditCardNr argument

    @Test(expected = InvalidCreditCard_Exception.class)
    public void nullCreditCardNr() throws InvalidCreditCard_Exception,
            EmptyCart_Exception, InvalidCartId_Exception {
        mediatorClient.buyCart(CART_ID, null);
    }

    @Test(expected = InvalidCreditCard_Exception.class)
    public void emptyCreditCardNr() throws InvalidCreditCard_Exception,
            EmptyCart_Exception, InvalidCartId_Exception {
        mediatorClient.buyCart(CART_ID, "");
    }

    @Test(expected = InvalidCreditCard_Exception.class)
    public void blankCreditCardNr() throws InvalidCreditCard_Exception,
            EmptyCart_Exception, InvalidCartId_Exception {
        mediatorClient.buyCart(CART_ID, "  ");
    }

    @Test(expected = InvalidCreditCard_Exception.class)
    public void newlineInCreditCardNr() throws InvalidCreditCard_Exception,
            EmptyCart_Exception, InvalidCartId_Exception {
        mediatorClient.buyCart(CART_ID, "\n");
    }

    @Test(expected = InvalidCreditCard_Exception.class)
    public void horiTabInCreditCardNr() throws InvalidCreditCard_Exception,
            EmptyCart_Exception, InvalidCartId_Exception {
        mediatorClient.buyCart(CART_ID, "\t");
    }

    @Test(expected = InvalidCreditCard_Exception.class)
    public void carriageReturnInCreditCardNr()
            throws InvalidCreditCard_Exception, EmptyCart_Exception,
            InvalidCartId_Exception {
        mediatorClient.buyCart(CART_ID, "\r");
    }

    @Test(expected = InvalidCreditCard_Exception.class)
    public void mixedWhitespaceInCreditCardNr()
            throws InvalidCreditCard_Exception, EmptyCart_Exception,
            InvalidCartId_Exception {
        mediatorClient.buyCart(CART_ID, "098123\r\n\t091283");
    }

    @Test(expected = InvalidCreditCard_Exception.class)
    public void creditCardNrNotAnInteger()
            throws InvalidCreditCard_Exception, EmptyCart_Exception,
            InvalidCartId_Exception {
        mediatorClient.buyCart(CART_ID, "987abcABC0");
    }


    // TODO: other functionality related tests


    @AfterClass
    public static void cleanup() {
        mediatorClient.clear();
        BaseIT.cleanup();
    }
}

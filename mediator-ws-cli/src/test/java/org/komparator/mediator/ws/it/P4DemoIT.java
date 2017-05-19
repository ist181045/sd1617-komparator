package org.komparator.mediator.ws.it;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.komparator.mediator.ws.CartView;
import org.komparator.mediator.ws.InvalidCartId_Exception;
import org.komparator.mediator.ws.InvalidItemId_Exception;
import org.komparator.mediator.ws.InvalidQuantity_Exception;
import org.komparator.mediator.ws.ItemIdView;
import org.komparator.mediator.ws.NotEnoughItems_Exception;
import org.komparator.security.SecurityManager;
import org.komparator.supplier.ws.BadProductId_Exception;
import org.komparator.supplier.ws.BadProduct_Exception;
import org.komparator.supplier.ws.ProductView;
import org.komparator.supplier.ws.cli.SupplierClient;
import org.komparator.supplier.ws.cli.SupplierClientException;


/**
 * Test suite
 */
public class P4DemoIT extends BaseIT {

	// static members
	private final static String CART_ID = "CartId";
	
	private static SupplierClient sc1;
	private static SupplierClient sc2;

	// Populate
	@Before
	public void setUp() {
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
					//This product will only be used in ZeroQuantityTest
					//Warning: it will be changed
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
				System.err.println("Empty suppliers: " + e.getMessage());
				fail();
			}
		} catch (SupplierClientException e) {
				System.err.println("Couldn't create supplier clients: "
						+ e.getMessage());
				fail();
		}
	}

	@After
	public void tearDown() {
		
	}
	
	@Test 
	public void P4Demo() {
		SecurityManager.getInstance().setSender("MediatorClient");
		try {
			mediatorClient.addToCart(CART_ID, newIIV("I1202", sc1.getWsName()), 20);
		} catch (InvalidCartId_Exception | InvalidItemId_Exception | InvalidQuantity_Exception
				| NotEnoughItems_Exception e) {
			fail("Can't perform test" + e.getMessage());
		}
		
		System.out.println("You can now kill Primary Mediator. Press enter to continue");

		while(mediatorClient.getWsUrl().equals("http://localhost:8071/mediator-ws/endpoint") ){
			try {
				Thread.sleep(3000);
				mediatorClient.ping("Kill first mediator");
				
			} catch (InterruptedException e) {
				fail("Can't perform test" + e.getMessage());
			}
		}
				
		
		
		List<CartView> cartList = mediatorClient.listCarts();
		assertEquals(1, cartList.size());
		
	}
	
	private ItemIdView newIIV(String pid, String sid) {
		ItemIdView iiv = new ItemIdView();
		iiv.setProductId(pid);
		iiv.setSupplierId(sid);
		return iiv;
	}

    
}

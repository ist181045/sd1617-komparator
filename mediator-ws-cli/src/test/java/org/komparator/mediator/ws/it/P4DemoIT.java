package org.komparator.mediator.ws.it;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.komparator.mediator.ws.CartItemView;
import org.komparator.mediator.ws.CartView;
import org.komparator.mediator.ws.EmptyCart_Exception;
import org.komparator.mediator.ws.InvalidCartId_Exception;
import org.komparator.mediator.ws.InvalidCreditCard_Exception;
import org.komparator.mediator.ws.InvalidItemId_Exception;
import org.komparator.mediator.ws.InvalidQuantity_Exception;
import org.komparator.mediator.ws.ItemIdView;
import org.komparator.mediator.ws.ItemView;
import org.komparator.mediator.ws.NotEnoughItems_Exception;
import org.komparator.mediator.ws.ShoppingResultView;
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
	private final static String CART_ID2 = "CartId2";
	private final static String CC_NUMBER = "4012888888881881";
	
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
	public void R1() {
		SecurityManager.getInstance().setSender("MediatorClient");
		try {
			mediatorClient.addToCart(CART_ID, newIIV("I1202", sc1.getWsName()), 20);
			mediatorClient.addToCart(CART_ID, newIIV("I1202", sc2.getWsName()), 20);
			mediatorClient.addToCart(CART_ID2, newIIV("I1420", sc1.getWsName()), 1);
			mediatorClient.buyCart(CART_ID2, CC_NUMBER);
		} catch (InvalidCartId_Exception | InvalidItemId_Exception | InvalidQuantity_Exception
				| NotEnoughItems_Exception | EmptyCart_Exception | InvalidCreditCard_Exception e) {
			fail("Can't perform test" + e.getMessage());
		}
		
		
		List<CartView> cartList = mediatorClient.listCarts();
		assertNotNull(cartList);
		assertEquals(1, cartList.size());
		
		assertEquals(CART_ID, cartList.get(0).getCartId());
		
		List<ShoppingResultView> srv = mediatorClient.shopHistory();
		
		assertNotNull(srv);
		assertEquals(1, srv.size());
		
		assertEquals("I1420", srv.get(0).getPurchasedItems().get(0).getItem().getItemId().getProductId());
		assertEquals(sc1.getWsName(), srv.get(0).getPurchasedItems().get(0).getItem().getItemId().getSupplierId());
		
	}
	
	@Test 
	public void R2() {
		SecurityManager.getInstance().setSender("MediatorClient");
		try {
			mediatorClient.addToCart(CART_ID, newIIV("I1202", sc1.getWsName()), 20);
			mediatorClient.addToCart(CART_ID, newIIV("I1202", sc2.getWsName()), 20);
			mediatorClient.addToCart(CART_ID2, newIIV("I1420", sc1.getWsName()), 1);
			mediatorClient.buyCart(CART_ID2, CC_NUMBER);
		} catch (InvalidCartId_Exception | InvalidItemId_Exception | InvalidQuantity_Exception
				| NotEnoughItems_Exception | EmptyCart_Exception | InvalidCreditCard_Exception e) {
			fail("Can't perform test" + e.getMessage());
		}

		while(mediatorClient.getWsUrl().equals("http://localhost:8071/mediator-ws/endpoint") ){
			try {
				System.out.printf("You can now kill Primary Mediator%n%n");
				Thread.sleep(3000);
				mediatorClient.ping("Kill first mediator");
				
			} catch (InterruptedException e) {
				fail("Can't perform test" + e.getMessage());
			}
		}
				
		
		
		List<CartView> cartList = mediatorClient.listCarts();
		assertNotNull(cartList);
		assertEquals(1, cartList.size());

		System.out.println();
		cartList.forEach(cv -> {
			System.out.println("--- Cart " + cv.getCartId() + " ---");
			cv.getItems().forEach(civ -> {
				ItemView iv = civ.getItem();
				ItemIdView iiv = iv.getItemId();
				System.out.println("Product ID: " + iiv.getProductId());
				System.out.println("- Price: " + iv.getPrice());
				System.out.println("- Quantity: " + civ.getQuantity());
				System.out.println("- Supplier: " + iiv.getSupplierId());
				System.out.println("- Desc: " + iv.getDesc());
			});
		});
		System.out.println();

		assertEquals(CART_ID, cartList.get(0).getCartId());
		
		List<ShoppingResultView> history = mediatorClient.shopHistory();
		
		assertNotNull(history);
		assertEquals(1, history.size());

		System.out.println();
		history.forEach(srv -> {
			System.out.println("--- Shopping Result " + srv.getId() + " ---");
			System.out.println("- Dropped Items:");
			srv.getDroppedItems().forEach(di -> {
				System.out.println("  * ID: "
						+ di.getItem().getItemId().getProductId());
				System.out.println("  * Quantity: " + di.getQuantity());
			});

			System.out.println("- Purchased Items:");
			srv.getPurchasedItems().forEach(pi -> {
				System.out.println("  * ID: "
						+ pi.getItem().getItemId().getProductId());
				System.out.println("  * Quantity: " + pi.getQuantity());
			});

			System.out.println("- Total Price: " + srv.getTotalPrice());
			System.out.println("- Result: " + srv.getResult());
		});
		System.out.println();
		
		assertEquals("I1420", history.get(0).getPurchasedItems().get(0).getItem().getItemId().getProductId());
		assertEquals(sc1.getWsName(), history.get(0).getPurchasedItems().get(0).getItem().getItemId().getSupplierId());
		
	}
	
	private ItemIdView newIIV(String pid, String sid) {
		ItemIdView iiv = new ItemIdView();
		iiv.setProductId(pid);
		iiv.setSupplierId(sid);
		return iiv;
	}

    
}

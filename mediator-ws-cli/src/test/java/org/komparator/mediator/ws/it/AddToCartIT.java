package org.komparator.mediator.ws.it;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.komparator.mediator.ws.CartItemView;
import org.komparator.mediator.ws.InvalidCartId_Exception;
import org.komparator.mediator.ws.InvalidItemId_Exception;
import org.komparator.mediator.ws.InvalidQuantity_Exception;
import org.komparator.mediator.ws.ItemIdView;
import org.komparator.mediator.ws.ItemView;
import org.komparator.mediator.ws.NotEnoughItems_Exception;
import org.komparator.supplier.ws.BadProductId_Exception;
import org.komparator.supplier.ws.BadProduct_Exception;
import org.komparator.supplier.ws.ProductView;
import org.komparator.supplier.ws.cli.SupplierClient;
import org.komparator.supplier.ws.cli.SupplierClientException;

public class AddToCartIT extends BaseIT {
	
	// static members
	private final static String VALID_CARTID = "CartId";
	private final static String VALID_PRODUCTID = "ProductID";
	private final static String VALID_SUPPLIERID = "SupplierID";
	private final static int VALID_QUANTITY = 1;
	
	private final static ItemIdView VALID_ITEMID_VIEW = new ItemIdView();
	private static ItemIdView INVALID_ITEMID_PRODUCT_VIEW = new ItemIdView();
	private static ItemIdView INVALID_ITEMID_SUPPLIER_VIEW = new ItemIdView();
	
	private static SupplierClient sc1;
	private static SupplierClient sc2;
	
	// one-time initialization and clean-up
	@BeforeClass
	public static void oneTimeSetUp() /*throws BadProductId_Exception, BadProduct_Exception*/ {
		// clear remote service state before all tests
		mediatorClient.clear();
		VALID_ITEMID_VIEW.setProductId(VALID_PRODUCTID);
		VALID_ITEMID_VIEW.setSupplierId(VALID_SUPPLIERID);
		
		INVALID_ITEMID_PRODUCT_VIEW.setSupplierId(VALID_SUPPLIERID);
		INVALID_ITEMID_SUPPLIER_VIEW.setProductId(VALID_PRODUCTID);

		
	}

	@AfterClass
	public static void oneTimeTearDown() {
		
		
		
	}

	// members

	// initialization and clean-up for each test
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
				//All tests will fail cause Suppliers are empty
				e.printStackTrace();
			}
			
		} catch (SupplierClientException e) {
			//Couldn't create SupplierClients. All tests will fail
			e.printStackTrace();
		}
		
		
	}

	@After
	public void tearDown() {
	}

	
	// bad input tests
		
		//invalid cartID

	@Test (expected = InvalidCartId_Exception.class)
	public void nullCartID() throws InvalidCartId_Exception, InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
		mediatorClient.addToCart(null, VALID_ITEMID_VIEW, VALID_QUANTITY);
	}
	
	@Test (expected = InvalidCartId_Exception.class)
	public void emptyCartID() throws InvalidCartId_Exception, InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
		mediatorClient.addToCart("", VALID_ITEMID_VIEW, VALID_QUANTITY);
	}
	
	@Test (expected = InvalidCartId_Exception.class)
	public void whitespaceCartID() throws InvalidCartId_Exception, InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
		mediatorClient.addToCart(" ", VALID_ITEMID_VIEW, VALID_QUANTITY);
	}
	
	@Test (expected = InvalidCartId_Exception.class)
	public void tabCartID() throws InvalidCartId_Exception, InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
		mediatorClient.addToCart("\t", VALID_ITEMID_VIEW, VALID_QUANTITY);
	}
	
	@Test (expected = InvalidCartId_Exception.class)
	public void newLineCartID() throws InvalidCartId_Exception, InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
		mediatorClient.addToCart("\n", VALID_ITEMID_VIEW, VALID_QUANTITY);
	}
	
		//Invalid ItemId
			//BadProductId

	@Test (expected = InvalidItemId_Exception.class)
	public void nullProductId() throws InvalidCartId_Exception, InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
		INVALID_ITEMID_PRODUCT_VIEW.setProductId(null);
		mediatorClient.addToCart(VALID_CARTID, INVALID_ITEMID_PRODUCT_VIEW, VALID_QUANTITY);
	}
	
	@Test (expected = InvalidItemId_Exception.class)
	public void emptyProductId() throws InvalidCartId_Exception, InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
		INVALID_ITEMID_PRODUCT_VIEW.setProductId("");
		mediatorClient.addToCart(VALID_CARTID, INVALID_ITEMID_PRODUCT_VIEW, VALID_QUANTITY);
	}
	
	@Test (expected = InvalidItemId_Exception.class)
	public void whitespaceProductId() throws InvalidCartId_Exception, InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
		INVALID_ITEMID_PRODUCT_VIEW.setProductId(" ");
		mediatorClient.addToCart(VALID_CARTID, INVALID_ITEMID_PRODUCT_VIEW, VALID_QUANTITY);
	}
	
	@Test (expected = InvalidItemId_Exception.class)
	public void tabProductId() throws InvalidCartId_Exception, InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
		INVALID_ITEMID_PRODUCT_VIEW.setProductId("\t");
		mediatorClient.addToCart(VALID_CARTID, INVALID_ITEMID_PRODUCT_VIEW, VALID_QUANTITY);
	}
	
	@Test (expected = InvalidItemId_Exception.class)
	public void newLineProductId() throws InvalidCartId_Exception, InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
		INVALID_ITEMID_PRODUCT_VIEW.setProductId("\n");
		mediatorClient.addToCart(VALID_CARTID, INVALID_ITEMID_PRODUCT_VIEW, VALID_QUANTITY);
	}
	
			//Bad SupplierID
	
	@Test (expected = InvalidItemId_Exception.class)
	public void nullSupplierId() throws InvalidCartId_Exception, InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
		INVALID_ITEMID_SUPPLIER_VIEW.setSupplierId(null);
		mediatorClient.addToCart(VALID_CARTID, INVALID_ITEMID_SUPPLIER_VIEW, VALID_QUANTITY);
	}
	
	@Test (expected = InvalidItemId_Exception.class)
	public void emptySupplierId() throws InvalidCartId_Exception, InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
		INVALID_ITEMID_SUPPLIER_VIEW.setSupplierId("");
		mediatorClient.addToCart(VALID_CARTID, INVALID_ITEMID_SUPPLIER_VIEW, VALID_QUANTITY);
	}
	
	@Test (expected = InvalidItemId_Exception.class)
	public void whitespaceSupplierId() throws InvalidCartId_Exception, InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
		INVALID_ITEMID_SUPPLIER_VIEW.setSupplierId(" ");
		mediatorClient.addToCart(VALID_CARTID, INVALID_ITEMID_SUPPLIER_VIEW, VALID_QUANTITY);
	}
	
	@Test (expected = InvalidItemId_Exception.class)
	public void tabSupplierId() throws InvalidCartId_Exception, InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
		INVALID_ITEMID_SUPPLIER_VIEW.setSupplierId("\t");
		mediatorClient.addToCart(VALID_CARTID, INVALID_ITEMID_SUPPLIER_VIEW, VALID_QUANTITY);
	}
	
	@Test (expected = InvalidItemId_Exception.class)
	public void newLineSupplierId() throws InvalidCartId_Exception, InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
		INVALID_ITEMID_SUPPLIER_VIEW.setSupplierId("\n");
		mediatorClient.addToCart(VALID_CARTID, INVALID_ITEMID_SUPPLIER_VIEW, VALID_QUANTITY);
	}
	
		//Invalid Quantity
	
	@Test (expected = InvalidQuantity_Exception.class)
	public void zeroQuantity() throws InvalidCartId_Exception, InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
		mediatorClient.addToCart(VALID_CARTID, VALID_ITEMID_VIEW, 0);
	}
	
	@Test (expected = InvalidQuantity_Exception.class)
	public void negativeQuantity() throws InvalidCartId_Exception, InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
		mediatorClient.addToCart(VALID_CARTID, VALID_ITEMID_VIEW, -420);
	}
	
	//Main tests
	
		//Not enough items
	@Test
	public void NotEnoughItems() throws InvalidCartId_Exception, InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
		ItemIdView iiv = new ItemIdView();
		iiv.setProductId("I1202");
		iiv.setSupplierId(sc1.getWsName());

		try {
			mediatorClient.addToCart(VALID_CARTID, iiv, 1000);
			fail();
		} catch (NotEnoughItems_Exception nie) {
			assertEquals(0, mediatorClient.listCarts().size());
		}
	}
	
	@Test
	public void NotEnoughItemsByOne() throws InvalidCartId_Exception, InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
		ItemIdView iiv = new ItemIdView();
		iiv.setProductId("I1202");
		iiv.setSupplierId(sc1.getWsName());
		assertEquals(0, mediatorClient.listCarts().size());
		
		try {
			mediatorClient.addToCart(VALID_CARTID, iiv, 21);
			fail();
		} catch (NotEnoughItems_Exception nie) {
			assertEquals(0, mediatorClient.listCarts().size());
		}
	}
	
	//Test if newCart is added and if is possible to get add products with the exact quantity that supplier has
	
	@Test
	public void newCart() throws InvalidCartId_Exception, InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
		ItemIdView iiv = new ItemIdView();
		
		iiv.setProductId("I1202");
		iiv.setSupplierId(sc1.getWsName());
		
		assertEquals(0, mediatorClient.listCarts().size());
		
		mediatorClient.addToCart(VALID_CARTID, iiv, 20);
		
		assertEquals(1, mediatorClient.listCarts().size());
		
		assertEquals(VALID_CARTID, mediatorClient.listCarts().get(0).getCartId());
		
		assertEquals(1, mediatorClient.listCarts().get(0).getItems().size());
		
		CartItemView civ1 = mediatorClient.listCarts().get(0).getItems().get(0);
		
		assertEquals(sc1.getWsName(), civ1.getItem().getItemId().getSupplierId());
		assertEquals("Fancy product description",civ1.getItem().getDesc());

		assertEquals(20,civ1.getQuantity());
		
		assertEquals(10, civ1.getItem().getPrice());
		
		assertEquals("I1202", civ1.getItem().getItemId().getProductId());
		
		
	}
	
	@Test
	public void sucessMultipleSuppliers() throws InvalidCartId_Exception, InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
		
		ItemIdView iiv = new ItemIdView();
		ItemIdView iiv2 = new ItemIdView();
		
		iiv.setProductId("I1202");
		iiv.setSupplierId(sc1.getWsName());
		
		assertEquals(0, mediatorClient.listCarts().size());
		
		mediatorClient.addToCart(VALID_CARTID, iiv, 20);
		
		assertEquals(1, mediatorClient.listCarts().size());
		
		iiv2.setProductId("I1202");
		iiv2.setSupplierId(sc2.getWsName());
		
		mediatorClient.addToCart(VALID_CARTID, iiv2, 20);
		
		assertEquals(1, mediatorClient.listCarts().size());
		
		assertEquals(VALID_CARTID, mediatorClient.listCarts().get(0).getCartId());
		
		assertEquals(2, mediatorClient.listCarts().get(0).getItems().size());
		
		CartItemView civ1 = mediatorClient.listCarts().get(0).getItems().get(0);
		CartItemView civ2 = mediatorClient.listCarts().get(0).getItems().get(1);
		
		if (civ1.getItem().getItemId().getSupplierId().equals(sc1.getWsName())) {
			assertEquals(sc2.getWsName(), civ2.getItem().getItemId().getSupplierId());
			assertEquals("Fancy product description",civ1.getItem().getDesc());
			assertEquals("Fancy",civ2.getItem().getDesc());

		}
		else if (civ2.getItem().getItemId().getSupplierId().equals(sc2.getWsName())){
			assertEquals(sc1.getWsName(), civ2.getItem().getItemId().getSupplierId());
			assertEquals("Fancy product description",civ2.getItem().getDesc());
			assertEquals("Fancy",civ1.getItem().getDesc());
		}
		else {
			fail();
		}
		
		assertEquals(20,civ1.getQuantity());
		assertEquals(20,civ2.getQuantity());
		
		assertEquals(10, civ1.getItem().getPrice());
		assertEquals(10, civ2.getItem().getPrice());
		
		assertEquals("I1202", civ1.getItem().getItemId().getProductId());
		assertEquals("I1202", civ2.getItem().getItemId().getProductId());
		
	}
}

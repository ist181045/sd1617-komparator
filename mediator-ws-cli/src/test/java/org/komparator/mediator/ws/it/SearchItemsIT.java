package org.komparator.mediator.ws.it;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.komparator.mediator.ws.InvalidText_Exception;
import org.komparator.mediator.ws.ItemView;
import org.komparator.supplier.ws.BadProductId_Exception;
import org.komparator.supplier.ws.BadProduct_Exception;
import org.komparator.supplier.ws.BadQuantity_Exception;
import org.komparator.supplier.ws.InsufficientQuantity_Exception;
import org.komparator.supplier.ws.ProductView;
import org.komparator.supplier.ws.cli.SupplierClient;
import org.komparator.supplier.ws.cli.SupplierClientException;

public class SearchItemsIT extends BaseIT {
	
	// static members
	private static SupplierClient sc1;
	private static SupplierClient sc2;

	// one-time initialization and clean-up
	@BeforeClass
	public static void oneTimeSetUp() {

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
					product.setDesc("Zero Quantity description");
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
					product.setId("I1203");
					product.setDesc("Yet another fancy description");
					product.setPrice(9);
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

	@AfterClass
	public static void oneTimeTearDown() {
		mediatorClient.clear();
	}

	// members

	// initialization and clean-up for each test
	@Before
	public void setUp() {
	}

	@After
	public void tearDown() {
	}

	
	// bad input tests

	@Test (expected = InvalidText_Exception.class)
	public void nullDescription() throws InvalidText_Exception {
		mediatorClient.searchItems(null);
	}
	
	@Test (expected = InvalidText_Exception.class)
	public void emptyDescription() throws InvalidText_Exception {
		mediatorClient.searchItems("");
	}
	
	@Test (expected = InvalidText_Exception.class)
	public void whitespaceDescription() throws InvalidText_Exception {
		mediatorClient.searchItems(" ");
	}
	
	@Test (expected = InvalidText_Exception.class)
	public void tabDescription() throws InvalidText_Exception {
		mediatorClient.searchItems("\t");
	}
	
	@Test (expected = InvalidText_Exception.class)
	public void newLineDescription() throws InvalidText_Exception {
		mediatorClient.searchItems("\n");
	}
	
	//Success cases
	@Test 
	public void nonExistentDesc() throws InvalidText_Exception {
		List<ItemView> products = mediatorClient.searchItems("Lalalalala");
		assertEquals(0, products.size());
	}

	@Test
	public void nonExistingItem() throws InvalidText_Exception {
		List<ItemView> products = mediatorClient.searchItems("I1203");
		assertEquals(0, products.size());
	}

	@Test
	public void unorderedSearch() throws InvalidText_Exception {
		List<ItemView> products = mediatorClient.searchItems("desc");
		assertEquals(4, products.size());
	}

	@Test
	public void orderedProducts() throws InvalidText_Exception {
		List<ItemView> products = mediatorClient.searchItems("desc");
		assertEquals(4, products.size());

		// Check order criteria two by two
		for (int i = 0; i < products.size() - 1; i++) {
			// Check the first order criterion: product id
			final String firstProductId = products.get(i).getItemId().getProductId();
			final String secondProductId = products.get(i + 1).getItemId().getProductId();
			assertTrue(firstProductId.compareTo(secondProductId) <= 0);
			// Check the second order criterion: price
			if (firstProductId.equals(secondProductId)) {
				final int firstPrice = products.get(i).getPrice();
				final int secondPrice = products.get(i + 1).getPrice();
				assertTrue(firstPrice <= secondPrice);
			}
		}
	}

	@Test
	public void testCaseSensitivity() throws InvalidText_Exception {
		{
			List<ItemView> items = mediatorClient.searchItems("zero");
			assertEquals(0, items.size());
		}
		{
			List<ItemView> items = mediatorClient.searchItems("Zero");
			assertEquals(1, items.size());
		}
	}

	@Test
	public void successOneItem() throws InvalidText_Exception {
		List<ItemView> products = mediatorClient.searchItems("product");
		
		assertEquals(1, products.size());
		
		assertEquals("I1202", products.get(0).getItemId().getProductId());
		assertEquals(sc1.getWsName(), products.get(0).getItemId().getSupplierId());
		assertEquals("Fancy product description", products.get(0).getDesc());
		assertEquals(10, products.get(0).getPrice());
	}
	
	@Test 
	public void successZeroQuantityItem() throws InvalidText_Exception {
		
		try {
			sc1.buyProduct("I1420", 1);
		} catch (BadProductId_Exception | BadQuantity_Exception | InsufficientQuantity_Exception e) {
			e.printStackTrace();
			fail("Non-conclusive test");
		}
		
		//Now the product quantity is 0
		
		List<ItemView> products = mediatorClient.searchItems("Zero");
		
		assertEquals(1, products.size());
		
		assertEquals("I1420", products.get(0).getItemId().getProductId());
		assertEquals(sc1.getWsName(), products.get(0).getItemId().getSupplierId());
		assertEquals("Zero Quantity description", products.get(0).getDesc());
		assertEquals(10, products.get(0).getPrice());
		
	}
	
	//Test order of result itemviews 
	
	@Test 
	public void successMultipleSupplier() throws InvalidText_Exception {
		List<ItemView> products = mediatorClient.searchItems("Fancy");
		
		assertEquals(2, products.size());
		
		assertEquals("I1202", products.get(0).getItemId().getProductId());
		assertEquals(sc1.getWsName(), products.get(0).getItemId().getSupplierId());
		assertEquals("Fancy product description", products.get(0).getDesc());
		assertEquals(10, products.get(0).getPrice());
		
		assertEquals("I1203", products.get(1).getItemId().getProductId());
		assertEquals(sc1.getWsName(), products.get(1).getItemId().getSupplierId());
		assertEquals("Fancy description", products.get(1).getDesc());
		assertEquals(10, products.get(1).getPrice());
		
	}
	
	
}

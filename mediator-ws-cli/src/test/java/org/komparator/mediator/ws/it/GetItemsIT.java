package org.komparator.mediator.ws.it;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.komparator.mediator.ws.InvalidItemId_Exception;
import org.komparator.mediator.ws.ItemView;
import org.komparator.supplier.ws.BadProductId_Exception;
import org.komparator.supplier.ws.BadProduct_Exception;
import org.komparator.supplier.ws.ProductView;
import org.komparator.supplier.ws.cli.SupplierClient;
import org.komparator.supplier.ws.cli.SupplierClientException;
import org.omg.CORBA.DynAnyPackage.Invalid;

/**
 * Test suite
 */
public class GetItemsIT extends BaseIT {

	private static SupplierClient sc1;
	private static SupplierClient sc2;

	private static final String PRODUCT1_ID = "X1";
	private static final String PRODUCT1_DESC = "Basketball";
	
	private static final String PRODUCT2_ID = "Y2";
	private static final String PRODUCT2_DESC = "Football";
	
	private static final String PRODUCT3_ID = "Z3";
	private static final String PRODUCT3_DESC = "Baseball";
	
	private static final String SUPPLIER1_ID = "A58_Supplier1";
	private static final String SUPPLIER2_ID = "A58_Supplier2";
	
	
	@BeforeClass
	public static void oneTimeSetUp() {
		
		mediatorClient.clear();
		
		try {
			sc1 = new SupplierClient(testProps.getProperty("uddi.url"), SUPPLIER1_ID);
			sc2 = new SupplierClient(testProps.getProperty("uddi.url"), SUPPLIER2_ID);
		} catch (SupplierClientException e) {
			fail();
		}
		
		{
			ProductView product = new ProductView();
			product.setId(PRODUCT1_ID);
			product.setDesc(PRODUCT1_DESC);
			product.setPrice(10);
			product.setQuantity(10);
			try {
				sc1.createProduct(product);
			} catch (BadProductId_Exception | BadProduct_Exception e) {
				fail();
			}
		}
		
		{
			ProductView product = new ProductView();
			product.setId(PRODUCT1_ID);
			product.setDesc(PRODUCT1_DESC);
			product.setPrice(20);
			product.setQuantity(15);
			try {
				sc2.createProduct(product);
			} catch (BadProductId_Exception | BadProduct_Exception e) {
				fail();
			}
		}
		
		{
			ProductView product = new ProductView();
			product.setId(PRODUCT2_ID);
			product.setDesc(PRODUCT2_DESC);
			product.setPrice(38);
			product.setQuantity(8);
			try {
				sc1.createProduct(product);
			} catch (BadProductId_Exception | BadProduct_Exception e) {
				fail();
			}
		}
		
		{
			ProductView product = new ProductView();
			product.setId(PRODUCT2_ID);
			product.setDesc(PRODUCT2_DESC);
			product.setPrice(30);
			product.setQuantity(69);
			try {
				sc2.createProduct(product);
			} catch (BadProductId_Exception | BadProduct_Exception e) {
				fail();
			}
		}
		
		{
			ProductView product = new ProductView();
			product.setId(PRODUCT3_ID);
			product.setDesc(PRODUCT3_DESC);
			product.setPrice(23);
			product.setQuantity(1);
			try {
				sc2.createProduct(product);
			} catch (BadProductId_Exception | BadProduct_Exception e) {
				fail();
			}
		}
	}
	
	
	//input tests
	
	@Test(expected = InvalidItemId_Exception.class)
	public void getItemsNullTest() throws InvalidItemId_Exception {
		mediatorClient.getItems(null);
	}
	
	@Test(expected = InvalidItemId_Exception.class)
	public void getItemsEmptyTest() throws InvalidItemId_Exception {
		mediatorClient.getItems("");
	}
	
	@Test(expected = InvalidItemId_Exception.class)
	public void getItemsWhitespaceTest() throws InvalidItemId_Exception {
		mediatorClient.getItems(" ");
	}
	
	@Test(expected = InvalidItemId_Exception.class)
	public void getitemsTabDest() throws InvalidItemId_Exception {
		mediatorClient.getItems("\t");
	}

	@Test(expected = InvalidItemId_Exception.class)
	public void getItemsNewLineTest() throws InvalidItemId_Exception {
		mediatorClient.getItems("\n");
	}
	
	//other tests
	@Test
	public void success() throws InvalidItemId_Exception {
		List<ItemView> result = mediatorClient.getItems(PRODUCT3_ID);
		
		assertEquals(1, result.size());
		
		assertEquals(PRODUCT3_ID, result.get(0).getItemId().getProductId());
		assertEquals(PRODUCT3_DESC, result.get(0).getDesc());
		assertEquals(23, result.get(0).getPrice());
		assertEquals(SUPPLIER2_ID, result.get(0).getItemId().getSupplierId());
	}

	@Test
	public void inexistingItem1() throws InvalidItemId_Exception {
		List<ItemView> items = mediatorClient.getItems("W4");
		assertTrue(items == null || items.size() == 0);
	}

	@Test
	public void inexistingItem2() throws InvalidItemId_Exception {
		List<ItemView> items = mediatorClient.getItems("Baseball");
		assertTrue(items == null || items.size() == 0);
	}

	@Test
	public void singleExistingItem() throws InvalidItemId_Exception {
		List<ItemView> items = mediatorClient.getItems(PRODUCT3_ID);
		assertEquals(1, items.size());
	}

	@Test
	public void severalSuppliers() throws InvalidItemId_Exception {
		List<ItemView> result = mediatorClient.getItems(PRODUCT1_ID);
		assertEquals(2, result.size());
	}

	@Test
	public void testOrder1() throws InvalidItemId_Exception {
		List<ItemView> items = mediatorClient.getItems(PRODUCT1_ID);
		assertEquals(2, items.size());
		assertTrue(items.get(0).getPrice() <= items.get(1).getPrice());
	}

	@Test
	public void testOrder2() throws InvalidItemId_Exception {
		List<ItemView> items = mediatorClient.getItems(PRODUCT2_ID);
		assertEquals(2, items.size());
		assertTrue(items.get(0).getPrice() <= items.get(1).getPrice());
	}

	@Test
	public void noResult() throws InvalidItemId_Exception {
		List<ItemView> result =mediatorClient.getItems(
				"cenas fixes que nao existem");
		assertEquals(0, result.size());
	}
	
	
	@AfterClass
	public static void cleanUp() {
		mediatorClient.clear();
		// clear suppliers explicitly
		sc1.clear();
		sc2.clear();
	}
	
	
	
	
}

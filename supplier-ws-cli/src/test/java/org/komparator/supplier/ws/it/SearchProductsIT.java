package org.komparator.supplier.ws.it;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.komparator.supplier.ws.*;
/**
 * Test suite
 */
public class SearchProductsIT extends BaseIT {

	// static members

	// one-time initialization and clean-up
	@BeforeClass
	public static void oneTimeSetUp() throws BadProductId_Exception, BadProduct_Exception {
		// clear remote service state before all tests
		client.clear();

		// fill-in test products
		// (since getProduct is read-only the initialization below
		// can be done once for all tests in this suite)
		{
			ProductView product = new ProductView();
			product.setId("X1");
			product.setDesc("Basketball");
			product.setPrice(10);
			product.setQuantity(10);
			client.createProduct(product);
		}
		{
			ProductView product = new ProductView();
			product.setId("Y2");
			product.setDesc("Soccer ball");
			product.setPrice(20);
			product.setQuantity(20);
			client.createProduct(product);
		}
		{
			ProductView product = new ProductView();
			product.setId("Z3");
			product.setDesc("Racket");
			product.setPrice(30);
			product.setQuantity(30);
			client.createProduct(product);
		}
	}

	@AfterClass
	public static void oneTimeTearDown() {
		client.clear();
	}

	// members

	// initialization and clean-up for each test
	@Before
	public void setUp() {
	}

	@After
	public void tearDown() {
	}

	// tests
	// assertEquals(expected, actual);

	// public List<ProductView> searchProducts(String descText) throws
	// BadText_Exception

	// bad input tests

	@Test (expected = BadText_Exception.class)
	public void nullDescription() throws BadText_Exception {
		client.searchProducts(null);
	}
	
	@Test (expected = BadText_Exception.class)
	public void emptyDescription() throws BadText_Exception {
		client.searchProducts("");
	}
	
	@Test (expected = BadText_Exception.class)
	public void whitespaceDescription() throws BadText_Exception {
		client.searchProducts(" ");
	}
	
	@Test (expected = BadText_Exception.class)
	public void tabDescription() throws BadText_Exception {
		client.searchProducts("\t");
	}
	
	@Test (expected = BadText_Exception.class)
	public void newLineDescription() throws BadText_Exception {
		client.searchProducts("\n");
	}
	
	
	
	// main tests

	@Test
	public void searchProductExists() throws BadText_Exception {
		String desc = "Basketball";
		List<ProductView> searchResults = client.searchProducts(desc);
		assertEquals(1, searchResults.size());
		assertEquals("X1", searchResults.get(0).getId());
		assertEquals(10, searchResults.get(0).getPrice());
		assertEquals(10, searchResults.get(0).getQuantity());
		assertEquals(desc, searchResults.get(0).getDesc());
	}
	
	@Test
	public void searchProductNotExists() throws BadText_Exception {
		assertEquals(0, client.searchProducts("cenas fixes").size());
	}
	
	@Test
	public void searchProductTwoExist() throws BadText_Exception {
		assertEquals(2, client.searchProducts("ball").size());
	}
	
	@Test
	public void searchProductLowerCase() throws BadText_Exception {
		assertEquals(1, client.searchProducts("basketball").size());
	}
	

}

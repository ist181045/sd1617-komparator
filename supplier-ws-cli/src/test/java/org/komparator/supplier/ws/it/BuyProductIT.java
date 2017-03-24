package org.komparator.supplier.ws.it;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.komparator.supplier.ws.BadProductId_Exception;
import org.komparator.supplier.ws.BadProduct_Exception;
import org.komparator.supplier.ws.BadQuantity_Exception;
import org.komparator.supplier.ws.InsufficientQuantity_Exception;
import org.komparator.supplier.ws.ProductView;

/**
 * Test suite
 */
public class BuyProductIT extends BaseIT {

	@Before
	public void setUp() throws BadProductId_Exception,
			BadProduct_Exception {
		{
			ProductView product = new ProductView();
			product.setId("I1202");
			product.setDesc("Fancy product description");
			product.setPrice(10);
			product.setQuantity(20);
			client.createProduct(product);
		}
	}

	@After
	public void tearDown() {
		client.clear();
	}

	/*
	 * Success test case: Verifies the purchaseId is the expected value '1'
	 * since it is the first purchase, and that the quantity available changed
	 * accordingly
	 */
	@Test
	public void success() throws BadProductId_Exception,
			BadQuantity_Exception, InsufficientQuantity_Exception {
		String purchaseId = client.buyProduct("I1202", 4);
		ProductView product = client.getProduct("I1202");

		Assert.assertEquals("1", purchaseId);
		Assert.assertEquals(16, product.getQuantity());
	}

	/*
	 * Tests for a productId with some whitespace characters
	 */
	@Test(expected = BadProductId_Exception.class)
	public void blankProductID() throws BadProductId_Exception,
			BadQuantity_Exception, InsufficientQuantity_Exception {
		client.buyProduct("   ", 1);
	}

	/*
	 * Tests if a newline is a valid productId
	 */
	@Test(expected = BadProductId_Exception.class)
	public void newlineInProductID() throws BadProductId_Exception,
			BadQuantity_Exception, InsufficientQuantity_Exception {
		client.buyProduct("\n", 1);
	}

	/*
	 * Tests if an horizontal tab is a valid productId
	 */
	@Test(expected = BadProductId_Exception.class)
	public void tabInProductID() throws BadProductId_Exception,
			BadQuantity_Exception, InsufficientQuantity_Exception {
		client.buyProduct("\t", 1);
	}

	/*
	 * Tests if it is possible to buy zero of a given product
	 */
	@Test(expected = BadQuantity_Exception.class)
	public void zeroQuantity() throws BadProductId_Exception,
			BadQuantity_Exception, InsufficientQuantity_Exception {
		client.buyProduct("I1202", 0);
	}

	/*
	 * Tests if it is possible to buy a negative amount of a given product
	 */
	@Test(expected = BadQuantity_Exception.class)
	public void negativeQuantity() throws BadProductId_Exception,
			BadQuantity_Exception, InsufficientQuantity_Exception {
		client.buyProduct("I1202", -1);
	}

	/*
	 * Tests if it is possible to buy more of a product than there is available
	 */
	@Test(expected = InsufficientQuantity_Exception.class)
	public void greaterQuantity() throws BadProductId_Exception,
			BadQuantity_Exception, InsufficientQuantity_Exception {
		client.buyProduct("I1202", 21);
	}

	/*
	 * Attempts to buy product that does not exists (id not in the product
	 * collection)
	 */
	@Test(expected = BadProductId_Exception.class)
	public void idMismatch() throws BadProductId_Exception,
			BadQuantity_Exception, InsufficientQuantity_Exception {
		client.buyProduct("i1202", 4);
	}
}

package org.komparator.mediator.ws.it;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.komparator.mediator.ws.CartItemView;
import org.komparator.mediator.ws.CartView;
import org.komparator.mediator.ws.InvalidCartId_Exception;
import org.komparator.mediator.ws.InvalidItemId_Exception;
import org.komparator.mediator.ws.InvalidQuantity_Exception;
import org.komparator.mediator.ws.ItemIdView;
import org.komparator.mediator.ws.NotEnoughItems_Exception;
import org.komparator.supplier.ws.BadProductId_Exception;
import org.komparator.supplier.ws.BadProduct_Exception;
import org.komparator.supplier.ws.ProductView;
import org.komparator.supplier.ws.cli.SupplierClient;
import org.komparator.supplier.ws.cli.SupplierClientException;

public class AddToCartIT extends BaseIT {
	
	// static members
	private final static String CART_ID = "CartId";
	
	private static SupplierClient sc1;
	private static SupplierClient sc2;

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
	
	//Main tests
	
		//Not enough items
	@Test
	public void notEnoughItems() throws InvalidCartId_Exception, InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
		ItemIdView iiv = newIIV("I1202", sc1.getWsName());
		iiv.setProductId("I1202");
		iiv.setSupplierId(sc1.getWsName());

		try {
			mediatorClient.addToCart(CART_ID, iiv, 1000);
			fail();
		} catch (NotEnoughItems_Exception nie) {
			assertEquals(0, mediatorClient.listCarts().size());
		}
	}

	@Test
	public void notEnoughItemsByOne() throws InvalidCartId_Exception, InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
		ItemIdView iiv = newIIV("I1202", sc1.getWsName());
		try {
			mediatorClient.addToCart(CART_ID, iiv, 20);
			mediatorClient.addToCart(CART_ID, iiv, 1);
			fail();
		} catch (NotEnoughItems_Exception nie) {
			List<CartView> cartList = mediatorClient.listCarts();
			assertEquals(1, cartList.size());
			assertEquals(20, cartList.get(0).getItems().get(0).getQuantity());
		}
	}

	@Test
	public void singleItemOnceCheckCartState() throws InvalidCartId_Exception, InvalidItemId_Exception,
			InvalidQuantity_Exception, NotEnoughItems_Exception {
		ItemIdView id = newIIV("I1202", sc1.getWsName());
		mediatorClient.addToCart(CART_ID, id, 1);

		List<CartView> cartList = mediatorClient.listCarts();
		assertEquals(1, cartList.size());

		CartView cart = cartList.get(0);
		assertEquals(CART_ID, cart.getCartId());
		assertEquals(1, cart.getItems().size());

		CartItemView cartItem = cart.getItems().get(0);
		assertEquals(1, cartItem.getQuantity());
		assertEquals("I1202", cartItem.getItem().getItemId().getProductId());
		assertEquals(sc1.getWsName(), cartItem.getItem().getItemId().getSupplierId());
		assertEquals("Fancy product description", cartItem.getItem().getDesc());
		assertEquals(10, cartItem.getItem().getPrice());
	}

	@Test
	public void singleItemAddedTwoTimes() throws InvalidCartId_Exception, InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
		// adds an item 2 times (quantity = 1 + 1 = 2)
		ItemIdView iiv = newIIV("I1202", sc1.getWsName());
		mediatorClient.addToCart(CART_ID, iiv, 1);
		mediatorClient.addToCart(CART_ID, iiv, 1);

		List<CartView> cartList = mediatorClient.listCarts();
		assertEquals(1, cartList.size());

		CartView cart = cartList.get(0);
		assertEquals(CART_ID, cart.getCartId());
		assertEquals(1, cart.getItems().size());

		CartItemView cartItem = cart.getItems().get(0);
		assertEquals(2, cartItem.getQuantity());
		assertEquals(10, cartItem.getItem().getPrice());
	}

	@Test
	public void singleItemAddAll() throws InvalidCartId_Exception, InvalidItemId_Exception,
			InvalidQuantity_Exception, NotEnoughItems_Exception {
		// resulting quantity in supplier is 0, still valid
		ItemIdView id = newIIV("I1202", sc1.getWsName());
		mediatorClient.addToCart(CART_ID, id, 20);
	}

	@Test
	public void singleItemAddAllTwoTimesDifferentCarts() throws InvalidCartId_Exception, InvalidItemId_Exception,
			InvalidQuantity_Exception, NotEnoughItems_Exception {
		// addToCart should not decrease available quantity
		ItemIdView id = newIIV("I1202", sc1.getWsName());
		mediatorClient.addToCart(CART_ID + "1", id, 20);
		mediatorClient.addToCart(CART_ID + "2", id, 20);
	}

	@Test
	public void multipleItemsOneCart() throws InvalidCartId_Exception, InvalidItemId_Exception,
			InvalidQuantity_Exception, NotEnoughItems_Exception {
		// -- add products --
		mediatorClient.addToCart("xyz",
				newIIV("I1202", sc1.getWsName()), 20);

		mediatorClient.addToCart("xyz",
				newIIV("I1202", sc2.getWsName()), 20);

		mediatorClient.addToCart("xyz",
				newIIV("I1203", sc1.getWsName()), 20);

		// -- assert number of carts --
		List<CartView> cartList = mediatorClient.listCarts();
		assertEquals(1, cartList.size());

		// -- assert state of each cart --
		// there are no ordering guarantees, and therefore
		// the code below assumes no ordering
		CartView cart = cartList.get(0);
		assertEquals("xyz", cart.getCartId());
		assertEquals(3, cart.getItems().size());

		// check first product added
		{
			final String prodId = "I1202";
			final String supplierId = sc1.getWsName();
			final String desc = "Fancy product description";
			final int price = 10;
			final int quantity = 20;
			final boolean itemStateIsCorrect =
					checkItemInCart(cart, prodId, supplierId, desc, price, quantity);
			assertTrue(itemStateIsCorrect);
		}

		// check second product added
		{
			final String prodId = "I1202";
			final String supplierId = sc2.getWsName();
			final String desc = "Fancy";
			final int price = 10;
			final int quantity = 20;
			final boolean itemStateIsCorrect =
					checkItemInCart(cart, prodId, supplierId, desc, price, quantity);
			assertTrue(itemStateIsCorrect);
		}

		// check third product added
		{
			final String prodId = "I1203";
			final String supplierId = sc1.getWsName();
			final String desc = "Fancy description";
			final int price = 10;
			final int quantity = 20;
			final boolean itemStateIsCorrect =
					checkItemInCart(cart, prodId, supplierId, desc, price, quantity);
			assertTrue(itemStateIsCorrect);
		}
	}

	@Test
	public void multipleItemsMultipleCarts() throws InvalidCartId_Exception, InvalidItemId_Exception,
			InvalidQuantity_Exception, NotEnoughItems_Exception {
		// -- add products --
		mediatorClient.addToCart("xyz",
				newIIV("I1202", sc1.getWsName()), 20);

		mediatorClient.addToCart("otherCart",
				newIIV("I1202", sc2.getWsName()), 20);

		mediatorClient.addToCart("xyz",
				newIIV("I1203", sc1.getWsName()), 20);

		// -- assert number of carts and total number of items --
		List<CartView> cartList = mediatorClient.listCarts();
		assertEquals(2, cartList.size());
		assertEquals(3,
				cartList.get(0).getItems().size() + cartList.get(1).getItems().size());

		// -- assert state of each cart --
		// there are no ordering guarantees, and therefore
		// the code below assumes no ordering

		// check first product added
		{
			final String cartId = "xyz";
			final String prodId = "I1202";
			final String supplierId = sc1.getWsName();
			final String desc = "Fancy product description";
			final int price = 10;
			final int quantity = 20;
			CartView cart = findCart(cartList, cartId);
			final boolean itemStateIsCorrect =
					checkItemInCart(cart, prodId, supplierId, desc, price, quantity);
			assertTrue(itemStateIsCorrect);
		}

		// check second product added
		{
			final String cartId = "otherCart";
			final String prodId = "I1202";
			final String supplierId = sc2.getWsName();
			final String desc = "Fancy";
			final int price = 10;
			final int quantity = 20;
			CartView cart = findCart(cartList, cartId);
			final boolean itemStateIsCorrect =
					checkItemInCart(cart, prodId, supplierId, desc, price, quantity);
			assertTrue(itemStateIsCorrect);
		}

		// check third product added
		{
			final String cartId = "xyz";
			final String prodId = "I1203";
			final String supplierId = sc1.getWsName();
			final String desc = "Fancy description";
			final int price = 10;
			final int quantity = 20;
			CartView cart = findCart(cartList, cartId);
			final boolean itemStateIsCorrect =
					checkItemInCart(cart, prodId, supplierId, desc, price, quantity);
			assertTrue(itemStateIsCorrect);
		}
	}


	private CartView findCart(List<CartView> cartList, String cartId) {
		// Find cart by id
		CartView cart = null;
		for (CartView c : cartList) {
			if (c.getCartId().equals(cartId)) {
				cart = c;
				break;
			}
		}
		return cart;
	}

	private boolean checkItemInCart(CartView cart, String prodId, String supplierId, String desc, int price,
			int quantity) {

		// Check if there is a matching item in the cart.
		// We cannot assume any particular order.
		if (cart != null) {
			for (CartItemView cartItemView : cart.getItems()) {
				boolean found = cartItemView.getItem().getItemId().getProductId().equals(prodId)
						&& cartItemView.getItem().getItemId().getSupplierId().equals(supplierId)
						&& cartItemView.getItem().getDesc().equals(desc) && cartItemView.getItem().getPrice() == price
						&& cartItemView.getQuantity() == quantity;

				if (found)
					return true;
			}
		}

		return false;
	}

	private ItemIdView newIIV(String pid, String sid) {
		ItemIdView iiv = new ItemIdView();
		iiv.setProductId(pid);
		iiv.setSupplierId(sid);
		return iiv;
	}
}

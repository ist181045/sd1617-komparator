package org.komparator.mediator.ws.it;

import static org.junit.Assert.assertEquals;

import org.junit.BeforeClass;
import org.junit.Test;
import org.komparator.supplier.ws.BadProductId_Exception;
import org.komparator.supplier.ws.BadProduct_Exception;
import org.komparator.supplier.ws.ProductView;
import org.komparator.supplier.ws.cli.SupplierClient;
import org.komparator.supplier.ws.cli.SupplierClientException;


/**
 * Test suite
 */
public class ClearIT extends BaseIT {
	
	private static SupplierClient sc1;
	private static SupplierClient sc2;
	
	@BeforeClass
	public static void oneTimeSetUp() {
		
		try {
			sc1 = new SupplierClient(testProps.getProperty("uddi.url"), "A58_Supplier1");
			sc2 = new SupplierClient(testProps.getProperty("uddi.url"), "A58_Supplier2");
			
			// clear remote services state before all tests
			
			sc1.clear();
			sc2.clear();
			
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
					product.setId("I1203");
					product.setDesc("Fancy product description");
					product.setPrice(10);
					product.setQuantity(20);
					sc1.createProduct(product);
				}
				{
					ProductView product = new ProductView();
					product.setId("I1213");
					product.setDesc("Fancy product description");
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
	
	//Success test case

	@Test
	public void clearMultipleSuppliers() {
		assertEquals(2, sc1.listProducts().size());
		assertEquals(1, sc2.listProducts().size());
		
		mediatorClient.clear();
		
		assertEquals(0,sc1.listProducts().size());
		assertEquals(0,sc2.listProducts().size());
		
	}
}

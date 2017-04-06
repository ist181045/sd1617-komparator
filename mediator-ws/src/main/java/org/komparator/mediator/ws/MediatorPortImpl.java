package org.komparator.mediator.ws;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.jws.WebService;

import org.komparator.supplier.ws.BadProductId_Exception;
import org.komparator.supplier.ws.BadText_Exception;
import org.komparator.supplier.ws.ProductView;
import org.komparator.supplier.ws.cli.SupplierClient;
import org.komparator.supplier.ws.cli.SupplierClientException;

import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDIRecord;


@WebService(
        endpointInterface = "org.komparator.mediator.ws.MediatorPortType",
        wsdlLocation = "mediator.1_0.wsdl",
        name = "MediatorWebService",
        portName = "MediatorPort",
        targetNamespace = "http://ws.mediator.komparator.org/",
        serviceName = "MediatorService"
)
public class MediatorPortImpl implements MediatorPortType {

    // end point manager
    private MediatorEndpointManager endpointManager;

    //shopping carts
    private Map<String,CartView> carts = new HashMap<>();

    // shopping history
    private Map<LocalDateTime, ShoppingResultView> shoppingHistory
            = new TreeMap<>();

    // ItemView comparator
    private static final Comparator<ItemView> ITEM_VIEW_COMPARATOR = (iv1, iv2) -> {
        String pid1 = iv1.getItemId().getProductId();
        String pid2 = iv2.getItemId().getProductId();

        int stringCompare = pid1.compareTo(pid2);

        return stringCompare != 0
                ? stringCompare
                : iv2.getPrice() - iv1.getPrice();
    };

    public MediatorPortImpl(MediatorEndpointManager endpointManager) {
        this.endpointManager = endpointManager;
    }

    @Override
    public void clear() {
        List<SupplierClient> suppliers;
        try {
            suppliers = supplierLookup();
        } catch (MediatorException | SupplierClientException e) {
            e.printStackTrace();
            return;
        }

        for (SupplierClient supplier : suppliers) {
            supplier.clear();
        }
        
        //Reset carts
        carts.clear();

    }

    @Override
    public List<ItemView> getItems(String productId) throws InvalidItemId_Exception {
        if (productId == null)
            throwInvalidItemId("Product ID cannot be null!");
        productId = productId.trim();
        if (productId.length() == 0)
            throwInvalidItemId("Product ID cannot be empty or whitespace!");

        List<SupplierClient> suppliers;
        try {
            suppliers = supplierLookup();
        } catch (MediatorException me) {
            //Handle things
            return null;
        } catch (SupplierClientException sce) {
            //Handle other things
            return null;
        }

        TreeSet<ItemView> products = new TreeSet<>(ITEM_VIEW_COMPARATOR);
        for (SupplierClient supplier : suppliers) {
            ProductView productview = null;
            try {
                productview = supplier.getProduct(productId);
            } catch (BadProductId_Exception e) {
                //Handle things
            }

            if (productview != null) {
                products.add(newItemView(productview, "Supplier_name"));
            }
        }

        return new ArrayList<>(products);
    }

    @Override
    public List<CartView> listCarts() {
        return new ArrayList<>(carts.values());
    }

    @Override
    public List<ItemView> searchItems(String descText) throws InvalidText_Exception {
        // check description text
        if (descText == null)
            throwInvalidText("Product description cannot be null!");
        descText = descText.trim();
        if (descText.length() == 0)
            throwInvalidText("Product description cannot be empty or whitespace!");

        List<SupplierClient> suppliers;
        try {
            suppliers = supplierLookup();
        } catch (MediatorException | SupplierClientException e) {
            e.printStackTrace();
            return null;
        }

        TreeSet<ItemView> products = new TreeSet<>(ITEM_VIEW_COMPARATOR);
        for (SupplierClient supplier : suppliers) {
            List<ProductView> productViews = null;
            try {
                productViews = supplier.searchProducts(descText);
            } catch (BadText_Exception e) {
                e.printStackTrace();
            }

            if (productViews != null) {
                for (ProductView pv : productViews) {
                    products.add(newItemView(pv, "supplier.getWsName()"));
                }
            }

        }

        return new ArrayList<>(products);

    }

    @Override
    public ShoppingResultView buyCart(String cartId, String creditCardNr)
            throws EmptyCart_Exception, InvalidCartId_Exception, InvalidCreditCard_Exception {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void addToCart(String cartId, ItemIdView itemId, int itemQty) throws InvalidCartId_Exception,
            InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {

    	// check cartID
        if (cartId == null)
            throwInvalidCartId("CartId cannot be null!");
        cartId = cartId.trim();
        if (cartId.length() == 0)
            throwInvalidCartId("CartId cannot be empty or whitespace!");

        // check itemId
        String productId = itemId.getProductId();
        if (productId == null)
            throwInvalidItemId("Product ID cannot be null!");
        productId = productId.trim();
        if (productId.length() == 0)
            throwInvalidItemId("Product ID cannot be empty or whitespace!");
        itemId.setProductId(productId);

        String supplierId = itemId.getSupplierId();
        if (supplierId == null)
            throwInvalidItemId("Supplier ID cannot be null!");
        supplierId = supplierId.trim();
        if (supplierId.length() == 0)
            throwInvalidItemId("Supplier ID cannot be empty or whitespace!");
        itemId.setProductId(supplierId);
        //check invalid quantity
      	if (itemQty <= 0)
      		throwInvalidQuantity("Quantity cannot be 0 or negative!");

      	ProductView pv = null;
  		try {
			SupplierClient supplier = new SupplierClient(endpointManager.getUddiURL(), supplierId);
			pv = supplier.getProduct(productId);
		} catch (SupplierClientException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BadProductId_Exception e) {
			// ISTO NUNCA VAI ACONTECER
			e.printStackTrace();
		}
  		if(pv != null) {
  			if(pv.getQuantity() < itemQty) {
  				throwNotEnoughItems("Quantity not available in supplier");
  			}
  			CartView cv = carts.get(cartId);
  			if(cv == null) {
  				cv = new CartView();
  				cv.setCartId(cartId);
  			}

  			ItemView iv = new ItemView();
			iv.setDesc(pv.getDesc());
			iv.setItemId(itemId);
			iv.setPrice(pv.getPrice());

			CartItemView civ = new CartItemView();
			civ.setItem(iv);
			civ.setQuantity(itemQty);


			cv.getItems().add(civ);
			carts.put(cartId, cv);
  		}

    }

    public String ping(String arg0) {
        if (arg0 == null || arg0.trim().length() == 0 ||
                arg0.trim().matches(".*[\\r\\n\\t]+.*")) {
            return "Invalid ping argument!";
        }

        arg0 = arg0.trim();

        List<SupplierClient> suppliers;
        try {
            suppliers = supplierLookup();
        } catch (MediatorException | SupplierClientException e) {
            return "Error occurred: " + e.getMessage();
        }

        if (suppliers != null) {
            StringBuilder sb = new StringBuilder();
            for (SupplierClient supplier : suppliers) {
                sb.append(String.format("Response from %s: ", supplier.getWsURL()));
                sb.append(supplier.ping(arg0));
                sb.append(System.lineSeparator());
            }

            return sb.toString();
        }

        return "No suppliers available!";
    }

    @Override
    public synchronized List<ShoppingResultView> shopHistory() {
        return new ArrayList<>(shoppingHistory.values());
    }

    // View helpers ----------------------------------------------------------

    private ItemView newItemView(ProductView pv, String supplierName) {
        ItemView view = new ItemView();
        ItemIdView idview = new ItemIdView();

        idview.setProductId(pv.getId());
        idview.setSupplierId(supplierName);

        view.setItemId(idview);
        view.setDesc(pv.getDesc());
        view.setPrice(pv.getPrice());
        return view;
    }
    /*
	private PurchaseView newPurchaseView(Purchase purchase) {
		PurchaseView view = new PurchaseView();
		view.setId(purchase.getPurchaseId());
		view.setProductId(purchase.getProductId());
		view.setQuantity(purchase.getQuantity());
		view.setUnitPrice(purchase.getUnitPrice());
		return view;
	}
	*/
    // Exception helpers -----------------------------------------------------

    /**
     * Helper method to throw new InvalidText exception
     */
    private void throwInvalidText(final String message) throws InvalidText_Exception {
        InvalidText faultInfo = new InvalidText();
        faultInfo.message = message;
        throw new InvalidText_Exception(message, faultInfo);
    }
    
    private void throwInvalidCartId(final String message) throws InvalidCartId_Exception {
    	InvalidCartId faultInfo = new InvalidCartId();
        faultInfo.message = message;
        throw new InvalidCartId_Exception(message, faultInfo);
    }

    private void throwInvalidItemId(final String message) throws InvalidItemId_Exception {
    	InvalidItemId faultInfo = new InvalidItemId();
        faultInfo.message = message;
        throw new InvalidItemId_Exception(message, faultInfo);
    }

    private void throwInvalidQuantity(final String message) throws InvalidQuantity_Exception {
    	InvalidQuantity faultInfo = new InvalidQuantity();
        faultInfo.message = message;
        throw new InvalidQuantity_Exception(message, faultInfo);
    }

    private void throwNotEnoughItems(final String message) throws NotEnoughItems_Exception {
    	NotEnoughItems faultInfo = new NotEnoughItems();
        faultInfo.message = message;
        throw new NotEnoughItems_Exception(message, faultInfo);
    }

    /**
     * UDDI supplier lookup
     */
    private List<SupplierClient> supplierLookup() throws MediatorException,
            SupplierClientException {
        Collection<UDDIRecord> uddiRecords;
        String uddiURL = endpointManager.getUddiURL();

        try {

            UDDINaming uddiNaming = new UDDINaming(uddiURL);
            uddiRecords = uddiNaming.listRecords("A58_Supplier%");

        } catch (Exception e) {
            String msg = String.format("Failed supplier lookup on uddi at %s!",
                    uddiURL);
            throw new MediatorException(msg, e);
        }

        if (uddiRecords == null) {
            String msg = String.format(
                    "A58 suppliers not found on UDDI at %s", uddiURL);
            throw new MediatorException(msg);
        } else {
            List<SupplierClient> suppliers = new ArrayList<>();
            for (UDDIRecord element : uddiRecords) {
                try {
                	SupplierClient temp = new SupplierClient(element.getUrl());
                	if(temp.ping("A58_Mediator") != null)
                		suppliers.add(temp);
                    
                } catch (SupplierClientException sce) {
                    sce.printStackTrace();
                }
            }

            return suppliers;
        }
    }
}

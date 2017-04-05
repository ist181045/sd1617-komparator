package org.komparator.mediator.ws;

import javax.jws.WebService;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDIRecord;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;
import org.komparator.supplier.ws.BadText_Exception;
import org.komparator.supplier.ws.ProductView;
import org.komparator.supplier.ws.cli.SupplierClient;
import org.komparator.supplier.ws.cli.SupplierClientException;


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

    private List<SupplierClient> suppliers;

    public MediatorPortImpl(MediatorEndpointManager endpointManager) {
        this.endpointManager = endpointManager;
    }

    @Override
    public void clear() {
        // TODO Auto-generated method stub

    }

    @Override
    public List<ItemView> getItems(String productId) throws InvalidItemId_Exception {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<CartView> listCarts() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<ItemView> searchItems(String descText) throws InvalidText_Exception {
        // check description text
        if (descText == null)
            throwInvalidText("Product description cannot be null!");
        descText = descText.trim();
        if (descText.length() == 0)
            throwInvalidText("Product description cannot be empty or whitespace!");

        try {
            supplierLookup();
        } catch (MediatorException me) {

        } catch (SupplierClientException sce) {

        }
        TreeSet<ItemView> products = new TreeSet<>(new ItemViewComparator());

        for (SupplierClient supplier : suppliers) {
            List<ProductView> productviews = null;
            try {
                productviews = supplier.searchProducts(descText);
            } catch (BadText_Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            if (productviews != null) {
                for (ProductView pv : productviews) {
                    products.add(newItemView(pv, "supplier.getWsURL()"));
                }
            }

        }

        return new ArrayList<ItemView>(products);

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
        // TODO Auto-generated method stub

    }

    @Override
    public String ping(String arg0) {
        try {
            supplierLookup();
        } catch (MediatorException | SupplierClientException e) {
            e.printStackTrace();
        }

        StringBuilder sb = new StringBuilder();
        for (SupplierClient supplier : suppliers) {
            sb.append(String.format("Response from %s: ", supplier.getWsURL()));
            sb.append(supplier.ping(arg0));
            sb.append("%n");
        }

        return sb.toString();
    }

    @Override
    public List<ShoppingResultView> shopHistory() {
        // TODO Auto-generated method stub
        return null;
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

    /**
     * UDDI lookup
     */
    private void supplierLookup() throws MediatorException, SupplierClientException {
        Collection<UDDIRecord> uddiRecords = null;
        String uddiURL = endpointManager.getUddiURL();

        try {

            UDDINaming uddiNaming = new UDDINaming(uddiURL);
            uddiRecords = uddiNaming.listRecords("%Supplier%");

        } catch (Exception e) {
            String msg = String.format("Failed supplier lookup on uddi at %s!",
                    uddiURL);
            throw new MediatorException(msg, e);
        }

        if (uddiRecords == null) {
            String msg = String.format(
                    "Suppliers with name estupido not found on UDDI at %s", uddiURL);
            throw new MediatorException(msg);
        } else {
            suppliers = new ArrayList<>();
            for (UDDIRecord element : uddiRecords) {
                try {
                    this.suppliers.add(new SupplierClient(uddiURL, element.getOrgName()));
                } catch (SupplierClientException sce) {
                    //Possibly do stuff
                }
            }
        }
    }

    class ItemViewComparator implements Comparator<ItemView> {

        @Override
        public int compare(ItemView iv1, ItemView iv2) {
            String pId1 = iv1.getItemId().getProductId();
            String pId2 = iv2.getItemId().getProductId();

            int iP1 = iv1.getPrice();
            int iP2 = iv2.getPrice();

            int ola = pId1.compareTo(pId2);

            if (ola != 0) {
                return ola;
            }
            return (iP1 >= iP2 ? 1 : -1);
        }
    }
}

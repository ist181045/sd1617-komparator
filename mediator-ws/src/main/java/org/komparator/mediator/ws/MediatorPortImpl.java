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

import javax.jws.WebService;

import org.komparator.supplier.ws.BadProductId_Exception;
import org.komparator.supplier.ws.BadQuantity_Exception;
import org.komparator.supplier.ws.BadText_Exception;
import org.komparator.supplier.ws.InsufficientQuantity_Exception;
import org.komparator.supplier.ws.ProductView;
import org.komparator.supplier.ws.cli.SupplierClient;
import org.komparator.supplier.ws.cli.SupplierClientException;

import pt.ulisboa.tecnico.sdis.ws.cli.CreditCardClient;
import pt.ulisboa.tecnico.sdis.ws.cli.CreditCardClientException;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINamingException;
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

    // ItemView comparator
    private static final Comparator<ItemView> ITEM_VIEW_COMPARATOR = (iv1, iv2) -> {
        String pid1 = iv1.getItemId().getProductId();
        String pid2 = iv2.getItemId().getProductId();

        int stringCompare = pid1.compareTo(pid2);

        return stringCompare != 0
                ? stringCompare
                : iv1.getPrice() - iv2.getPrice();
    };
    public static final String CREDIT_CARD_WS_URL = "http://ws.sd.rnl.tecnico.ulisboa.pt:8080/cc";
    // end point manager
    private MediatorEndpointManager endpointManager;
    //shopping carts
    private Map<String, CartView> carts = new HashMap<>();
    // shopping history
    private Map<LocalDateTime, ShoppingResultView> shoppingHistory
            = new TreeMap<>();

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

        //Reset history
        shoppingHistory.clear();

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
            me.printStackTrace();
            return null;
        } catch (SupplierClientException sce) {
            sce.printStackTrace();
            return null;
        }

        TreeSet<ItemView> products = new TreeSet<>(ITEM_VIEW_COMPARATOR);

        synchronized (this) {
            for (SupplierClient supplier : suppliers) {
                ProductView productview = null;
                try {
                    productview = supplier.getProduct(productId);
                } catch (BadProductId_Exception e) {
                    e.printStackTrace();
                    return null;
                }

                if (productview != null) {
                    products.add(newItemView(productview, supplier.getWsName()));
                }
            }
        }

        return new ArrayList<>(products);
    }

    @Override
    public synchronized List<CartView> listCarts() {
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

        synchronized (this) {
            for (SupplierClient supplier : suppliers) {
                List<ProductView> productViews = null;
                try {
                    productViews = supplier.searchProducts(descText);
                } catch (BadText_Exception e) {
                    e.printStackTrace();
                }

                if (productViews != null) {
                    for (ProductView pv : productViews) {
                        products.add(newItemView(pv, supplier.getWsName()));
                    }
                }

            }
        }


        return new ArrayList<>(products);

    }

    @Override
    public ShoppingResultView buyCart(String cartId, String creditCardNr)
            throws EmptyCart_Exception, InvalidCartId_Exception, InvalidCreditCard_Exception {
        if (cartId == null || cartId.trim().length() == 0
                || cartId.matches(".*[\r\n\t]+.*"))
            throwInvalidCartId("Cart ID given is null, empty or contains "
                + "whitespace characters!");

        if (creditCardNr == null || creditCardNr.trim().length() == 0
                || creditCardNr.matches(".*[\r\n\t]+.*")) {
            throwInvalidCreditCard("Credit Card number given is null, empty "
                + "or contains whitespace characters!");
        }

        try {
            Integer.parseInt(creditCardNr.trim());
        } catch (NumberFormatException nfe) {
            throwInvalidCreditCard("Credit Card number given is not a number!");
        }

        CreditCardClient ccClient;
        try {
            ccClient = new CreditCardClient(CREDIT_CARD_WS_URL);
        } catch (CreditCardClientException e) {
            System.err.println("Couldn't communicate with CreditCard web "
                    + "service: " + e.getMessage());
            return null;
        }
        if (!ccClient.validateNumber(creditCardNr)) {
            throwInvalidCreditCard("Credit card number given couldn't "
                    + "be validated!");
        }

        CartView cv = carts.get(cartId.trim());
        if (cv == null)
            throwInvalidCartId("Cart with the given ID couldn't be found!");

        if (cv.getItems().isEmpty())
            throwEmptyCart("Cart with the given ID is empty!");

        synchronized (this) {
            ShoppingResultView srv = new ShoppingResultView();

            for (CartItemView civ : cv.getItems()) {
                String sid = civ.getItem().getItemId().getSupplierId();

                try {
                    String pid = civ.getItem().getItemId().getProductId();
                    int quantity = civ.getQuantity();
                    SupplierClient supplier =
                            new SupplierClient(endpointManager.getUddiURL(), sid);

                    try {
                        supplier.buyProduct(pid, quantity);
                        srv.getPurchasedItems().add(civ);
                    } catch (BadProductId_Exception
                            | InsufficientQuantity_Exception
                            | BadQuantity_Exception e) {
                        System.err.println("Could not buy product: "
                                + e.getMessage());
                        srv.getDroppedItems().add(civ);
                    }
                } catch (SupplierClientException e) {
                    System.err.println("Couldn't find supplier: "
                            + e.getMessage());
                    srv.getDroppedItems().add(civ);
                }
            }

            if (!srv.getPurchasedItems().isEmpty()) {
                if (srv.getDroppedItems().isEmpty()) {
                    srv.setResult(Result.COMPLETE);
                } else {
                    srv.setResult(Result.PARTIAL);
                }
            } else {
                srv.setResult(Result.EMPTY);
            }

            LocalDateTime datetime = LocalDateTime.now();
            String srvId = "SR#"
                    + String.format("%010d", shoppingHistory.size())
                    + "@" + datetime; // SR#xxxxxxxxxx@yyyy-mm-ddThh:mm:ss.nnn

            srv.setId(srvId);

            shoppingHistory.put(datetime, srv);

            return srv;
        }
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
        if (itemId == null)
            throwInvalidItemId("Item ID cannot be null!");

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
        itemId.setSupplierId(supplierId);

        //check invalid quantity
        if (itemQty <= 0)
            throwInvalidQuantity("Quantity cannot be 0 or negative!");

        ProductView pv = null;
        try {
            SupplierClient supplier = new SupplierClient(endpointManager.getUddiURL(), supplierId);
            synchronized (this) {
                pv = supplier.getProduct(productId);
            }
        } catch (SupplierClientException e) {
            System.err.println("Supplier Connection Error");
            e.printStackTrace();
            return;
        } catch (BadProductId_Exception e) {
            throwInvalidItemId("Product ID exception on Supplier");
            return;
        }
        if (pv != null) {
            if (pv.getQuantity() < itemQty) {
                throwNotEnoughItems("Quantity not available in supplier");
            }
            CartView cv = carts.get(cartId);
            if (cv == null) {
                cv = new CartView();
                cv.setCartId(cartId);
            }


            ItemView iv = newItemView(pv, supplierId);
            CartItemView civ = new CartItemView();

            civ.setItem(iv);
            civ.setQuantity(itemQty);


            cv.getItems().add(civ);

            synchronized (this) {
                carts.put(cartId, cv);
            }
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
                sb.append(String.format("Response from %s: ",
                        supplier.getWsName()));
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

    // Exception helpers -----------------------------------------------------

    private void throwEmptyCart(final String message)
            throws EmptyCart_Exception {
        EmptyCart faultInfo = new EmptyCart();
        faultInfo.message = message;
        throw new EmptyCart_Exception(message, faultInfo);
    }

    private void throwInvalidCreditCard(final String message)
            throws InvalidCreditCard_Exception {
        InvalidCreditCard faultInfo = new InvalidCreditCard();
        faultInfo.message = message;
        throw new InvalidCreditCard_Exception(message, faultInfo);
    }

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
                    temp.setWsName(element.getOrgName());
                    if (temp.ping("A58_Mediator") != null)
                        suppliers.add(temp);

                } catch (SupplierClientException sce) {
                    sce.printStackTrace();
                }
            }

            return suppliers;
        }
    }
}

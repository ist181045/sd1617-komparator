package org.komparator.mediator.ws;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.jws.WebService;

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
	
	private String uddiURL;
	private String wsName;
	
	private List<SupplierClient> suppliers;

	public MediatorPortImpl(MediatorEndpointManager endpointManager, String uddiURL, String wsName) {
		this.endpointManager = endpointManager;
		
		this.uddiURL = uddiURL;
		this.wsName = wsName;
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
		// TODO Auto-generated method stub
		return null;
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<ShoppingResultView> shopHistory() {
		// TODO Auto-generated method stub
		return null;
	}

    /** UDDI lookup */
    private void supplierLookup() throws MediatorException, SupplierClientException {
    	Collection<UDDIRecord> uddi_records = null;
        try {

            UDDINaming uddiNaming = new UDDINaming(uddiURL);
            uddi_records = uddiNaming.listRecords(wsName);

        } catch (Exception e) {
            String msg = String.format("Failed supplier lookup on uddi at %s!",
                    uddiURL);
            throw new MediatorException(msg, e);
        }

        if (uddi_records == null) {
            String msg = String.format(
                    "Suppliers with name %s not found on UDDI at %s", wsName,
                    uddiURL);
            throw new MediatorException(msg);
        }
        else {
        	for(UDDIRecord element : uddi_records) {
        		try {
        			this.suppliers.add(new SupplierClient(this.uddiURL, element.getOrgName()));
        		}
        		catch (SupplierClientException sce) {
        			//Possibly do stuff
        		}
        	}
        }
    }

}

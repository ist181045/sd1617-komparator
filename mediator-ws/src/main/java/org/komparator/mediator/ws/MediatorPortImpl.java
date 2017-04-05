package org.komparator.mediator.ws;

import java.util.List;

import javax.jws.WebService;

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

	

}

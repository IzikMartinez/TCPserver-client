import java.io.IOException;

public class ClientProtocolHandler extends AbstractProtocolHandler {

	public static final int STATE_WAITING_ACK_1 = 1;
	public static final int STATE_WAITING_ACK_2 = 2;

	

	

	private boolean containsNegativeAcknowledgements(ProtocolMessageWindow window){

		for(ProtocolMessage message: window.getMessages()){
			if (message.getHeader().equals(AbstractProtocolHandler.RESPONSE_NEGATIVE_ACKNOWLEDGEMENT)){
				return true;
			}
		}

		return false;
	}

	private boolean containsPositiveAcknowledgements(ProtocolMessageWindow window){

		for(ProtocolMessage message: window.getMessages()){
			if (message.getHeader().equals(AbstractProtocolHandler.RESPONSE_NEGATIVE_ACKNOWLEDGEMENT)){
				return false;
			}
		}

		return true;
	}

	private ProtocolMessageWindow getMessagesToResend(ProtocolMessageWindow originalWindow, ProtocolMessageWindow responseWindow){
		ProtocolMessageWindow result = new ProtocolMessageWindow();

		for(ProtocolMessage negAckMessage: responseWindow.getMessages()){
			int index = Integer.parseInt(negAckMessage.getBody());
			result.addMessage(originalWindow.getMessages().get(index));
		}

		return result;		
	}

	public ProtocolMessageWindow handleProtocolMessageWindow(ProtocolMessageWindow ingressWindow) throws IOException, ClassNotFoundException{

		ProtocolMessageWindow responseWindow = new ProtocolMessageWindow();

		switch(this.currentState){

		default:
		case STATE_DISCONNECTED:
			this.currentState = STATE_WAITING_ACK_2;
			responseWindow = ingressWindow;
			break;

		case STATE_CONNECTED_IDLE:
			if(containsNegativeAcknowledgements(ingressWindow))
				responseWindow = getMessagesToResend(ingressWindow,responseWindow);
			else
				responseWindow = ingressWindow;
				this.currentState = STATE_WAITING_ACK_1;
		    break;

		case STATE_WAITING_ACK_1:
			if(containsPositiveAcknowledgements(ingressWindow)) {
				this.currentState = STATE_CONNECTED_IDLE;
				responseWindow = ingressWindow;
			}
			else if (containsNegativeAcknowledgements(ingressWindow)) {
			    this.currentState= STATE_WAITING_ACK_2;
			    responseWindow  = getMessagesToResend(ingressWindow,responseWindow);
			}
			break;

		case STATE_WAITING_ACK_2:
			if(containsPositiveAcknowledgements(ingressWindow)) {
				this.currentState = STATE_CONNECTED_IDLE;
				responseWindow = ingressWindow;
			}
			else if (containsNegativeAcknowledgements(ingressWindow)) {
				responseWindow  = getMessagesToResend(ingressWindow,responseWindow);
			}
			break;
		}

		return responseWindow;
	}
}

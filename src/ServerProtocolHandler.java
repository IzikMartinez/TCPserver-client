import java.util.ArrayList;

public class ServerProtocolHandler extends AbstractProtocolHandler{
    
    public static final int STATE_RECEIVING_WINDOW = 1;
    public static final int STATE_WAITING_MISSING_MESSAGE = 2;
    public static final int STATE_WINDOW_PROCESSED = 3;
    
    private PasswordTable passwordTable;
    private RBACMonitor monitor;
    
    public ServerProtocolHandler(PasswordTable passwordTable, 
    		               RBACMonitor monitor){
    	this.passwordTable = passwordTable;
        this.monitor = monitor; 
    }
    
    
    public ProtocolMessageWindow processProtocolMessageWindow(ProtocolMessageWindow window){
    	ProtocolMessageWindow egressWindow = new ProtocolMessageWindow();
    	
    	for(ProtocolMessage message: window.getMessages()){
    		
    		ProtocolMessage response = this.handleRequestMessage(message);
    		egressWindow.addMessage(response);
    	}
    	
    	return egressWindow;
    }
    
    public PasswordTable getPasswordTable(){
        return this.passwordTable;
    }
    
    public RBACMonitor getRBACMonitor(){
        return this.monitor;
    }
    
    private ProtocolMessage getResponseMessage(String header, String body){    
        return new ProtocolMessage(header, body);
    }

    private ProtocolMessage handleAuthorizationRequest(ProtocolMessage requestMessage){
        
        ProtocolMessage responseMessage = this.getResponseMessage(RESPONSE_ERROR, "ERROR WHEN HANDLING AUTHORIZATION");
        
        String[] bodySegments = requestMessage.getBody().split(BODY_SEGMENTS_SEPARATOR);
        
        String subRequest = bodySegments[0];
        String user;
        String permission;
        String role;
        
        switch(subRequest){
  
            case AUTHORIZATION_REQUEST_CHECK_PERM:
                user = bodySegments[1];
                permission = bodySegments[2];
                
                if(this.monitor.hasPermission(user, permission)){
                    responseMessage = this.getResponseMessage(RESPONSE_SUCCESS, "PERMISSION GRANTED!");            
                }else{
                    responseMessage = this.getResponseMessage(RESPONSE_ERROR, "PERMISSION DENIED!");
                }
                break;
            
            case AUTHORIZATION_REQUEST_ADD_PERM:
                role = bodySegments[1];
                permission = bodySegments[2];
                
                if(!this.monitor.hasPermission(role, permission)){
                    this.monitor.addPermission(role, permission);
                    responseMessage = this.getResponseMessage(RESPONSE_SUCCESS, "PERMISSION ADDED!");
                }else{
                    responseMessage = this.getResponseMessage(RESPONSE_ERROR, "PERMISSION ALREADY ADDED!");
                }
                break;

            case AUTHORIZATION_REQUEST_REM_PERM:
                role = bodySegments[1];
                permission = bodySegments[2];
                
                if(!this.monitor.hasPermission(role, permission)){
                    this.monitor.removePermission(role, permission);
                    responseMessage = this.getResponseMessage(RESPONSE_SUCCESS, "PERMISSION REMOVED!");
                }else{
                    responseMessage = this.getResponseMessage(RESPONSE_ERROR, "NON-EXISTENT PERMISSION!");
                }
                break;
    
                
            case AUTHORIZATION_REQUEST_ADD_ROLE:
                role = bodySegments[1];
                user = bodySegments[2];
                
                if(!this.monitor.hasRole(role, user)){
                    this.monitor.addRole(role, user);
                    responseMessage = this.getResponseMessage(RESPONSE_SUCCESS, "ROLE ADDED!");
                }else{
                    responseMessage = this.getResponseMessage(RESPONSE_ERROR, "ROLE ALREADY ADDED!");
                }
                break;
          
            case AUTHORIZATION_REQUEST_REM_ROLE:
                role = bodySegments[1];
                user = bodySegments[2];
                
                if(this.monitor.hasRole(role, user)){
                    this.monitor.removeRole(role, user);
                    responseMessage = this.getResponseMessage(RESPONSE_SUCCESS, "ROLE REMOVED!");
                }else{
                    responseMessage = this.getResponseMessage(RESPONSE_ERROR, "NON-EXISTENT ROLE!");
                }    
                
            default:
                break;
                
        }
        
        return responseMessage;
    }
    
    private ProtocolMessage handleAuthenticationRequest(ProtocolMessage requestMessage){
        
        ProtocolMessage responseMessage = this.getResponseMessage(RESPONSE_ERROR, "ERROR WHEN HANDLING AUTHENTICATION");
        
        String[] bodySegments = requestMessage.getBody().split(BODY_SEGMENTS_SEPARATOR);
        
        String subRequest = bodySegments[0];        
        String username = bodySegments[1];
        String password = bodySegments[2];;
        
        switch(subRequest){
                
            case AUTHENTICATION_REQUEST_CHECK_PWD:
                if(this.passwordTable.checkCorrectPassword(username, password)){
                    responseMessage = this.getResponseMessage(RESPONSE_SUCCESS, "PASSWORD CORRECT!");            
                }else{
                    responseMessage = this.getResponseMessage(RESPONSE_ERROR, "PASSWORD INCORRECT!");    
                }
                break;
            
            case AUTHENTICATION_REQUEST_ADD_PWD:
                this.passwordTable.addPassword(username, password);
                responseMessage = this.getResponseMessage(RESPONSE_SUCCESS, "PASSWORD ADDED!");            
                break;
                
            default: 
                break;
        }
        
        return responseMessage;
    }
    
    private ProtocolMessage handleRequestMessage(ProtocolMessage requestMessage){
    
        ProtocolMessage responseMessage = null;
        
        switch(requestMessage.getHeader()){
            
            case REQUEST_INITIATE_CONNECTION:
                responseMessage = this.getResponseMessage(RESPONSE_CONNECTION_ACCEPTED, "CONNECTION ACCEPTED!");
                break;
                
            case REQUEST_CLOSE_CONNECTION:
                responseMessage = this.getResponseMessage(RESPONSE_CONNECTION_CLOSED, "CONNECTION CLOSED!");
                break;
                
            case REQUEST_AUTHENTICATE: 
                responseMessage = this.handleAuthenticationRequest(requestMessage);
                break;
            
            case REQUEST_AUTHORIZE: 
                responseMessage = this.handleAuthorizationRequest(requestMessage);
                break; 
                
            default: 
                responseMessage = this.getResponseMessage(RESPONSE_ERROR,"REQUEST NOT RECOGNIZED");
                break;
        }
        
        return responseMessage;
    }

    public int getCurrentState(){
    	return this.currentState;
    }
    
    public ProtocolMessageWindow getNegativeAcknowledmentMessages(ProtocolMessageWindow ingressWindow){
    	
    	ArrayList<Integer> missingMessages = ingressWindow.getMissingMessages();
    	ProtocolMessageWindow result = new ProtocolMessageWindow();
    	
    	for(Integer sequenceNumber: missingMessages){
    		ProtocolMessage negativeAckmsg = new ProtocolMessage(RESPONSE_NEGATIVE_ACKNOWLEDGEMENT, sequenceNumber.toString());
    		result.addMessage(negativeAckmsg);
    	}
    	
    	return result;
    }

    public ProtocolMessageWindow getPositiveAcknowledmentMessages(ProtocolMessageWindow ingressWindow){

        ArrayList<Integer> missingMessages = ingressWindow.getMissingMessages();
        ProtocolMessageWindow result = new ProtocolMessageWindow();

        for(Integer sequenceNumber: missingMessages){
            ProtocolMessage positiveAckmsg = new ProtocolMessage(RESPONSE_NEGATIVE_ACKNOWLEDGEMENT, sequenceNumber.toString());
            result.addMessage(positiveAckmsg);
        }

        return result;
    }
    
    public static ProtocolMessageWindow initiateConnection(ProtocolMessageWindow requestWindow) {
    	ProtocolMessageWindow response = new ProtocolMessageWindow();
    	
    	ProtocolMessage requestMessage = requestWindow.getMessages().get(0);
    	
    	if(requestMessage.getHeader().equals(AbstractProtocolHandler.REQUEST_INITIATE_CONNECTION)) {
    		response.addMessage(new ProtocolMessage(AbstractProtocolHandler.RESPONSE_CONNECTION_ACCEPTED, "Connection Accepted!"));
    	}
    	
    	return response;
    }
    
    public static ProtocolMessageWindow closeConnection(ProtocolMessageWindow requestWindow) {
    	
    	ProtocolMessageWindow response = new ProtocolMessageWindow();
    	response.addMessage(new ProtocolMessage(AbstractProtocolHandler.RESPONSE_CONNECTION_CLOSED, "Bye!"));
    	
    	System.out.println("Shutting down Authorization Server...bye!");
    	
    	return response;
    }
    
    public static boolean isOpenConnectionRequest(ProtocolMessageWindow requestWindow) {
    	return requestWindow.getMessages().get(0).getHeader().equals(REQUEST_INITIATE_CONNECTION);
    }
    
    public static boolean isCloseConnectionRequest(ProtocolMessageWindow requestWindow) {
    	return requestWindow.getMessages().get(0).getHeader().equals(REQUEST_CLOSE_CONNECTION);
    }

    public static boolean isLostMessage(ProtocolMessageWindow requestWindow) {
        return requestWindow.getMessages().get(0).getHeader().equals(SIMULATED_LOST_MESSAGE);
    }
    
    public ProtocolMessageWindow handleProtocolMessageWindow(ProtocolMessageWindow ingressWindow) {
    	
    	ProtocolMessageWindow result = new ProtocolMessageWindow();

    	switch(this.currentState){

    	case STATE_DISCONNECTED:
    		if(isOpenConnectionRequest(ingressWindow)) {
    			result = initiateConnection(ingressWindow);
    			this.currentState = STATE_CONNECTED_IDLE;
    		}
    		break;
    	
    	case STATE_CONNECTED_IDLE:
    	    if(isLostMessage(ingressWindow))
    	        result = getNegativeAcknowledmentMessages(ingressWindow);
    	    else
    	        result = processProtocolMessageWindow(ingressWindow);
    	    break;

    	case STATE_RECEIVING_WINDOW:
    	    if(isLostMessage(ingressWindow)) {
    	        result = getNegativeAcknowledmentMessages(ingressWindow);
    	        this.currentState = STATE_WAITING_MISSING_MESSAGE;
            } else {
    	        result = processProtocolMessageWindow(ingressWindow);
    	        this.currentState = STATE_CONNECTED_IDLE;
            }

    		break;

    	case STATE_WAITING_MISSING_MESSAGE:
    	    if(isLostMessage(ingressWindow))
    	        result = getNegativeAcknowledmentMessages(ingressWindow);
    	    else {
                result = processProtocolMessageWindow(ingressWindow);
                this.currentState = STATE_CONNECTED_IDLE;
            }
    		break;

        case STATE_WINDOW_PROCESSED:

    	default:
    		break;
    	
    	}
    
    	return result;
    }
    
    
    
}
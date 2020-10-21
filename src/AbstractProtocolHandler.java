import java.io.IOException;

public abstract class AbstractProtocolHandler {

	public static final String REQUEST_INITIATE_CONNECTION = "000";
    public static final String REQUEST_CLOSE_CONNECTION    = "999";
    
    public static final String REQUEST_AUTHENTICATE = "100";
    public static final String REQUEST_AUTHORIZE    = "200";
    
    public static final String RESPONSE_CONNECTION_ACCEPTED = "001";
    public static final String RESPONSE_CONNECTION_CLOSED   = "1000";
    
    public static final String RESPONSE_SUCCESS = "300";
    public static final String RESPONSE_ERROR = "400";
    public static final String RESPONSE_NEGATIVE_ACKNOWLEDGEMENT = "500";
    
    public static final String BODY_SEGMENTS_SEPARATOR = ":";
    
    public static final String AUTHENTICATION_REQUEST_CHECK_PWD = "101";
    public static final String AUTHENTICATION_REQUEST_ADD_PWD   = "102";
    
    public static final String AUTHORIZATION_REQUEST_CHECK_PERM = "201";
    public static final String AUTHORIZATION_REQUEST_ADD_PERM   = "202";
    public static final String AUTHORIZATION_REQUEST_ADD_ROLE   = "203";
    public static final String AUTHORIZATION_REQUEST_REM_PERM   = "204";
    public static final String AUTHORIZATION_REQUEST_REM_ROLE   = "205";

    public static final String SIMULATED_LOST_MESSAGE = "600";
    
    protected int currentState = STATE_DISCONNECTED;
    
    public static final int STATE_DISCONNECTED = -1;
    public static final int STATE_CONNECTED_IDLE = 0;

    protected ProtocolMessageWindow originalWindow;
    
    public ProtocolMessageWindow getOriginalWindow() {
		return originalWindow;
	}

	public void setOriginalWindow(ProtocolMessageWindow originalWindow) {
		this.originalWindow = originalWindow;
	}
    
    public int getCurrentState(){
    	return this.currentState;
    }
    
    public void setCurrentState(int state){
    	this.currentState = state;
    }
    
    public abstract ProtocolMessageWindow handleProtocolMessageWindow(ProtocolMessageWindow ingressWindow) throws IOException, ClassNotFoundException;
}

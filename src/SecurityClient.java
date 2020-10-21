import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

public class SecurityClient {
    
    public static BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
    
       public static ProtocolMessage getAddPasswordRequest() throws IOException{
        
        System.out.println("Enter username:");
        
        String username = stdIn.readLine();
        
        System.out.println("Enter password:");
        
        String password = stdIn.readLine();
        
        return new ProtocolMessage(ServerProtocolHandler.REQUEST_AUTHENTICATE, 
                                   ServerProtocolHandler.AUTHENTICATION_REQUEST_ADD_PWD +
                                   ServerProtocolHandler.BODY_SEGMENTS_SEPARATOR +
                                   username + 
                                   ServerProtocolHandler.BODY_SEGMENTS_SEPARATOR + 
                                   password);
    }
    
    
    public static ProtocolMessage getCheckPasswordRequest() throws IOException{
        
        System.out.println("Enter username:");
        
        String username = stdIn.readLine();
        
        System.out.println("Enter password:");
        
        String password = stdIn.readLine();
        
        return new ProtocolMessage(ServerProtocolHandler.REQUEST_AUTHENTICATE, 
                                   ServerProtocolHandler.AUTHENTICATION_REQUEST_CHECK_PWD +
                                   ServerProtocolHandler.BODY_SEGMENTS_SEPARATOR +
                                   username + 
                                   ServerProtocolHandler.BODY_SEGMENTS_SEPARATOR + 
                                   password);
    }
    
    public static ProtocolMessage getCheckPermissionRequest() throws IOException{
        
        System.out.println("Enter username:");
        
        String username = stdIn.readLine();
        
        System.out.println("Enter permission:");
        
        String permission = stdIn.readLine();
        
        return new ProtocolMessage(ServerProtocolHandler.REQUEST_AUTHORIZE, 
                                   ServerProtocolHandler.AUTHORIZATION_REQUEST_CHECK_PERM  +
                                   ServerProtocolHandler.BODY_SEGMENTS_SEPARATOR +
                                   username + 
                                   ServerProtocolHandler.BODY_SEGMENTS_SEPARATOR + 
                                   permission);
    }
    
    
    public static ProtocolMessage getAddPermissionRequest() throws IOException{
        
        System.out.println("Enter role:");
        
        String role = stdIn.readLine();
        
        System.out.println("Enter permission:");
        
        String permission = stdIn.readLine();
        
        return new ProtocolMessage(AbstractProtocolHandler.REQUEST_AUTHORIZE, 
                                   AbstractProtocolHandler.AUTHORIZATION_REQUEST_ADD_PERM  +
                                   AbstractProtocolHandler.BODY_SEGMENTS_SEPARATOR +
                                   role + 
                                   AbstractProtocolHandler.BODY_SEGMENTS_SEPARATOR + 
                                   permission);
    }
    
    public static ProtocolMessage getRemovePermissionRequest() throws IOException{
    	
    	System.out.println("Enter role:");

    	String role = stdIn.readLine();

    	System.out.println("Enter permission:");

    	String permission = stdIn.readLine();

    	return new ProtocolMessage(AbstractProtocolHandler.REQUEST_AUTHORIZE, 
    			AbstractProtocolHandler.AUTHORIZATION_REQUEST_REM_PERM  +
    			AbstractProtocolHandler.BODY_SEGMENTS_SEPARATOR +
    			role + 
    			AbstractProtocolHandler.BODY_SEGMENTS_SEPARATOR + 
    			permission);	

    }
    
    public static ProtocolMessage getAddRoleRequest() throws IOException{
        
        System.out.println("Enter role:");
        
        String role = stdIn.readLine();
        
        System.out.println("Enter username:");
        
        String username = stdIn.readLine();
        
        return new ProtocolMessage(ServerProtocolHandler.REQUEST_AUTHORIZE, 
                                   ServerProtocolHandler.AUTHORIZATION_REQUEST_ADD_ROLE  +
                                   ServerProtocolHandler.BODY_SEGMENTS_SEPARATOR +
                                   role + 
                                   ServerProtocolHandler.BODY_SEGMENTS_SEPARATOR + 
                                   username);
    }
    
    public static ProtocolMessage getRemoveRoleRequest() throws IOException{
        
        System.out.println("Enter role:");
        
        String role = stdIn.readLine();
        
        System.out.println("Enter username:");
        
        String username = stdIn.readLine();
        
        return new ProtocolMessage(ServerProtocolHandler.REQUEST_AUTHORIZE, 
                                   ServerProtocolHandler.AUTHORIZATION_REQUEST_REM_ROLE  +
                                   ServerProtocolHandler.BODY_SEGMENTS_SEPARATOR +
                                   role + 
                                   ServerProtocolHandler.BODY_SEGMENTS_SEPARATOR + 
                                   username);
    }
    
    private static void handleClientRequest(ObjectInputStream in, 
    										ObjectOutputStream out, 
    										ClientProtocolHandler clientHandler,
    										ArrayList<ProtocolMessage> messages) throws IOException, ClassNotFoundException{
    	
    	ProtocolMessageWindow originalWindow = new ProtocolMessageWindow(messages);
    	ProtocolMessageWindow window = new ProtocolMessageWindow(messages);
    	
    	System.out.println("Original Window...");
		System.out.println(originalWindow);
		clientHandler.setOriginalWindow(originalWindow);
		
    	window = simulateLossOfMessages(window);
    	window = clientHandler.handleProtocolMessageWindow(window);
    	
    	do{
    		System.out.println("Sending Window...");
    		System.out.println(window);
    		out.writeObject(window);
    		
    		window = (ProtocolMessageWindow) in.readObject();
    		System.out.println("Receiving Window...");
    		System.out.println(window);
    		
    		window = clientHandler.handleProtocolMessageWindow(window);
    		
    	} while(clientHandler.getCurrentState() != AbstractProtocolHandler.STATE_CONNECTED_IDLE);
    	
    }
    
    private static void initiateConnection(ObjectInputStream in, ObjectOutputStream out, ClientProtocolHandler clientHandler) throws IOException, ClassNotFoundException{
    	
    	ProtocolMessageWindow window = new ProtocolMessageWindow();
    	window.addMessage(new ProtocolMessage(AbstractProtocolHandler.REQUEST_INITIATE_CONNECTION, "Hello!"));
    	Boolean flag = true;
    	
    	while(flag){
    		System.out.println("Sending Request: " + window);
    		out.writeObject(window);
    		
    		window = (ProtocolMessageWindow) in.readObject();
    		System.out.println("Receiving Response: " + window);
    		
    		if(window.getMessages().get(0).getHeader().equals(AbstractProtocolHandler.RESPONSE_CONNECTION_ACCEPTED)){
    			clientHandler.setCurrentState(AbstractProtocolHandler.STATE_CONNECTED_IDLE);
    			flag = false;
    		}
    	}
    }
    
   private static void closeConnection(ObjectInputStream in, ObjectOutputStream out, ClientProtocolHandler clientHandler) throws IOException, ClassNotFoundException{
    	
    	ProtocolMessageWindow window = new ProtocolMessageWindow();
    	window.addMessage(new ProtocolMessage(AbstractProtocolHandler.REQUEST_CLOSE_CONNECTION, "Bye!"));
    	Boolean flag = true;
    	
    	while(flag){
    		out.writeObject(window);
    		window = (ProtocolMessageWindow) in.readObject();
    		
    		if(window.getMessages().get(0).getHeader().equals(AbstractProtocolHandler.RESPONSE_CONNECTION_CLOSED)){
    			clientHandler.setCurrentState(AbstractProtocolHandler.STATE_DISCONNECTED);
    			flag = false;
    		}
    	}
    }
    
   public static ProtocolMessageWindow simulateLossOfMessages(ProtocolMessageWindow originalWindow) {
	   
	   
	   int numMessagesLost = ThreadLocalRandom.current().nextInt(0, originalWindow.getSize());
	   
	   System.out.println("Number of Messages Lost: " + numMessagesLost);
	   
	   for(int i = 0; i < numMessagesLost; i++) {
		   int sequenceNumberToRemove = ThreadLocalRandom.current().nextInt(0, originalWindow.getSize() + 1);
		   ProtocolMessage simulatedLostMessage = new ProtocolMessage(AbstractProtocolHandler.SIMULATED_LOST_MESSAGE, "Lost Message!",sequenceNumberToRemove);
		   
		   originalWindow.removeMessageAt(sequenceNumberToRemove);
		   originalWindow.addMessageAt(simulatedLostMessage, sequenceNumberToRemove);
	   }
	   
	   return originalWindow;
   }
    
    public static void main(String[] args) throws IOException {
        
        if (args.length != 2) {
            System.err.println(
                "Usage: java SecurityClient <host name> <port number>");
            System.exit(1);
        }

        String hostName = args[0];
        int portNumber = Integer.parseInt(args[1]);

        try (
            Socket serverSocket = new Socket(hostName, portNumber);
            ObjectOutputStream out = new ObjectOutputStream(serverSocket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(serverSocket.getInputStream());
        ) {
            
            String userInput;
            boolean flag = true;
            
            ClientProtocolHandler clientHandler = new ClientProtocolHandler();
        	initiateConnection(in, out, clientHandler);
            
        	ArrayList<ProtocolMessage> messagesInQueue = new ArrayList<ProtocolMessage>();
            
            do{
                System.out.println("-----------------------");
                System.out.println("Select an option: \n");
                System.out.println("1. Check Password");
                System.out.println("2. Check Permission");
                System.out.println("3. Add Password");
                System.out.println("4. Add Permission");                
                System.out.println("5. Add Role");
                System.out.println("6. Remove Permission");                
                System.out.println("7. Remove Role");    
                System.out.println("8. SEND REQUEST");
                System.out.println("9. Exit");
                System.out.println("-----------------------");
                System.out.println("Your choice: ");
                
                userInput = stdIn.readLine();

                switch(userInput){
                        
                    case "1":
                        messagesInQueue.add(getCheckPasswordRequest());
                        break;
                    
                    case "2":
                    	messagesInQueue.add(getCheckPermissionRequest());
                        break;
                    
                    case "3":
                    	messagesInQueue.add(getAddPasswordRequest());
                        break;
                        
                    case "4":
                    	messagesInQueue.add(getAddPermissionRequest());
                        break;
                    
                    case "5":
                        messagesInQueue.add(getAddRoleRequest());
                        break;
                        
                    case "6":
                        messagesInQueue.add(getRemovePermissionRequest());
                        break;
                        
                    case "7":
                        messagesInQueue.add(getRemoveRoleRequest());
                        break;
                        
                    case "8":
                    	if(messagesInQueue.size() > 0) {
                    		handleClientRequest(in, out, clientHandler, messagesInQueue);
                    		messagesInQueue = new ArrayList<ProtocolMessage>();
                    	}else {
                    		System.out.println("No messages selected so far!");
                    	}
                    	break;

                    case "9":
                        flag = false;
                        closeConnection(in, out, clientHandler);
                        System.out.println("Thank you! Bye!");
                        break;
                        
                    default:
                        System.out.println("Unrecognized option: " + userInput);
                        break;
                }
                
            }while(userInput != null && flag);
            
        } catch (UnknownHostException e) {
            System.err.println("Don't know about host " + hostName);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to " +
                hostName);
            System.exit(1);
        } catch (ClassNotFoundException e){
            System.err.println("Class not found: " + e.getMessage());
            System.exit(1);
        }
    }
}
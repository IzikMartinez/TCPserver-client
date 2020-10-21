import java.net.*;
import java.io.*;

public class SecurityServer {

    public static ServerProtocolHandler handler;
    
    public static RBACMonitor getRBACMonitor(){
    	RBACMonitor monitor = new RBACMonitor();
        
        monitor.addRole("student", "josie");
        monitor.addRole("student", "efren");
        monitor.addRole("student", "vu");

        monitor.addRole("professor", "carlos");
        monitor.addRole("professor", "dulal");
        monitor.addRole("professor", "scott");

        monitor.addRole("dean", "frank");
        
        monitor.addPermission("student",   "readGradesPermission");
        monitor.addPermission("professor", "writeGradesPermission");
        monitor.addPermission("professor", "readGradesPermission");

        
        return monitor;
    }
    
    public static PasswordTable getPasswordTable(){
        PasswordTable table = new PasswordTable();
    
        table.addPassword("carlos", "abc123");
        table.addPassword("dulal", "xyz789");
        table.addPassword("scott", "qwerty7890");
 
        return table;
    }
    
    public static void main(String[] args) throws IOException {
        
        if (args.length != 1) {
            System.err.println("Usage: java SecurityServer <port number>");
            System.exit(1);
        }
        
        int portNumber = Integer.parseInt(args[0]);

        System.out.println("Running Authorization Server....");

        try (
            ServerSocket serverSocket =
                new ServerSocket(Integer.parseInt(args[0]));
            
            Socket clientSocket = serverSocket.accept();     

            ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
            
            ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());          
            
        ) {
            Object inputObj;
            ServerProtocolHandler handler = new ServerProtocolHandler(getPasswordTable(), getRBACMonitor());
            
            while ((inputObj = in.readObject()) != null) {             

               ProtocolMessageWindow requestWindow = (ProtocolMessageWindow) inputObj;
               System.out.println("Received request: " + requestWindow);

               ProtocolMessageWindow responseWindow = handler.handleProtocolMessageWindow(requestWindow);
  
               if(responseWindow.getSize() > 0){
            	   
            	   System.out.println("Sending response: " + responseWindow);
            	   out.writeObject(responseWindow);
            	               	   
            	   if(handler.getCurrentState() == AbstractProtocolHandler.STATE_DISCONNECTED){
            		   break;
            	   }

               }
            }
        } catch (IOException e) {
            System.out.println("Exception caught when trying to listen on port "
                + portNumber + " or listening for a connection");
            System.out.println(e.getMessage());
            e.printStackTrace();
            
        } catch(ClassNotFoundException e){
            System.out.println("Exception caught when casting an unknown type..");
            System.out.println(e.getMessage());
        }
    }
}
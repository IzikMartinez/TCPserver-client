import java.util.*;

public class RBACMonitor{

    private Hashtable<String, ArrayList<String>> roleAssignmentTable;
    private Hashtable<String, ArrayList<String>> permissionAssignmentTable;
    
    public RBACMonitor(){
        this.roleAssignmentTable = new Hashtable<String, ArrayList<String>>();
        this.permissionAssignmentTable = new Hashtable<String, ArrayList<String>>();
    }
    
    public boolean hasRole(String role, String user){
        ArrayList<String> users = this.roleAssignmentTable.get(role);
        
        if(users != null){
            return users.contains(user);    
        }
        
        return false;
    }
    
    public ArrayList<String> getRoles(String user){
        ArrayList<String> result = new ArrayList<String>();
        
        Enumeration<String> keys = this.roleAssignmentTable.keys();
        
        while(keys.hasMoreElements()){
           String role = ((String) keys.nextElement());
            
            if(this.hasRole(role, user)){
                result.add(role);
            }
        }
        
        return result;
    }
    
    public boolean hasPermission(String user, String permission){
        
        ArrayList<String> roles = this.getRoles(user);
        
        for(String role: roles){
        
            ArrayList<String> permissions = this.permissionAssignmentTable.get(role);
        
            if(permissions != null){
                return permissions.contains(permission);  
            }
        }
        
        return false;
    }
    
    public void addRole(String role, String user){
        ArrayList<String> users = this.roleAssignmentTable.get(role);
        
        if(users == null){
            users = new ArrayList<String>();
            this.roleAssignmentTable.put(role, users);
        }
        
        if(!users.contains(user)){
            users.add(user);
        }
    }
    
    public void removeRole(String role, String user){
    	ArrayList<String> users = this.roleAssignmentTable.get(role);

    	if(users != null){
    		if(users.contains(user)){
    			users.remove(user);
    		}
    	}
    }

    public void addPermission(String role, String permission){
        ArrayList<String> permissions = this.permissionAssignmentTable.get(role);
        
        if(permissions == null){
            permissions = new ArrayList<String>();
            this.permissionAssignmentTable.put(role, permissions);
        }
        
        if(!permissions.contains(permission)){
            permissions.add(permission);
        }
    
    }
    
    public void removePermission(String role, String permission){
    	ArrayList<String> permissions = this.permissionAssignmentTable.get(role);
        
        if(permissions != null){
        	if(permissions.contains(permission)){
                permissions.remove(permission);
            } 
        }
    }
    
    
    public String toString(){
        return "Roles: " + this.roleAssignmentTable.toString() + "\n" + 
               "Permissions: " + this.permissionAssignmentTable.toString();
        
    }
    
}
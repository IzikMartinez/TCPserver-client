import java.util.Hashtable;

public class PasswordTable{

    private Hashtable<String, String> passwordTable;
    
    public PasswordTable(){
        this.passwordTable = new Hashtable<String, String>();
    }
    
    public boolean checkCorrectPassword(String user, String password){
        String storedPassword = this.passwordTable.get(user);
        
        if(storedPassword != null){
            return storedPassword.equals(password);
        }
        
        return false;
    }
    
    public void addPassword(String user, String password){
        
        if(user!= null && password != null){
            this.passwordTable.put(user, password);
        }
    }
        
        
    
    public String toString(){
        return this.passwordTable.toString();
    }
    
    public static void main(String args[]){
      
        PasswordTable table = new PasswordTable();
    
        table.addPassword("carlos", "abc123");
      
        System.out.println(table);
     
        System.out.println(table.checkCorrectPassword("carlos", "abc123"));
    }
}
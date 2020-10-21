import java.io.*;

public class ProtocolMessage implements Serializable{

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String header;
    private String body;
    private int sequenceNumber;
    public static int SEQUENCE_NUM_NOT_SET = -1;

    public ProtocolMessage(String header, String body){
        this.header= header;
        this.body = body;
        this.sequenceNumber = SEQUENCE_NUM_NOT_SET;
    }
    
    public ProtocolMessage(String header, String body, int sequenceNumber){
        this.header= header;
        this.body = body;
        this.sequenceNumber = sequenceNumber;
    }
    
    public String getHeader(){
        return this.header;
    }
    
    public String getBody(){
        return this.body;
    }
    
    public int getSequenceNumber() {
		return sequenceNumber;
	}

	public void setSequenceNumber(int sequenceNumber) {
		this.sequenceNumber = sequenceNumber;
	}

	public String toString(){
        return "[" + this.sequenceNumber + "|" + this.header + " | " + this.body + "]";
    }
    
	public boolean equals(Object other) {
		
		if(other instanceof ProtocolMessage) {
			ProtocolMessage otherMessage = (ProtocolMessage) other;
			
			if(this.getSequenceNumber() == otherMessage.getSequenceNumber() &&
				this.getHeader().equals(otherMessage.getHeader()) &&
				this.getBody().equals(otherMessage.getBody())) {
				
				return true;
			}
		}
		
		return false;
	}
	
    private void writeObject(ObjectOutputStream aOutputStream) throws IOException 
    {
    	aOutputStream.writeInt(this.sequenceNumber);
    	aOutputStream.writeUTF(this.header);
        aOutputStream.writeUTF(this.body);
    }
    
    private void readObject(ObjectInputStream aInputStream) throws ClassNotFoundException, IOException 
    {       
        this.sequenceNumber = aInputStream.readInt();
    	this.header = aInputStream.readUTF();
        this.body = aInputStream.readUTF();
    }
}
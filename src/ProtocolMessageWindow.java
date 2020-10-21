import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;

public class ProtocolMessageWindow implements Serializable {

	private static final long serialVersionUID = 1L;
	private ArrayList<ProtocolMessage> messages;

	public ProtocolMessageWindow() {
		this.messages = new ArrayList<ProtocolMessage>();
	}
	
	public ProtocolMessageWindow(ArrayList<ProtocolMessage> messages){
		this.messages = new ArrayList<ProtocolMessage>();
		this.addAllMessages(messages);
	}

	public int getSize(){
		return this.getMessages().size();
	}

	public void addMessage(ProtocolMessage message){
		
		this.messages.add(message);
		
		if(message.getSequenceNumber() == ProtocolMessage.SEQUENCE_NUM_NOT_SET) {
			message.setSequenceNumber(this.messages.size()-1);
		}
	}
	
	public void addMessageAt(ProtocolMessage message, int sequenceNumber) {
		
		this.messages.add(sequenceNumber, message);
		
		if(message.getSequenceNumber() == ProtocolMessage.SEQUENCE_NUM_NOT_SET) {
			message.setSequenceNumber(this.messages.size()-1);
		}
	}

	public void addAllMessages(ArrayList<ProtocolMessage> messages){
		for(ProtocolMessage message: messages) {
			this.addMessage(message);
		}
	}

	public ArrayList<ProtocolMessage> getMessages(){
		return this.messages;
	}

	public ArrayList<Integer> getMissingMessages(){
		ArrayList<Integer> result = new ArrayList<Integer>();

		for(ProtocolMessage message: this.getMessages()) {
			if(message.getHeader().equals(AbstractProtocolHandler.SIMULATED_LOST_MESSAGE)) {
				result.add(message.getSequenceNumber());
			}
		}

		return result;
	}

	public boolean checkWindowComplete(){

		if(this.getMissingMessages().size() == 0){
			return true;
		}

		return false;
	}

	public boolean isLastMessage(ProtocolMessage message){

		return message.getSequenceNumber() == (this.getSize() - 1);
	}

	private void writeObject(ObjectOutputStream aOutputStream) throws IOException{

		aOutputStream.writeInt(this.getSize());

		for(ProtocolMessage message: this.getMessages()){
			aOutputStream.writeObject(message);
		}
	}

	private void readObject(ObjectInputStream aInputStream) throws ClassNotFoundException, IOException {       

		int size = aInputStream.readInt();

		this.messages = new ArrayList<ProtocolMessage>();
		
		for(int i = 0; i < size; i++){
			ProtocolMessage message = (ProtocolMessage) aInputStream.readObject();
			this.addMessage(message);
		}
	}
	
	public void removeMessageAt(int sequenceNumber){
		if(sequenceNumber >= 0 && sequenceNumber < this.getSize()){
			this.getMessages().remove(sequenceNumber);
		}
	}
	
	public boolean equals(Object other) {
	
		if(other instanceof ProtocolMessageWindow) {
			ProtocolMessageWindow otherWindow = (ProtocolMessageWindow) other;
			
			if(this.getSize() == otherWindow.getSize()) {
				
				for(int i = 0; i < this.getSize(); i++) {
					ProtocolMessage otherMessage = otherWindow.getMessages().get(i);
					ProtocolMessage thisMessage = this.getMessages().get(i);
					
					if(!thisMessage.equals(otherMessage)) {
						return false;
					}
				}
				
				return true;
			}
		}
		
		return false;
	}
	
	public void merge(ProtocolMessageWindow otherWindow) {
    	
    	for(ProtocolMessage message: otherWindow.getMessages()){
    		
    		this.removeMessageAt(message.getSequenceNumber());
    		this.addMessageAt(message, message.getSequenceNumber());
    	}
    }
    
	
	public String toString() {
		StringBuilder builder = new StringBuilder();
		
		builder.append("(");
			
		for(ProtocolMessage message: this.getMessages()) {
			builder.append(message);
			builder.append(" ");
		}
		
		builder.append(")");
		
		return builder.toString();
	}
	

}

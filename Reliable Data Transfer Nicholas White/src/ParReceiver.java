import java.io.PrintWriter;

/*
 * Author: Wenbing Zhao
 * Last Modified: 10/4/2009
 * For EEC484 Project
 */

public class ParReceiver extends TransportLayer{
    public static final int RECEIVER_PORT = 9888;
    public static final int SENDER_PORT = 9887;

    public ParReceiver(LossyChannel lc) {
	super(lc);
    }

    public void run() {
	byte nextPacketExpected = 0;
	Packet packetReceived = new Packet();
	Packet packetToSend = new Packet();

	System.out.println("Ready to receive: ");

	while(true) {
	    int event = waitForEvent();
	    if(EVENT_PACKET_ARRIVAL == event) {
			packetReceived = receiveFromLossyChannel();
			deliverMessage(packetReceived);
			deliverMessageToFile(packetReceived);
			
			Packet packet = new Packet();
			byte[] msgToSend = ("Got your message, replying...").getBytes();
			int payloadLength = 0;
			for(int i = 0; i < msgToSend.length; i++){
				packet.payload[i] = msgToSend[i];
				payloadLength++;
			}
			packet.length = payloadLength;
		    sendToLossyChannel(packet);
		    m_wakeup = false;
	    }
	    
	}
    }
    
    // To be modified for task#5
    //
    // We simply extract the payload and display it as a string in stdout
    void deliverMessage(Packet packet) {
	byte[] payload = new byte[packet.length];
	for(int i=0; i<payload.length; i++)
	    payload[i] = packet.payload[i];
	String recvd = new String(payload);
	System.out.println("Received "+packet.length+" bytes: "
			   +new String(payload));
	System.out.println("received payload len: "+recvd.length());
    }
    
    void deliverMessageToFile(Packet packet) {
    	byte[] payload = new byte[packet.length];
    	for(int i=0; i<payload.length; i++)
    	    payload[i] = packet.payload[i];
    	String recvd = new String(payload);
    	
    	String outputFileName =  "Packet Received"+packet.toString();
		try{
		PrintWriter writer = new PrintWriter(outputFileName+".txt", "UTF-8");
		writer.println("Received "+packet.length+" bytes: "
				   +new String(payload));
		writer.println("received payload len: "+recvd.length());
		writer.close();
		
		}catch (Exception e) {}
    }

    public static void main(String args[]) throws Exception { 
	LossyChannel lc = new LossyChannel(RECEIVER_PORT, SENDER_PORT);
	lc.userDefinedLossRate = Integer.parseInt(args[0]);
	ParReceiver receiver = new ParReceiver(lc);
	lc.setTransportLayer(receiver);
	receiver.run();
    } 
}  

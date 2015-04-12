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
	packetToSend.ack = 1;
	packetToSend.seq = 0;

	System.out.println("Ready to receive: ");

	while(true) {
		System.out.println("Waiting for: Seq:"+packetToSend.seq+" and ACK:"+packetToSend.ack);
	    int event = waitForEvent();
	    if(EVENT_PACKET_ARRIVAL == event) {
			packetReceived = receiveFromLossyChannel();
			deliverMessage(packetReceived);
			System.out.println("Received: Seq:" + packetReceived.seq +" Ack:" + packetReceived.ack);
			if(packetReceived.seq == nextPacketExpected){
				deliverMessageToFile(packetReceived);
			}
			else{
				System.out.println("Packet received but was out of order...");
				System.out.println("Expecting: "+nextPacketExpected);
				System.out.println("Got: "+packetReceived.seq);
			}
			
			packetToSend.seq = packetReceived.seq;
			packetToSend.ack = increment(packetReceived.ack);
			nextPacketExpected = increment(nextPacketExpected);
			String msg = "Seq:" + packetToSend.seq + "Ack:"+packetToSend.ack;
			byte[] msgToSend = msg.getBytes();
			int payloadLength = 0;
			for(int i = 0; i < msgToSend.length; i++){
				packetToSend.payload[i] = msgToSend[i];
				payloadLength++;
			}
			packetToSend.length = payloadLength;
			
		    sendToLossyChannel(packetToSend);
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
    	
    	String outputFileName =  "OUTPUT";
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

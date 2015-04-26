/* Author: Wenbing Zhao
 * Last Modified: 10/4/2009
 * For EEC484 Project
 */

import java.io.*; 
import java.util.*;

public class ParSender extends TransportLayer{ 
    public static final int RECEIVER_PORT = 9888;
    public static final int SENDER_PORT = 9887;
    
    public String filePath;
    
    public List<String> inputLines = null;
    public int inputLinesIndex = 0;
    
    public ParSender(LossyChannel lc) 
    {
    	super(lc);
    }


   public void waitAck(byte ack,Packet sndpkt){
	   int event = waitForEvent();
	    if(EVENT_PACKET_ARRIVAL == event) {
	    	Packet packet = new Packet();
	    	packet = receiveFromLossyChannel();
	    	
	    	if(packet.ack != ack){
	    		waitAck(ack,sndpkt);
	    	}
	    	if(packet.ack == ack){
	    		waitForCallFromAbove(increment(ack));
	    	}
		// To be completed for task#2
		// PAR protocol implementation: sender side
	    }
	    else if(EVENT_TIMEOUT == event){
	    	System.out.println("Timeout, resending...");
	    	sendToLossyChannel(sndpkt);
	    	startTimer();
	    	waitAck(ack,sndpkt);

	    }
   }
   public void waitForCallFromAbove(byte sndpktSeq){
		Packet sndpkt = new Packet();
		sndpkt.seq = sndpktSeq;
		try{
			sndpkt.payload = inputLines.get(inputLinesIndex).toString().getBytes();
			sndpkt.length = inputLines.get(inputLinesIndex).toString().length();
		}
		catch(Exception e){
			System.out.printf("Done sending all '%i' message/s! \n",inputLinesIndex);
			return;
		}
		inputLinesIndex++;
		
		sendToLossyChannel(sndpkt);
    	startTimer();
    	waitAck(sndpktSeq,sndpkt);
   }
    
    public void run(){
    	getMessageToSend();
    	try{
    	waitForCallFromAbove((byte)0);
    	}
    	catch(Exception e){
    		return;
    	}
    }
    
    /*
	*	Function	:	getMessageToSendGet 
	*	Purpose		:	Gets the message or file-path from user
	*	Arguments	:	none
	*	Returns		:	byte array
    */
    void getMessageToSend() {
    	
	    System.out.println("Enter a message to send (To send a file, enter the complete path) :> ");
	    
		//Assuming the user's input is a file, try to open it...
		String userInputFromConsole = null;
		
		try {
			BufferedReader inFromUser = 
			new BufferedReader(new InputStreamReader(System.in)); 

			//Get user's input
			userInputFromConsole = inFromUser.readLine(); 
			if(null == userInputFromConsole){
				System.exit(1);
			}
			
			BufferedReader br = new BufferedReader(new FileReader(userInputFromConsole));
			filePath = userInputFromConsole;
		    try 
		    {	    	
		        StringBuilder sb = new StringBuilder();
		        String line = br.readLine();
		        inputLines = new ArrayList<String>();
		        inputLines.add(line);
		        while (line != null) {
		            sb.append(line);
		            sb.append(System.lineSeparator());
		            line = br.readLine();
		            inputLines.add(line);
	        }
		        userInputFromConsole = sb.toString();
		    } finally {
		        br.close();
		    }

		    //Tell the user we are sending the file at path 'userInputFromConsole'
		    //System.out.println("Sending: " + userInputFromConsole);
		 	//Return the contents of the file in byte form
		    
		} catch(Exception e) { //Executed when the user enters a message, not a file path
		   // System.out.printf("Sending '%s' ",userInputFromConsole);
		    inputLines = new ArrayList<String>();
		    inputLines.add(userInputFromConsole);
		    //Return the message string in byte form
		}
    }

    /*
	*	Function	:	getLossRate //Get loss rate from user using Scanner
	*	Returns		:	integer //User defined loss rate
	*	Arguments	:	none
    */
    public static int getLossRate(){
    	//Prompt
    	System.out.println("Would you like to change the loss rate? (Enter 0 for No and 1 for Yes) :> ");
    	//Create scanner and take input
	    Scanner in = new Scanner(System.in);
	    String answer = in.nextLine();
	    //If user wants to change rate...
		if(answer.equals("1"))
		{
			System.out.println("Please enter your desired loss rate as a percentage (E.g. 20 for 20%):> ");
		    answer = in.nextLine();	  
		    return Integer.valueOf(answer);
		}
		//Return 0% loss rate if user does not want to change rate
		else{
			return 0;
		}
    }

    /*
	*	Function	:	Main
	*	Returns		:	Void
	*	Arguments	:	String args[] //Command line arguments
    */
    public static void main(String args[]) throws Exception { 
    	//Instantiate lossy channel
		LossyChannel lc = new LossyChannel(SENDER_PORT, RECEIVER_PORT); //Instantiate lossy channel
		//Get packet loss rate from user
		lc.userDefinedLossRate = getLossRate();
		//Instantiate and run sender ParSender
		ParSender sender = new ParSender(lc);
		lc.setTransportLayer(sender);
		sender.run();
		System.out.printf("Done sending all messages! \n");
	} 
} 

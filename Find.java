import java.util.Vector;            
public class Find extends Algorithm {
    private int m;                   	// Ring of identifiers has size 2^m
    private int SizeRing;              // SizeRing = 2^m

    public Object run() {
        return find(getID());
    }

	// Each message sent by this algorithm has the form: flag, value, ID 
	// where:
	// - if flag = "GET" then the message is a request to get the document with the given key
	// - if flag = "LOOKUP" then the message is request to forward the message to the closest
	//   processor to the position of the key
	// - if flag = "FOUND" then the message contains the key and processor that stores it
	// - if flag = "NOT_FOUND" then the requested data is not in the system
	// - if flag = "END" the algorithm terminates
	
	/* Complete method find, which must implement the Chord search algorithm using finger tables 
	   and assumming that there are two processors in the system that received the same ring identifier. */ 
	/* ----------------------------- */
    public Object find(String id) {
	/* ------------------------------ */
        try{

       
			 /* The following code will determine the keys to be stored in this processor, the keys that this processor
			    needs to find (if any), and the addresses of the finger table                                           */
			Vector<Integer> searchKeys; 		// Keys that this processor needs to find in the P2P system. Only
				                                // for one processor this vector will not be empty
			Vector<Integer> localKeys;   		// Keys stored in this processor

			localKeys = new Vector<Integer>();
			String[] fingerTable;                  // Addresses of the fingers are stored here
			searchKeys = keysToFind();             // Read keys and fingers from configuration file
			fingerTable = getKeysAndFingers(searchKeys,localKeys,id);  // Determine local keys, keys that need to be found, and fingers
			m = fingerTable.length-1;
			SizeRing = exp(2,m);

			/* Your initialization code goes here */

			boolean shareValuesWithAddedProcessor = false;
			String result = "";
			Message mssg = null, message;

			// Only the processor that has keys needs to worry about this variable.
			// Everytime we create/send a message, this variable will be turned to true.
			// This variable will remind the processor that it has created/sent a key and must wait for a response before 
			// checking if it has more keys to search, hence if it has to repeat the same process for more keys. 
			boolean keyProcessed = false;

			// Successor of this processor in the ring of identifiers
			String succ = successor();
			// Ring identifier for this processor
			int hashID = hp(id);
			// Ring identifier for the successor of this processor
			int hashSucc = hp(succ);

			int keyValue;
			String[] data;

			// IMPORTANT -- Check if the successor of this processors has been mapped to the same Ring identifier
			if(hashID == hashSucc){
				// If this is true, then I am processor p(i) and I have split half of my values with processor p(j)
				shareValuesWithAddedProcessor = true;
			}

			//Only one processor will need to execute this condition
			// If this condition is true, the processor has keys that need to be found
			if (searchKeys.size() > 0) {
			  	
				// Grab the first key
			  	keyValue = searchKeys.elementAt(0);
			  	// Do not search for the same key twice
				searchKeys.remove(0);

				// Check if we have the key locally
				if (localKeys.contains(keyValue)) {
					// Store location of key in the result
					result = result + keyValue + ":" + id + " "; 
					keyProcessed = true;
				}

				// Otherwise, the key was not stored locally and we need to create a message to send
				else{

					// Check if the key is stored in my successor
					if(inSegment(hk(keyValue), hashID, hashSucc)){
						mssg = makeMessage (succ, pack("GET",keyValue,id));
					}
					else{
						int closest = -1;
						// Iterate through each element within the fingerTable and determine the best processor to contact
						for(int i=0; i < m ; i++){
							String fingerElementStringValue = fingerTable[i];
							// Check if the current finger table processor is the closest to the key AND is not < 0
							if(closest == -1 || hk(keyValue) - hp(fingerElementStringValue) > 0 && closest > hk(keyValue) - hp(fingerElementStringValue)){
								mssg = makeMessage (fingerElementStringValue, pack("LOOKUP",keyValue,id));
								closest = hk(keyValue) - hp(fingerElementStringValue);
							}
						}
					}
				}
			}

			// Synchronous loop
	        while (waitForNextRound()) {

	        	// SEND MESSAGES
         		if (mssg != null) {
					send(mssg);
					data = unpack(mssg.data());
					// If the message contains 'END' and there are no more keys to enquire, then return the result and terminate
					if (data[0].equals("END") && searchKeys.size() == 0) return result;
				}

				// Clear the message value
				mssg = null;

				// RECEIVE MESSAGES
				message = receive();
				// While there are still messages to unpack
				while (message != null) {
					data = unpack(message.data());

					// GET Messages
					if (data[0].equals("GET")) {
						// If this is the same GET message that this processor originally sent, then the
						// key is not in the system
						if (data[2].equals(id)) {
							result = result + data[1] + ":not found ";
							keyProcessed = true;
						}
						// This processor has the key and return FOUND to the processor which requested the key
						else if (localKeys.contains(stringToInteger(data[1]))){
							mssg = makeMessage(data[2],pack("FOUND",data[1],id));
						}
						// If this processor does NOT have the key and it HAS a processor that it shares values with, then forward the message to that processor
						else if(shareValuesWithAddedProcessor){
							mssg = makeMessage(succ,pack("GET",data[1],data[2]));
						}
						// Otherwise, the key does not exists
						else{
							mssg = makeMessage(data[2],pack("NOT_FOUND",data[1]));
						}
					}
					// LOOKUP Messages 
					else if (data[0].equals("LOOKUP")) {
						// Grab the key
					  	keyValue = stringToInteger(data[1]);

						// Check if the key is stored in the successor
						if(inSegment(hk(keyValue), hashID, hashSucc)){
							mssg = makeMessage (succ, pack("GET",keyValue,data[2]));
						}
						else{
							int closest = -1;
							// Iterate through each element within the fingerTable and determine the best processor to contact
							for(int i=0; i < m ; i++){
								String fingerElementStringValue = fingerTable[i];
								// Check if the current finger table processor is the closest to the key AND is not < 0
								if(closest == -1 || hk(keyValue) - hp(fingerElementStringValue) > 0 && closest > hk(keyValue) - hp(fingerElementStringValue)){
									mssg = makeMessage (fingerElementStringValue, pack("LOOKUP",keyValue,data[2]));
									closest = hk(keyValue) - hp(fingerElementStringValue);
								}
							}
						}
					}
					// FOUND MESSAGES
					else if (data[0].equals("FOUND")) {
						result = result + data[1] + ":" + data[2] + " ";
						keyProcessed = true;
					}
					// NOT_FOUND MESSAGES
					else if (data[0].equals("NOT_FOUND")) {
						result = result + data[1] + ":not found ";
						keyProcessed = true;
					}
					// END MESSAGES
					else if (data[0].equals("END")) 
							mssg = makeMessage(succ,"END");
					message = receive();
				}

				// If the key has been processed (Only the processor that is searching for keys will execute this part)
				if(keyProcessed){

					//Only one processor will need to execute this condition
					// If this condition is true, the processor has keys that need to be found
					if (searchKeys.size() > 0) {
					  	
						// Grab the first key
					  	keyValue = searchKeys.elementAt(0);
					  	// Do not search for the same key twice
						searchKeys.remove(0);

						// Check if we have the key locally
						if (localKeys.contains(keyValue)) {
							// Store location of key in the result
							result = result + keyValue + ":" + id + " "; 
							keyProcessed = true;
						}

						// Otherwise, the key was not stored locally and we need to create a message to send
						else{

							// Check if the key is stored in the successor
							if(inSegment(hk(keyValue), hashID, hashSucc)){
								mssg = makeMessage (succ, pack("GET",keyValue,id));
							}
							else{
								int closest = -1;
								// Iterate through each element within the fingerTable and determine the best processor to contact
								for(int i=0; i < m ; i++){
									String fingerElementStringValue = fingerTable[i];
									// Check if the current finger table processor is the closest to the key AND is not < 0
									if(closest == -1 || hk(keyValue) - hp(fingerElementStringValue) > 0 && closest > hk(keyValue) - hp(fingerElementStringValue)){
										mssg = makeMessage (fingerElementStringValue, pack("LOOKUP",keyValue,id));
										closest = hk(keyValue) - hp(fingerElementStringValue);
									}
								}
							}
						}
					}
					// Otherwise, no more keys need to be searched for
					else{
						mssg = makeMessage (succ, pack("END","",id));
					}
					keyProcessed = false;
				}
	        }
                
 
        } catch(SimulatorException e){
            System.out.println("ERROR: " + e.toString());
        }
    
        /* At this point something likely went wrong. If you do not have a result you can return null */
        return null;
    }

	/* Determine the keys that need to be stored locally and the keys that the processor needs to find.
	   Negative keys returned by the simulator's method keysToFind() are to be stored locally in this 
           processor as positive numbers.                                                                    */
	/* ---------------------------------------------------------------------------------------------------- */
	private String[] getKeysAndFingers (Vector<Integer> searchKeys, Vector<Integer> localKeys, String id) throws SimulatorException {
	/* ---------------------------------------------------------------------------------------------------- */
		Vector<Integer>fingers = new Vector<Integer>();
		String[] fingerTable;
		String local = "";
		int m;
			
		if (searchKeys.size() > 0) {
			for (int i = 0; i < searchKeys.size();) {
				if (searchKeys.elementAt(i) < 0) {   	// Negative keys are the keys that must be stored locally
					localKeys.add(-searchKeys.elementAt(i));
					searchKeys.remove(i);
				}
				else if (searchKeys.elementAt(i) > 1000) {
					fingers.add(searchKeys.elementAt(i)-1000);
					searchKeys.remove(i);
				}
				else ++i;  // Key that needs to be searched for
			}
		}
			
		m = fingers.size();
		// Store the finger table in an array of Strings
		fingerTable = new String[m+1];
		for (int i = 0; i < m; ++i) fingerTable[i] = integerToString(fingers.elementAt(i));
		fingerTable[m] = id;
	
		for (int i = 0; i < localKeys.size(); ++i) local = local + localKeys.elementAt(i) + " ";
		showMessage(local); // Show in the simulator the keys stored in this processor
		return fingerTable;
	}

    /* Hash function to map processor ids to ring identifiers. */
    /* ------------------------------- */
    private int hp(String ID) throws SimulatorException{
	/* ------------------------------- */
        return stringToInteger(ID) % SizeRing;
    }

    /* Hash function to map keys to ring identifiers */
    /* ------------------------------- */
    private int hk(int key) {
    /* ------------------------------- */
        return key % SizeRing;
    }

    /* Compute base^exponent ("base" to the power "exponent") */
    /* --------------------------------------- */
    private int exp(int base, int exponent) {
    /* --------------------------------------- */
        int i = 0;
        int result = 1;

		while (i < exponent) {
			result = result * base;
			++i;
		}
		return result;
    }

    /* Determine whether hk(value) is in (hp(ID),hp(succ)] */
    /* ---------------------------------------------------------------- */
    private boolean inSegment(int hashValue, int hashID, int hashSucc) {
    /* ----------------------------------------------------------------- */
		if (hashID == hashSucc)
			if (hashValue == hashID) return true;
			else return false;
        else if (hashID < hashSucc) 
			if ((hashValue > hashID) && (hashValue <= hashSucc)) return true;
			else return false;
		else 
			if (((hashValue > hashID) && (hashValue < SizeRing)) || 
                ((0 <= hashValue) && (hashValue <= hashSucc)))  return true;
			else return false;
    }
}

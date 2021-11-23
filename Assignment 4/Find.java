import java.util.Vector;

public class Find extends Algorithm {
	private int m; // Ring of identifiers has size 2^m
	private int SizeRing; // SizeRing = 2^m

	public Object run() {
		return find(getID());
	}

	// Each message sent by this algorithm has the form: flag, value, ID
	// where:
	// - if flag = "GET" then the message is a request to get the document with the
	// given key
	// - if flag = "LOOKUP" then the message is request to forward the message to
	// the closest
	// processor to the position of the key
	// - if flag = "FOUND" then the message contains the key and processor that
	// stores it
	// - if flag = "NOT_FOUND" then the requested data is not in the system
	// - if flag = "END" the algorithm terminates

	/*
	 * Complete method find, which must implement the Chord search algorithm using
	 * finger tables and assumming that there are two processors in the system that
	 * received the same ring identifier.
	 */
	/* ----------------------------- */
	public Object find(String id) {
		/* ------------------------------ */
		try {

			/*
			 * The following code will determine the keys to be stored in this processor,
			 * the keys that this processor needs to find (if any), and the addresses of the
			 * finger table
			 */
			Vector<Integer> searchKeys; // Keys that this processor needs to find in the P2P system. Only
										// for one processor this vector will not be empty
			Vector<Integer> localKeys; // Keys stored in this processor

			localKeys = new Vector<Integer>();
			String[] fingerTable; // Addresses of the fingers are stored here
			searchKeys = keysToFind(); // Read keys and fingers from configuration file
			fingerTable = getKeysAndFingers(searchKeys, localKeys, id); // Determine local keys, keys that need to be
																		// found, and fingers
			m = fingerTable.length - 1;
			SizeRing = exp(2, m);

			/* Your initialization code goes here */
			String succ = successor();
			String result = "";
			String[] data;
			int hashID = hp(id);
			int hashSucc = hp(succ);
			int keyValue;
			boolean keyProcessed = false;
			Message mssg = null;
			Message message;

			if (searchKeys.size() > 0) { // If this condition is true, the processor has keys that need to be found
				// Get the first key
				keyValue = searchKeys.elementAt(0);
				// Get rid off the key you just had and never see use it again
				searchKeys.removeElementAt(0);
				// Check if the key is contained locally
				if (localKeys.contains(keyValue)) {
					// If so, add the key into result and change the bool flag
					result = result + keyValue + ":" + id + " ";
					keyProcessed = true;
				} else {
					// If the key is not contained locally, check if it is contained in the
					// successor
					if (inSegment(hk(keyValue), hashID, hashSucc)) {
						// You check by sending a message to S U C C
						mssg = makeMessage(succ, pack("GET", keyValue, id));
					} else {
						// If not in S U C C you will need to itegrate through the finger table and to
						// locate the ideal processor to contact
						int intervalDistance = -1;
						for (int i = 0; i < m; i++) {
							String currentFingerValue = fingerTable[i];
							if (hk(keyValue) - hp(currentFingerValue) < intervalDistance
									&& hk(keyValue) > hp(currentFingerValue) || intervalDistance == -1) {
								intervalDistance = hk(keyValue) - hp(currentFingerValue);
								mssg = makeMessage(currentFingerValue, pack("LOOKUP", keyValue, id));
							}
						}
					}
				}
			}

			while (waitForNextRound()) { // Synchronous loop
				if (mssg != null) {
					send(mssg);
					data = unpack(mssg.data());
					// Algorithm terminates when receives "END", means there are no more keys that
					// haven't been checked
					if (data[0].equals("END") && searchKeys.size() == 0)
						return result;
				}
				mssg = null;
				message = receive();
				while (message != null) {
					data = unpack(message.data());
					if (data[0].equals("GET")) {
						// If this is the same GET message that this processor originally sent, then the
						// key is not in the system
						if (data[2].equals(id)) {
							result = result + data[1] + ":not found ";
							keyProcessed = true;
						}
						// This processor must contain the key, if it is in the system
						else if (localKeys.contains(stringToInteger(data[1]))) {
							mssg = makeMessage(data[2], pack("FOUND", data[1], id));
						} else {
							mssg = makeMessage(data[2], pack("NOT_FOUND", data[1]));
						}
					} else if (data[0].equals("LOOKUP")) {
						// Forward the request
						keyValue = stringToInteger(data[1]);
						if (inSegment(hk(keyValue), hashID, hashSucc)) {
							mssg = makeMessage(succ, pack("GET", keyValue, data[2]));
						} else {
							// If not in S U C C you will need to itegrate through the finger table and to
							// locate the ideal processor to contact
							int intervalDistance = -1;
							for (int i = 0; i < m; i++) {
								String currentFingerValue = fingerTable[i];
								if (hk(keyValue) - hp(currentFingerValue) < intervalDistance
										&& hk(keyValue) > hp(currentFingerValue) || intervalDistance == -1) {
									intervalDistance = hk(keyValue) - hp(currentFingerValue);
									mssg = makeMessage(currentFingerValue, pack("LOOKUP", keyValue, data[2]));
								}
							}
						}
					} else if (data[0].equals("FOUND")) {
						result = result + data[1] + ":" + data[2] + " ";
						keyProcessed = true;
					} else if (data[0].equals("NOT_FOUND")) {
						result = result + data[1] + ":not found ";
						keyProcessed = true;
					} else if (data[0].equals("END")) {
						mssg = makeMessage(succ, "END");
					}
					message = receive();
				}
				if (keyProcessed) {
					if (searchKeys.size() <= 0) // There are no more keys to find
						mssg = makeMessage(succ, "END");
					else {
						keyValue = searchKeys.elementAt(0);
						searchKeys.remove(0); // Do not search for same key twice
						if (localKeys.contains(keyValue)) {
							result = result + keyValue + ":" + id + " "; // Store location of key in the result
							keyProcessed = true;
						} else if (inSegment(hk(keyValue), hashID, hashSucc)) {// Check if key must be in successor
							mssg = makeMessage(succ, pack("GET", keyValue, id));
						} else {
							// If not in S U C C you will need to itegrate through the finger table and to
							// locate the ideal processor to contact
							int intervalDistance = -1;
							for (int i = 0; i < m; i++) {
								String currentFingerValue = fingerTable[i];
								if (hk(keyValue) - hp(currentFingerValue) < intervalDistance
										&& hk(keyValue) > hp(currentFingerValue) || intervalDistance == -1) {
									intervalDistance = hk(keyValue) - hp(currentFingerValue);
									mssg = makeMessage(succ, pack("LOOKUP", keyValue, id)); // Key is not in successor
								}
							}
						}
					}
					if (mssg != null)
						keyProcessed = false;
				}
			}

		} catch (SimulatorException e) {
			System.out.println("ERROR: " + e.toString());
		}

		/*
		 * At this point something likely went wrong. If you do not have a result you
		 * can return null
		 */
		return null;
	}

	/*
	 * Determine the keys that need to be stored locally and the keys that the
	 * processor needs to find. Negative keys returned by the simulator's method
	 * keysToFind() are to be stored locally in this processor as positive numbers.
	 */
	/*
	 * -----------------------------------------------------------------------------
	 * -----------------------
	 */
	private String[] getKeysAndFingers(Vector<Integer> searchKeys, Vector<Integer> localKeys, String id)
			throws SimulatorException {
		/*
		 * -----------------------------------------------------------------------------
		 * -----------------------
		 */
		Vector<Integer> fingers = new Vector<Integer>();
		String[] fingerTable;
		String local = "";
		int m;

		if (searchKeys.size() > 0) {
			for (int i = 0; i < searchKeys.size();) {
				if (searchKeys.elementAt(i) < 0) { // Negative keys are the keys that must be stored locally
					localKeys.add(-searchKeys.elementAt(i));
					searchKeys.remove(i);
				} else if (searchKeys.elementAt(i) > 1000) {
					fingers.add(searchKeys.elementAt(i) - 1000);
					searchKeys.remove(i);
				} else
					++i; // Key that needs to be searched for
			}
		}

		m = fingers.size();
		// Store the finger table in an array of Strings
		fingerTable = new String[m + 1];
		for (int i = 0; i < m; ++i)
			fingerTable[i] = integerToString(fingers.elementAt(i));
		fingerTable[m] = id;

		for (int i = 0; i < localKeys.size(); ++i)
			local = local + localKeys.elementAt(i) + " ";
		showMessage(local); // Show in the simulator the keys stored in this processor
		return fingerTable;
	}

	/* Hash function to map processor ids to ring identifiers. */
	/* ------------------------------- */
	private int hp(String ID) throws SimulatorException {
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
			if (hashValue == hashID)
				return true;
			else
				return false;
		else if (hashID < hashSucc)
			if ((hashValue > hashID) && (hashValue <= hashSucc))
				return true;
			else
				return false;
		else if (((hashValue > hashID) && (hashValue < SizeRing)) || ((0 <= hashValue) && (hashValue <= hashSucc)))
			return true;
		else
			return false;
	}
}

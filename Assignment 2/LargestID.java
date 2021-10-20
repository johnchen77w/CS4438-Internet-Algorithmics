import java.util.Vector; 

/* Algorithm for solving the leader election problem in a synchronous ring network. */

public class LargestID extends Algorithm {
    private static int leaderID = 0;

    /* Do not modify this method */
    public Object run() {
	int largest = findLargest(getID());
        return largest;
    }
 
    public int findLargest(String id) {
        Vector<String> v = neighbours(); // Set of neighbours of this node.
        String leftNeighbour = (String) v.elementAt(0);
        String rightNeighbour = (String) v.elementAt(1);

        // Your initialization code goes here
        Message mssg = makeMessage(leftNeighbour, id);
		Message m;
        String status = "unknown";
        
        try {
            while (waitForNextRound()) { // Main loop. All processors wait here for the beginning of the next round.
                // Sending phase
                if (mssg != null) {
                    send(mssg); // The destination and data is inside the Message.
                    // If we are sent an END message terminate after.
                    if (equal(mssg.data(),"END")) {
                        // ===Terminate===
                        return leaderID;
                    }
                }
                mssg = null;
                // Receiving phase
                m = receive();
                if (m != null) {
                    if (equal(m.data(),"END")) {
                        if (equal(m.source(), rightNeighbour)) {
                            mssg = makeMessage(leftNeighbour,"END");
                        } else {
                            mssg = makeMessage(rightNeighbour,"END");
                        }
                    } else {
                        if (equal(m.data(),id)) {   // We got our own message back, we must be the leader!
                            mssg = makeMessage(leftNeighbour,"END");
                            status = "Leader";
                            leaderID = stringToInteger(id);
                        } else if (larger(m.data(),id)) {
                            // mssg = makeMessage(leftNeighbour,m.data());
                            if (equal(m.source(), rightNeighbour)) {
                                mssg = makeMessage(leftNeighbour,m.data());
                            } else {
                                mssg = makeMessage(rightNeighbour,m.data());
                            }
                            if (equal(status,"unknown")) {
                                status = "Not leader";
                            }
                        } else {
                            mssg = null;
                        }
                    }
                }
            }
        } catch(SimulatorException e){
            System.out.println("ERROR: " + e.toString());
        }   
        // If we got here, something went wrong! (Exception, node failed, etc.)
		return 0;
    }
}
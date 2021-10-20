import java.util.Vector;

/* Algorithm for counting the number of processors to the left of a given processor in a line network. */

public class Position extends Algorithm {

    /* Do not modify this method */
    public Object run() {
        int pos = findPosition(getID());
        return pos;
    }

    static int count = 1;
    public int findPosition(String id) {
        Vector<String> v = neighbours(); // Set of neighbours of this node.

        String leftNeighbour = (String) v.elementAt(0);
        String rightNeighbour = (String) v.elementAt(1);

            boolean leftmostProcessor, rightmostProcessor;
            if (equal(leftNeighbour,"0")) leftmostProcessor = true;
            else leftmostProcessor = false;
            if (equal(rightNeighbour,"0")) rightmostProcessor = true;
            else rightmostProcessor = false;

        // handle the special case where no messages need to be sent (i.e. only one node in network)
        if (leftmostProcessor && rightmostProcessor) return 1;

        Message msgSent = null;
        Message msgRcvd;
        
        if (leftmostProcessor) {
            msgSent = makeMessage(rightNeighbour, id);
        }
        
        // Your initialization code goes here
        
        try {
               
            while (waitForNextRound()) { // Main loop. All processors wait here for the beginning of the next round.
                // Send phase
                if (msgSent != null) {
                    send(msgSent);
                    if (equal(msgSent.destination(), leftNeighbour)) {
                        return count;
                    }
                }
                msgSent = null;
                //Receive phase
                msgRcvd = receive();
                if (msgRcvd != null) {
                    
                    if (equal(msgRcvd.source(), leftNeighbour)) {
                        count++;
                        msgSent = makeMessage(rightNeighbour,id);
                        if (rightmostProcessor) {
                            msgSent = makeMessage(leftNeighbour,id);
                        } else if (leftmostProcessor) {
                            msgSent = makeMessage(rightNeighbour,id);
                            return count;
                        }
                    } else {
                        msgSent = makeMessage(leftNeighbour,id);
                        count --;
                        if (rightmostProcessor) {
                            msgSent = makeMessage(leftNeighbour,id);
                        } else if (leftmostProcessor) { 
                            msgSent = makeMessage(rightNeighbour,id);
                            return count - 1;
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
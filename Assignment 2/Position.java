import java.util.Vector;

/* Algorithm for counting the number of processors to the left of a given processor in a line network. */

public class Position extends Algorithm {
    private static int count = 0;

    /* Do not modify this method */
    public Object run() {
        int pos = findPosition(getID());
        return pos;
    }

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

        Message mssg = null;
        Message m;
        
        if (leftmostProcessor) {
            mssg = makeMessage(rightNeighbour, id);
            count++;
        }
        // Your initialization code goes here
        try {
            while (waitForNextRound()) { // Main loop. All processors wait here for the beginning of the next round.
                // Send phase
                if (mssg != null) {
                    send(mssg);
                    if (equal(mssg.destination(), leftNeighbour)) {
                        return count;
                    }
                    if (equal(mssg.data(), "END")) {
                        count --;
                        return count;
                    }
                }
                mssg = null;
                //Receive phase
                m = receive();
                if (m != null) {
                    if (equal(m.source(), leftNeighbour) && !equal(m.data(), "END")) {
                        mssg = makeMessage(rightNeighbour,id);
                        count++;
                        if (rightmostProcessor) {
                            mssg = makeMessage(leftNeighbour,id);
                        }
                    } else {
                        mssg = makeMessage(leftNeighbour,id);
                        count --;
                        if (leftmostProcessor) {
                            mssg = makeMessage(rightNeighbour,"END");
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

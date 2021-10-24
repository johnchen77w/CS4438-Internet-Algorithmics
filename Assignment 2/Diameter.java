import java.util.Vector;

/* Algorithm for computing the diameter of a hub-and-spoke network. */

public class Diameter extends Algorithm {
    private static int count = 0;
    private static int spoke1 = 0;
    private static int spoke2 = 0;

    /* Do not modify this method */
    public Object run() {
        int d = findDiameter(getID());
        return d;
    }

    public int findDiameter(String id) {
        Vector<String> v = neighbours(); // Set of neighbours of this node.
        Vector<Integer> d = new Vector<Integer>();
        boolean isTerminal = false;
        boolean isSpoke = false;
        boolean isHub = false;

        if (v.size() == 1) isTerminal = true;
        else isTerminal = false;
        if (v.size() == 2) isSpoke = true;
        else isSpoke = false;
        if (v.size() >= 3) isHub = true;
        else isHub = false;

        // Your initialization code goes here
        Message mssg = null;
        Message m;
        
        if (isTerminal) {
            mssg = makeMessage((String) v.elementAt(0), integerToString(count));
        }

        try {
            while (waitForNextRound()) { // Main loop. All processors wait here for the beginning of the next round.
                // Your code goes here 
		if (mssg != null) {
                    send(mssg);
                    return stringToInteger(mssg.data());
                }
                mssg = null;
                //Receive phase
                m = receive();
                if (m != null) {
                    if (isSpoke) {
                        count = stringToInteger(m.data());
                        mssg = makeMessage((String) v.elementAt(0), integerToString(count + 1));
                    } else if (isHub) {
                        d.add(stringToInteger(m.data()) + 1);
                    }
                    if (d.size() == v.size()) {
                        return d.elementAt(d.size() - 1) + d.elementAt(d.size() - 2);
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

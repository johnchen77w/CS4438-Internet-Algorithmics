import java.util.Vector;

/* Algorithm for computing the diameter of a hub-and-spoke network. */

public class Diameter extends Algorithm {

    /* Do not modify this method */
    public Object run() {
        int d = findDiameter(getID());
        return d;
    }

    public int findDiameter(String id) {
        Vector<String> v = neighbours(); // Set of neighbours of this node.
        System.out.println(v);
        // Your initialization code goes here
        
        try {
            while (waitForNextRound()) { // Main loop. All processors wait here for the beginning of the next round.
                // Your code goes here   
				

            }
        } catch(SimulatorException e){
            System.out.println("ERROR: " + e.toString());
        }
    
        // If we got here, something went wrong! (Exception, node failed, etc.)
        return 0;
    }
}

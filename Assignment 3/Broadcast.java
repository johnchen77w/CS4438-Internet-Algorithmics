import java.util.Vector; //We need this for the Vector class.

/* This class implements the flooding algorithm for broadcasting */
/* information in a synchronous network.                         */
/* Requires a root node.                                         */
public class Broadcast extends Algorithm {

    public Object run() {
        // Invoke the main algorithm, passing as parameter the node's id.
        // Our broadcast algorithm returns the message that was broadcast by the root node. 
        String lastmsg = broadcast(getID());
        return lastmsg;
    }

    public String broadcast(String id) {
        try {
            // ===Initial Setup===
            Message mssg, message;
            String status, lastmsg = null;
            int rounds_left;

            Vector<String> adjacent = neighbours(); // Set of neighbours of this node
            rounds_left = -1;

            if (equal(id, "1") || equal(id, "2")) { // If we are the root node that will start the broadcast.
                mssg = makeMessage(adjacent, "The message"); // Message to all neighbours.
                
                status = "marked";
                lastmsg = mssg.data();
                showMessage(status);
            }
            else { // If we are not the root node.
                mssg = null;
                status = "unmarked";
                showMessage(status);
            }

            // ===Main Loop===
            while (waitForNextRound()) {  // A synchronous algorithm must wait for the 
                                          // beginning of the next round before sending
                                          // messages.

                // ===Send Phase===
                if (mssg != null) {
                    send(mssg);
                    rounds_left = 1;
                }
                mssg = null;

                // ===Receive Phase===
                message = receive();
                while (message != null) {
                    if (equal(status, "unmarked")) {
                        printMessage("Message received");
                        status = "marked";
                        showMessage(status);
                        lastmsg = message.data();
                        mssg = makeMessage(adjacent, message.data());
                    }

                    message = receive();
                }

                if (rounds_left == 0){
                    // ===Terminate===
                    return lastmsg;
                } else if (rounds_left == 1) {
                    rounds_left = 0;
                }
            }

        } catch(SimulatorException e){
            System.out.println("ERROR: "+e.getMessage());
        }
        
        // If we got here, something went wrong! (Exception, node failed, etc.)
        // Return no result.
        return null;
    }
}

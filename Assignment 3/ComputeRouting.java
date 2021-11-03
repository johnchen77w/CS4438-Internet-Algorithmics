import java.time.chrono.IsoEra;

import javax.sound.midi.Receiver;
import javax.xml.crypto.Data;

public class ComputeRouting extends Algorithm {

    public Object run() {
        // Our sumTree algorithm returns the total of distances from the descendants to
        // this node
        String result = computeRoutingTable(getID());
        return result;
    }

    public String computeRoutingTable(String id) {
        try {
            RoutingTable table = new RoutingTable(id);

            /* Your initialization code goes here */
            Message mssg, message;
            String[] data;
            int rounds_left = -1;
            int count = 0;

            if (numChildren() == 0) {
                mssg = makeMessage(getParent(), table.stringRepresentation(id, table));
            } else {
                mssg = null;
            }

            while (waitForNextRound()) { /* synchronous loop */
                /* Your algorithm goes here */
                if (mssg != null) {
                    send(mssg);
                    rounds_left = 0;
                }
                // Receive message
                message = receive();
                while (message != null) {
                    data = message.data().split(" ");
                    System.out.println(message.data());
                    for (int i = 2; i < data.length; i += 2) {
                        table.addEntry(data[0], data[i]);
                    }
                    table.addEntry(data[0], data[0]);
                    count++;
                    message = receive();
                }
                if (numChildren() == count) { // All children at this rank have been added
                    if (isRoot()) { // We arrived at root
                        table.printTables();
                        return "";
                    } else { // We are not at root yet
                        mssg = makeMessage(getParent(), table.stringRepresentation(id, table)); // Send mssg up
                    }
                }
                if (rounds_left == 0) {
                    return "";
                }
            }
        } catch (SimulatorException e) {
            System.out.println("ERROR: " + e.toString());
        }
        return "";
    }
}
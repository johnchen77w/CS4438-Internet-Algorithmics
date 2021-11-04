import java.util.Vector; //We need this for the Vector class.

public class DBFS extends Algorithm {

    public Object run() {
        // Invoke the main algorithm, passing as parameter the node's id.
        String lastmsg = bfsTrees(getID());
        return lastmsg;
    }

    public String bfsTrees(String id) {
        try {
            // Two parents, two children, two messeges, etc
            /* Your initialization code goes here */
            Message mssg1 = null;
            Message mssg2 = null;
            Message message = null; // Receive
            Message ack1 = null;
            Message ack2 = null;
            String parent1 = "unknown";
            String parent2 = "unknown";
            Vector<String> children1 = new Vector<String>(); // Children list 1
            Vector<String> children2 = new Vector<String>(); // Children list 2
            String[] data; // Data transmitted each message

            Vector<String> adjacent1 = neighbours(); // Set of neighbours of the current processor for tree 1
            Vector<String> adjacent2 = neighbours(); // Set of neighbours of the current processor for tree 2

            if (equal(id, "1")) { // Root 1
                mssg1 = makeMessage(adjacent1, pack(id, "?" + " " + "1"));
                parent1 = "";
            } else if (equal(id, "2")) { // Root 2
                mssg2 = makeMessage(adjacent2, pack(id, "?" + " " + "2"));
                parent2 = "";
            } else {
                mssg1 = null;
                mssg2 = null;
                parent1 = "unknown";
                parent2 = "unknown";
            }
            ack1 = null;
            ack2 = null;
            int rounds_left_ = -1;

            while (waitForNextRound()) {
                /* Your algorithm goes here */
                if (mssg1 != null) {
                    send(mssg1);
                    rounds_left_ = 1;
                }
                if (mssg2 != null) {
                    send(mssg2);
                    rounds_left_ = 1;
                }
                if (ack1 != null) {
                    send(ack1);
                }
                if (ack2 != null) {
                    send(ack2);
                }
                mssg1 = null;
                mssg2 = null;
                ack1 = null;
                ack2 = null;

                message = receive();
                while (message != null) {
                    data = unpack(message.data());
                    if (data[1].split(" ")[0].equals("?")) { // Request for adoption
                        if (data[1].split(" ")[1].equals("1")) {
                            if (equal(parent1, "unknown")) { // Parent not set yet
                                parent1 = data[0];
                                adjacent1.remove(parent1); // Request for adoption will not be sent to parent
                                mssg1 = makeMessage(adjacent1, pack(id, "?" + " " + "1")); // Sent own adoption requests
                                                                                          // to neighbours
                                ack1 = makeMessage(parent1, pack(id, "Y" + " " + "1")); // Agree to be child of parent
                            } else {
                                adjacent1.remove(data[0]); // Do not send request for adoption for processors
                                                          // that have send to this processor an adoption request
                            }
                        } else if (data[1].split(" ")[1].equals("2")) {
                            if (equal(parent2, "unknown")) { // Parent not set yet
                                parent2 = data[0];
                                adjacent2.remove(parent2); // Request for adoption will not be sent to parent
                                mssg2 = makeMessage(adjacent2, pack(id, "?" + " " + "2")); // Sent own adoption requests
                                                                                          // to neighbours
                                ack2 = makeMessage(parent2, pack(id, "Y" + " " + "2")); // Agree to be child of parent
                            } else {
                                adjacent2.remove(data[0]); // Do not send request for adoption for processors that have
                                                          // send to this processor an adoption request
                            }
                        }
                    } else if (data[1].split(" ")[0].equals("Y")) {
                        if (data[1].split(" ")[1].equals("1")) {
                            children1.add(data[0]);
                        } else if (data[1].split(" ")[1].equals("2")) {
                            children2.add(data[0]);
                        }
                    }
                    message = receive();
                }
                if (!equal(parent1, "unknown") && !equal(parent2, "unknown")) {
                    if (rounds_left_ == 0) {
                        printParentsChildren(parent1, parent2, children1, children2);
                        return "";
                    } else if (rounds_left_ == 1) {
                        rounds_left_ = 0;
                    }
                }
            }
        } catch (SimulatorException e) {
            System.out.println("ERROR: " + e.getMessage());
        }
        return null;
    }

    /*
     * Print information about the parent and children of this processor in both BFS
     * trees
     */
    private void printParentsChildren(String parent1, String parent2, Vector<String> children1,
            Vector<String> children2) {
        String outMssg = "[" + parent1 + ":";
        for (int i = 0; i < children1.size() - 1; ++i)
            outMssg = outMssg + children1.elementAt(i) + " ";
        if (children1.size() > 0)
            outMssg = outMssg + children1.elementAt(children1.size() - 1) + "] [" + parent2 + ":";
        else
            outMssg = outMssg + "] [" + parent2 + ":";
        for (int i = 0; i < children2.size() - 1; ++i)
            outMssg = outMssg + children2.elementAt(i) + " ";
        if (children2.size() > 0)
            outMssg = outMssg + children2.elementAt(children2.size() - 1) + "]";
        else
            outMssg = outMssg + "]";
        showMessage(outMssg);
        printMessage(outMssg);
    }
}

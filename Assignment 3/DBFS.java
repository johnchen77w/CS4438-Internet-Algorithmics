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

            Vector<String> adjacent = neighbours(); // Set of neighbours of the current processor

            if (equal(id, "1")) { // Root 1
                mssg1 = makeMessage(adjacent, pack(id, "?" + " " + "1"));
                parent1 = "";
            } else if (equal(id, "2")) { // Root 2
                mssg2 = makeMessage(adjacent, pack(id, "?" + " " + "2"));
                parent2 = "";
            } else {
                mssg1 = null;
                mssg2 = null;
                parent1 = "unknown";
                parent2 = "unknown";
            }
            ack1 = null;
            ack2 = null;
            int rounds_left = -1;

            while (waitForNextRound()) {
                /* Your algorithm goes here */
                if (mssg1 != null) {
                    send(mssg1);
                    rounds_left = 1;
                } else if (mssg2 != null) {
                    send(mssg2);
                    rounds_left = 1;
                }
                if (ack1 != null) {
                    send(ack1);
                } else if (ack2 != null) {
                    send(ack2);
                }
                mssg1 = null;
                mssg2 = null;
                ack1 = null;
                ack2 = null;

                message = receive();
                while (message != null) {
                    data = unpack(message.data());
                    if (data[1].split(" ")[1].equals("1")) { // Tree 1
                        if (data[1].split(" ")[0].equals("?")) { // Request for adoption
                            if (equal(parent1, "unknown")) {
                                parent1 = data[0];
                                adjacent.remove(parent1);
                                mssg1 = makeMessage(adjacent, pack(id, "?" + " " + "1"));
                                ack1 = makeMessage(parent1, pack(id, "Y" + " " + "1"));
                            } else {
                                adjacent.remove(data[0]);
                            }
                        } else if (data[1].split(" ")[0].equals("Y")) {
                            children1.add(data[0]);
                        }
                    } else if (data[1].split(" ")[1].equals("2")) { // Tree 2
                        if (data[1].split(" ")[0].equals("?")) { // Request for adoption
                            if (equal(parent1, "unknown")) {
                                parent2 = data[0];
                                adjacent.remove(parent2);
                                mssg2 = makeMessage(adjacent, pack(id, "?" + " " + "2"));
                                ack2 = makeMessage(parent2, pack(id, "Y" + " " + "2"));
                            } else {
                                adjacent.remove(data[0]);
                            }
                        } else if (data[1].split(" ")[0].equals("Y")) {
                            children2.add(data[0]);
                        }
                    }
                    message = receive();
                }
                if (rounds_left == 0) {
                    printParentsChildren(parent1, parent2, children1, children2);
                    return "";
                } else if (rounds_left == 1) {
                    rounds_left = 0;
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




// import java.util.Vector; //We need this for the Vector class.

// public class DBFS extends Algorithm {

// public Object run() {
// // Invoke the main algorithm, passing as parameter the node's id.
// String lastmsg = bfsTrees(getID());
// return lastmsg;
// }

// public String bfsTrees(String id) {
// try {
// /* Your initialization code goes here */
// Message mssg = null;
// // Message mssg2 = null;
// Message message, ack;
// String parent1 = "unknown";
// String parent2 = "unknown";

// boolean isRoot1, isRoot2;
// if (equal(id, "1"))
// isRoot1 = true;
// else
// isRoot1 = false;
// if (equal(id, "2"))
// isRoot2 = true;
// else
// isRoot2 = false;

// Vector<String> children1 = new Vector<String>();
// Vector<String> children2 = new Vector<String>();

// String[] data;

// Vector<String> adjacent = neighbours();

// if (isRoot1) {
// mssg = makeMessage(adjacent, pack(id, "?" + " " + "1"));
// parent1 = "";
// } else if (isRoot2) {
// mssg = makeMessage(adjacent, pack(id, "?" + " " + "2"));
// parent2 = "";
// } else {
// mssg = null;
// // mssg2 = null;
// parent1 = "unknown";
// parent2 = "unknown";
// }
// ack = null;
// int rounds_left = -1;

// while (waitForNextRound()) {
// if (mssg != null) {
// send(mssg);
// rounds_left = 1;
// }
// if (ack != null)
// send(ack);
// mssg = null;
// // mssg2 = null;
// ack = null;
// // Receive message
// message = receive();
// while (message != null) {
// data = unpack(message.data());
// if (data[1].split(" ")[0].equals("?")) {
// if (equal(parent1, "unknown") && data[1].split(" ")[1].equals("1")) {
// parent1 = data[0];
// adjacent.remove(parent1);
// mssg = makeMessage(adjacent, pack(id, "?" + " " + "1"));
// ack = makeMessage(parent1, pack(id, "Y" + " " + "1"));
// }
// else if (equal(parent2, "unknown") && data[1].split(" ")[1].equals("2")) {
// parent2 = data[0];
// adjacent.remove(parent2);
// mssg = makeMessage(adjacent, pack(id, "?" + " " + "2"));
// ack = makeMessage(parent2, pack(id, "Y" + " " + "2"));
// }
// else {
// adjacent.remove(data[0]);
// }
// }
// if (data[1].split(" ")[0].equals("Y") && data[1].split(" ")[1].equals("1"))
// children1.add(data[0]);
// if (data[1].split(" ")[0].equals("Y") && data[1].split(" ")[1].equals("2"))
// children2.add(data[0]);
// message = receive();
// }

// if (rounds_left == 0) {
// printParentsChildren(parent1, parent2, children1, children2);
// if (/* Termination cond. */) {
// // return "";
// }
// } else if (rounds_left == 1) {
// rounds_left = 0;
// }
// }

// } catch (SimulatorException e) {
// System.out.println("ERROR: " + e.getMessage());
// }
// return null;
// }

// /*
// * Print information about the parent and children of this processor in both
// BFS
// * trees
// */
// private void printParentsChildren(String parent1, String parent2,
// Vector<String> children1,
// Vector<String> children2) {
// String outMssg = "[" + parent1 + ":";
// for (int i = 0; i < children1.size() - 1; ++i)
// outMssg = outMssg + children1.elementAt(i) + " ";
// if (children1.size() > 0)
// outMssg = outMssg + children1.elementAt(children1.size() - 1) + "] [" +
// parent2 + ":";
// else
// outMssg = outMssg + "] [" + parent2 + ":";
// for (int i = 0; i < children2.size() - 1; ++i)
// outMssg = outMssg + children2.elementAt(i) + " ";
// if (children2.size() > 0)
// outMssg = outMssg + children2.elementAt(children2.size() - 1) + "]";
// else
// outMssg = outMssg + "]";
// showMessage(outMssg);
// printMessage(outMssg);
// }
// }

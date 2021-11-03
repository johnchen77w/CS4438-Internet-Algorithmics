import java.util.*;

public class RoutingTable {

    /* These two Lists store the routing tables of all processors */
	private static List<String> tableID = Collections.synchronizedList(new ArrayList<String>());
	private static List<RTable> tableLink = Collections.synchronizedList(new ArrayList<RTable>());
	private static int numTables = 0;  /* Total number of routing tables or processors in the system */
	
	private String processorID;
	private int processorIndex; /* Index of the routing table of this processor */
	
	public RoutingTable(String id) {
		tableID.add(id);
		RTable newTable = new RTable();
		tableLink.add(newTable);        /* Save the routint able for this processor */
		processorID = id;
		processorIndex = numTables;     /* Remember position of the routing table of this processor in the List of routin tables */
		++numTables;
	}
	
	/* Receives as parameter a string containint a processor ID and its routing table. It returns
       the number of entries in the routing table */
	public int numEntries(String message) {
		StringTokenizer tokens = new StringTokenizer(message);
		return (tokens.countTokens() - 1)/2; /* do not count the processor'd ID */
	}
	
	/* Receives as parameter a string encoding a processor ID and its routing table. 
	   It returns the destination in the entry of the routing table specified by the  
	   second parameter.                                                      */
	public String getDestination(String message, int index) {
		StringTokenizer tokens = new StringTokenizer(message);
		String token;
		for (int i = 0; i < 2*(index+1); ++i) token = tokens.nextToken();
		return tokens.nextToken();
	}
	
	/* Receives as parameter a message with a processor ID and a routing table. Returns the
	   processor ID; this is the next hop for all addresses in the routing table.           */
	public String getProcessor(String message) {
		StringTokenizer tokens = new StringTokenizer(message);	
		if (tokens.countTokens() == 0) {
			System.out.println("Error. The message used to extract the next hop is empty");
			return "";
		}
		return tokens.nextToken();
	}

	/* Receives as parameter a message with a processor ID and a routing table. Returns true
	   if the routing table is empty; returns false otherwise.           */	
	public boolean emptyRoutingTable(String message) {
		StringTokenizer tokens = new StringTokenizer(message);	
		if (tokens.countTokens() < 2) return true;
		else return false;
	}
	
	/* Adds to the current routing table an entry containing the given next hop and destination */
	public void addEntry(String nextHop, String destination) {
		tableLink.get(processorIndex).addEntry(nextHop,destination);
	}
	
	/* Converts the given routing table to a String representation and prepends to it the id received in the first parameter */
	public String stringRepresentation(String id, RoutingTable table) {
		String result = id;
		RTable t = tableLink.get(table.processorIndex);
		for (int i = 0; i < t.getNumEntries(); ++i) 
			result = result + " " + t.getHop(i) + " " + t.getDestination(i);
		return result;
	}
	
	/* Prints all the routing tables */
	public void printTables() {
		System.out.println("ROUTING TABLES");
		System.out.println("--------------");
		for (int i = 0; i < numTables; ++i) {
			System.out.println("Routing table for processor " + tableID.get(i));
			tableLink.get(i).printTable();
			System.out.println(" ");
		}
	}
}
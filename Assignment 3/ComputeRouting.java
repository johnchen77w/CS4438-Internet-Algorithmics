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
			Message mssg = null;
			Message message = null;
			int count = 0;
			// Start at leaf
			if (numChildren() == 0 && !isRoot())
				mssg = makeMessage(getParent(), table.stringRepresentation(id, table));
			else
				mssg = null;

			while (waitForNextRound()) {
				if (mssg != null)
					send(mssg);
				mssg = null;
				// All children of this level has been added
				if (count == numChildren()) {
					if (isRoot()) // If at root, return table and terminate
						table.printTables();
					else // If not at root, send message up to parent
						mssg = makeMessage(getParent(), table.stringRepresentation(id, table));
					return ""; // For simulator
				}
				// Receive message
				message = receive();
				while (message != null) {
					count++;
					// Sender info added, hop and destination are the same
					table.addEntry(message.source(), message.source());
					if (table.emptyRoutingTable(message.data()) && !isRoot()) // Received empty table from leaf
						mssg = makeMessage(getParent(), table.stringRepresentation(id, table));
					else { // Received non-empty table somewhere above the leaf
							// Iterate the message and populate our own table
						for (int i = 0; i < table.numEntries(message.data()); i++)
							// Keeping the source as destination but next hop set to our source
							table.addEntry(message.source(), table.getDestination(message.data(), i));
						if (!isRoot()) // If not at root, send message up to parent
							mssg = makeMessage(getParent(), table.stringRepresentation(id, table));
					}
					message = receive(); // Receive message (again)
				}
			}
		} catch (SimulatorException e) {
			System.out.println("ERROR: " + e.toString());
		}
		return "";
	}
}

package de.haw.ttvp.chord;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import de.uniba.wiai.lspi.chord.com.CommunicationException;
import de.uniba.wiai.lspi.chord.com.Node;
import de.uniba.wiai.lspi.chord.data.ID;
import de.uniba.wiai.lspi.chord.data.URL;
import de.uniba.wiai.lspi.chord.service.Chord;
import de.uniba.wiai.lspi.chord.service.impl.ChordImpl;

/** This class is copied from de.uniba.wiai.lspi.chord.console.command to modify it's behavior
 * 
 * @author sven
 * @version 1.0.5
 */
public final class RemoteChordNetworkAccess {
	private static final Logger LOG = Logger.getLogger(RemoteChordNetworkAccess.class);

	int protocolType = URL.SOCKET_PROTOCOL;
	
	/**
	 * Invisible constructor.
	 */
	private RemoteChordNetworkAccess() {
		/*
		 * nothing to do here. 
		 */
	}

	/**
	 * this is a singleton.
	 */
	private static final RemoteChordNetworkAccess uniqueInstance = new RemoteChordNetworkAccess();

	/**
	 * Provides unique instance of <code>RemoteChordNetworkAccess</code>.
	 * 
	 * @return Singleton instance of <code>RemoteChordNetworkAccess</code>.
	 */
	public static RemoteChordNetworkAccess getUniqueInstance() {
		return uniqueInstance;
	}

	/**
	 * contains one instance of a remote chord node
	 */
	private Chord chordInstance = null;

	/**
	 * Join a remote chord network with help of the provided
	 * <code>bootstrapURL</code>. <code>port</code> must be a valid port
	 * number.
	 * 
	 * @param bootstrapURL
	 * @param port
	 * @throws Exception
	 */
	public void join(URL bootstrapURL, int port) throws Exception {
		if (chordInstance != null) {
			throw new Exception("Already joined chord network!");
		}
		chordInstance = new ChordImpl();
    chordInstance.setCallback(new CallbackHandler()); //register our callback handler
		URL acceptIncomingConnections = null;
		try {
                        //determine how to obtain ip-address on linux system. see bug 1510537. sven
			String host = java.net.InetAddress.getLocalHost().getHostAddress();
			if ((port <= 0) || (port >= 65536)) {
				acceptIncomingConnections = new URL(
						URL.KNOWN_PROTOCOLS.get(this.protocolType) + "://" + host
								+ "/");
			} else {
				acceptIncomingConnections = new URL(
						URL.KNOWN_PROTOCOLS.get(this.protocolType) + "://" + host
								+ ":" + port + "/");
			}
		} catch (Exception e) {
			throw new Exception("Could not create url for this host!", e);
		}
		try {
			if (bootstrapURL == null) {
				chordInstance.create(acceptIncomingConnections);
			} else {
				chordInstance.join(acceptIncomingConnections, bootstrapURL);
			}
		} catch (Exception e) {
			/*
			 * join/create failed. Set instance to null, so that we can try
			 * again.
			 */
			chordInstance.leave();
			chordInstance = null;
			throw e;
		}
	}

	/**
	 * Leaves the remote chord network.
	 * 
	 * @throws Exception
	 */
	public void leave() throws Exception {
		if (this.chordInstance == null) {
			/*
			 * Nothing to do here.
			 */
			return;
		}
		Chord chord = this.chordInstance;
		this.chordInstance = null;
		chord.leave();
	}

	/**
	 * @return Returns the chordInstance.
	 */
	public Chord getChordInstance() {
		return this.chordInstance;
	}
	
	/**
	 * <strong>Find Nodes</strong><br>
	 * Find all Nodes currently present in Chord-Network
	 * @return List of Nodes
	 */
	public List<ID> findNodes(){
		LOG.info("Fetching all Nodes in Chord Network");
		
		// Fetch Chord Instance
		ChordImpl chord = (ChordImpl) this.chordInstance;
		
		// This Nodes Information
		ID ownId = chord.getID();
		Node node = chord.getLocalNode();
		
		// List of Nodes in Chord-Network
		List<ID> nodes = new ArrayList<ID>();
		try {
			findNextSuccessor(ownId, nodes, node, ownId);
		} catch (CommunicationException e) {
			LOG.error("ERROR: CommunicationException: "+e.getLocalizedMessage(), e);
		}
		
		// Return List of Nodes in Chord-Network
		return nodes;
	}
	
	/**
	 * <strong>Find Next Successor</strong><br>
	 * Recursive Method to fetch all ChordNodes Successors currently present in the Network
	 * Terminates if next Successor is Origin-Node or next Successor is already known
	 * @param current
	 * @param nodes
	 * @param node
	 * @param source
	 * @throws CommunicationException
	 */
	private void findNextSuccessor(ID current, List<ID> nodes, Node node, ID source) throws CommunicationException {
		LOG.info("Recursive Call to find next Successor for Node: "+current.toString()+" with Origin: "+source.toString());

		// Find Successor for current Node
		Node nextNode = node.findSuccessor(current.add(1));
		ID next = nextNode.getNodeID();
		
		// Recursive Call if next Node is not this Node and next Node is not yet 
		if(!next.equals(source)){
			nodes.add(next);
			findNextSuccessor(next, nodes, nextNode, source);
		} else LOG.info("Successor Discorvery detected Nodes: "+nodes.size());
		
	}
	
	
}

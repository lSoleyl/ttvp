package de.haw.ttvp.chord;

import java.net.MalformedURLException;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import de.uniba.wiai.lspi.chord.com.Broadcast;
import de.uniba.wiai.lspi.chord.com.CommunicationException;
import de.uniba.wiai.lspi.chord.com.Entry;
import de.uniba.wiai.lspi.chord.com.Node;
import de.uniba.wiai.lspi.chord.com.RefsAndEntries;
import de.uniba.wiai.lspi.chord.data.ID;
import de.uniba.wiai.lspi.chord.data.URL;

public class NodeImpl extends Node {
	private static final Logger LOG = Logger.getLogger(NodeImpl.class);
	
	NodeImpl(String url) throws MalformedURLException {
		this.nodeURL = new URL(url);
		this.nodeID = new ID(this.nodeURL.toString().getBytes());
	}

	@Override
	public Node findSuccessor(ID key) throws CommunicationException {
		LOG.debug("findSuccessor called with ID: "+key.toString());
		
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Node> notify(Node potentialPredecessor)
			throws CommunicationException {
		LOG.debug("notify called with potentialPredecessor: "+potentialPredecessor.toString());
		
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RefsAndEntries notifyAndCopyEntries(Node potentialPredecessor)
			throws CommunicationException {
		LOG.debug("notifyAndCopyEntries called with potentialPredecessor: "+potentialPredecessor.toString());
		
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void ping() throws CommunicationException {
		LOG.debug("ping called");
		
		// TODO Auto-generated method stub
		
	}

	@Override
	public void insertEntry(Entry entryToInsert) throws CommunicationException {
		LOG.debug("insertEntry called with entryToInsert: "+entryToInsert.toString());
		
		// TODO Auto-generated method stub
		
	}

	@Override
	public void insertReplicas(Set<Entry> entries)
			throws CommunicationException {
		LOG.debug("insertReplicas called with number of entries: "+entries.size());
		
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeEntry(Entry entryToRemove) throws CommunicationException {
		LOG.debug("removeEntry called with entryToRemove: "+entryToRemove.toString());
		
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeReplicas(ID sendingNode, Set<Entry> replicasToRemove)
			throws CommunicationException {
		LOG.debug("removeReplicas called with sendingNode ID: "+sendingNode.toString()+" and number of replicasToRemove: "+replicasToRemove.size());
		
		// TODO Auto-generated method stub
		
	}

	@Override
	public Set<Entry> retrieveEntries(ID id) throws CommunicationException {
		LOG.debug("retrieveEntries called with ID: "+id.toString());
		
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void leavesNetwork(Node predecessor) throws CommunicationException {
		LOG.debug("leavesNetwork called with ID: "+predecessor.toString());
		
		// TODO Auto-generated method stub
		
	}

	@Override
	public void disconnect() {
		LOG.debug("disconnect called with ID");
		
		// TODO Auto-generated method stub
		
	}

	@Override
	public void broadcast(Broadcast info) throws CommunicationException {
		LOG.debug("broadcast called with Info: "+info.toString());
		
		// TODO Auto-generated method stub
		
	}

}

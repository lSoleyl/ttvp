package de.haw.ttvp.chord;

import de.uniba.wiai.lspi.chord.data.URL;
import de.uniba.wiai.lspi.chord.service.Chord;
import de.uniba.wiai.lspi.chord.service.impl.ChordImpl;

/** This class is copied from de.uniba.wiai.lspi.chord.console.command to modify it's behavior
 * 
 * @author sven
 * @version 1.0.5
 */
public final class RemoteChordNetworkAccess {

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
}

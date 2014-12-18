package de.haw.ttvp;

import de.haw.ttvp.chord.RemoteChordNetworkAccess;
import de.haw.ttvp.gamelogic.Game;
import de.uniba.wiai.lspi.chord.data.URL;
import de.uniba.wiai.lspi.chord.service.Chord;
import de.uniba.wiai.lspi.chord.service.PropertiesLoader;
import de.uniba.wiai.lspi.chord.service.impl.ChordImpl;
import org.apache.log4j.Logger;

public class Main {
	private static final Logger LOG = Logger.getLogger(Main.class);
	
	static final String URLPrefix = "ocsocket://";
  static final int ListenPort = 4245;
	
  /** Anwendung wird wie folgt gestartet:
   * - server [port=4245]
   * - client ip [port=4245]
   */
	public static void main(String[] args) throws Exception {
		LOG.info("Starting Test-Application ...");
    String ip = (args.length >= 1) ? args[0] : null;
    String port = (args.length >= 2) ? args[1] : ""+ListenPort;
    URL connectURL = null;
    
    if (ip != null) {
      connectURL = new URL(URLPrefix + ip + ":" + port + "/");
      LOG.info("Joining chord network at "+ connectURL.toString());
    } else {
      LOG.info("Starting new chord Network.");
    }
      
		
		// Loading Chord.properties File
		PropertiesLoader.loadPropertyFile();
    RemoteChordNetworkAccess network = RemoteChordNetworkAccess.getUniqueInstance();
    network.join(connectURL, ListenPort);
    
    Chord chord = network.getChordInstance();
    
    Game game = new Game(chord);
    game.start();
    
    //TODO this will be send whenever someone retrieves a key from our keyspace
    //     the targetID will then be the retrieved keyID
    //chord.broadcast(null, Boolean.TRUE);
	}

}

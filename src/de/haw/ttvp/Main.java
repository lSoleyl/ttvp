package de.haw.ttvp;

import de.haw.ttvp.chord.RemoteChordNetworkAccess;
import de.uniba.wiai.lspi.chord.data.URL;
import de.uniba.wiai.lspi.chord.service.PropertiesLoader;
import org.apache.log4j.Logger;

public class Main {
	private static final Logger LOG = Logger.getLogger(Main.class);
	
	static final String URLPrefix = "ocsocket://";
  static final int ListenPort = 4245;
	
	public static void main(String[] args) throws Exception {
		LOG.info("Starting Test-Application ...");
    String ip = (args.length >= 1) ? args[1] : null;
    String port = (args.length >= 2) ? args[2] : ""+ListenPort;
    URL connectURL = null;
    
    if (ip != null) {
      connectURL = new URL(URLPrefix + ip + ":" + port + "/");
      LOG.info("Joining chord network at "+ connectURL.toString());
    } else {
      LOG.info("Starting new chord Network.");
    }
      
		
		// Loading Chord.properties File
		PropertiesLoader.loadPropertyFile();
    RemoteChordNetworkAccess chord = RemoteChordNetworkAccess.getUniqueInstance();
    chord.join(connectURL, ListenPort);

	}

}

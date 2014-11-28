package de.haw.ttvp;

import java.net.MalformedURLException;

import de.haw.ttvp.chord.NodeImpl;
import de.uniba.wiai.lspi.chord.com.Endpoint;
import de.uniba.wiai.lspi.chord.service.PropertiesLoader;

import org.apache.log4j.Logger;

public class Main {
	private static final Logger LOG = Logger.getLogger(Main.class);
	
	static final String URL1 = "ocsoket://localhost:4245/";
	
	public static void main(String[] args) {
		LOG.info("Starting Test-Application ...");
		
		// Loading Chord.properties File
		PropertiesLoader.loadPropertyFile();
		
		try {
			NodeImpl node = new NodeImpl(URL1);
			Endpoint ep = Endpoint.createEndpoint(node, node.getURL());
			ep.listen();
			ep.acceptEntries();
		} catch(MalformedURLException e){
			LOG.error("ERROR: MalformedURLException: "+e.getLocalizedMessage(), e);
			
			// TODO do something
		}
		

	}

}

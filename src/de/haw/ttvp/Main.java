package de.haw.ttvp;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.log4j.Logger;

import de.haw.ttvp.chord.RemoteChordNetworkAccess;
import de.haw.ttvp.gamelogic.Game;
import de.uniba.wiai.lspi.chord.data.URL;
import de.uniba.wiai.lspi.chord.service.Chord;
import de.uniba.wiai.lspi.chord.service.PropertiesLoader;

public class Main {
	private static final Logger LOG = Logger.getLogger(Main.class);
	
	private static final String URLPrefix = "ocsocket://";
	private static final Integer DefaultListenPort = 4245;
	
	// Chord Bootstrap Parameters
	private static String ip;
	private static String port;
	private static URL connectURL;
	
	/**
	 * Anwendung wird wie folgt gestartet:
	 * - server [port=4245]
	 * - client ip [port=4245]
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		LOG.info("Starting Test-Application ...");
		
		// Initialize Chord Game
		boolean success = init(args);
		
		if (success) {
			LOG.info("Chord Game completed successfully");
			System.exit(0);
		} else {
			LOG.info("Chord Game failed -> Exiting");
			System.exit(2);
		}
	}
	
	/**
	 * <strong>Initialize</strong><br>
	 * @param args
	 * @return success
	 * @throws Exception
	 */
	public static boolean init(String[] args) throws Exception{
		// Define Options for CommandLine Input
		Options opts = new Options();
		
		opts.addOption("ip", true, "Chord Network Bootstrap-Address\nIf not provided, new Chord-Network will be started");
		opts.addOption("port", true, "Chord Network Bootstrap-Port\nDefault: "+DefaultListenPort);
		opts.addOption("debug", false, "Reserved for Debug-Functionality");
		opts.addOption("help", false, "Print usage");
		
		// Parse Arguments
		CommandLine cliParser = new GnuParser().parse(opts, args);
		
		if (cliParser.hasOption("help")) {
			printUsage(opts);
			return false;
		}
		
		if (cliParser.hasOption("debug")) {
			//TODO Reserved for DEBUG functionality
		}
		
		// Parse Port from CommandLine or use default
		if(cliParser.hasOption("port")){
			port = cliParser.getOptionValue("port");
		} else port = DefaultListenPort.toString();
		
		LOG.debug("Port set to: "+port);
		
		// Parse Bootstrap-Address
		if(cliParser.hasOption("ip")){
			ip = cliParser.getOptionValue("ip");
			connectURL = new URL(URLPrefix + ip + ":" + port + "/");
			
			LOG.info("Joining chord network at "+ connectURL.toString());
		} LOG.info("Starting new chord Network.");
		
		// Loading Chord.properties File
		PropertiesLoader.loadPropertyFile();
	    RemoteChordNetworkAccess network = RemoteChordNetworkAccess.getUniqueInstance();
	    network.join(connectURL, DefaultListenPort);
	    
	    Chord chord = network.getChordInstance();
	    
	    Game game = new Game(chord);
	    game.start();
	    
	    //TODO this will be send whenever someone retrieves a key from our keyspace
	    //     the targetID will then be the retrieved keyID
	    //chord.broadcast(null, Boolean.TRUE);
		
	    // Finally return SUCCESSFUL
	    return true;
	}
	
	/**
	 * <strong>Print Usage</strong><br>
	 * Prints CommandLine Options
	 * @param opts
	 */
	private static void printUsage(Options opts) {
		new HelpFormatter().printHelp("Chord Game", opts);
	}
	
	

}

package de.haw.ttvp.gamelogic;

import de.haw.ttvp.gamelogic.player.*;
import de.uniba.wiai.lspi.chord.com.CommunicationException;
import de.uniba.wiai.lspi.chord.com.Node;
import de.uniba.wiai.lspi.chord.data.ID;
import de.uniba.wiai.lspi.chord.service.Chord;
import de.uniba.wiai.lspi.chord.service.impl.ChordImpl;
import java.util.Map.Entry;
import org.apache.log4j.Logger;

public class NodeCrawler extends Thread {
  public static final int FAIL_LIMIT = 10;
  public static final int SUSPEND_TIME = 100; //ms
  
  private final ChordImpl chord;
  private final static Logger log = Logger.getLogger(NodeCrawler.class);
  private int failcount;
  
  public NodeCrawler(Chord chord) {
    this.chord = (ChordImpl)chord;
    this.failcount = 0;
  }
  
  @Override
  public void run() {
    log.info("starting crawler");
    Node localNode = chord.getLocalNode();
    ID predecessor = localNode.getNodeID();
    ID current = predecessor.add(1);    
    
    while(!Game.instance.self.known().getInterval().contains(current)) {
      try {
        log.debug("calling findSuccessor(" + current + ")");
        Node remote = localNode.findSuccessor(current);
        ID nodeID = remote.getNodeID();
        log.debug("findSucessor() returned new node with ID: " + nodeID);
       
        //Neuen Spieler hinzufügen/bekannten updaten
        Game.instance.addKnownPlayer(nodeID, new IDInterval(predecessor, nodeID));
        
        predecessor = nodeID;
        current = nodeID.add(1);
      } catch (CommunicationException ex) { //lookup ist fehlgeschlagen
        log.info("Retrieving successor failed!");
        current = current.add(1); 
        if (++failcount >= FAIL_LIMIT) {
          log.warn("findSuccessor() failed too many times, suspending crawler...");
          failcount = 0;
          try {
            current = predecessor;
            Thread.sleep(SUSPEND_TIME);
          } catch (InterruptedException e) {}
        }
      } 
    }
    log.debug("crawler found all nodes");
    Game.instance.reapplyHistory();
    
    //Prüfen, ob nun alle Nodes "known" sind, was eigentlich der Fall sein sollte.
    //Ansonsten haben wir Knoten in der Liste, die nicht im Ring existieren
    checkNodes();
    log.info("crawler finished");
  }

  private void checkNodes() {
    for (Entry<ID,Player> entry: Game.instance.playerMap.entrySet()) {
      if (entry.getValue() instanceof UnknownPlayer) {
        log.error("Found unknown Player (after crawling!) @ Node:" + entry.getKey());
      }
    }
  }
}

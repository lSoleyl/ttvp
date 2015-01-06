package de.haw.ttvp.gamelogic;

import de.haw.ttvp.gamelogic.History.HistoryEntry;
import de.haw.ttvp.gamelogic.player.*;
import de.uniba.wiai.lspi.chord.data.ID;
import de.uniba.wiai.lspi.chord.service.Chord;
import de.uniba.wiai.lspi.chord.service.impl.ChordImpl;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import javax.swing.JOptionPane;
import org.apache.log4j.Logger;



public class Game {
  public static final boolean USE_NODE_CRAWLER = true;
  public static final int INTERVALS = 100;
  public static final int SHIPS = 10;
  public static Game instance = null;
  
  private static final Logger log = Logger.getLogger(Game.class);
  private boolean ready = false;
  private final Semaphore readyLock = new Semaphore(0);
  
  public final Map<ID, Player> playerMap = new ConcurrentHashMap<>(); //Wird von dem Crawler-Thread mitbeschrieben
  public Player self;
  public final History history = new History();
  
  private final Chord chord;

  private boolean reapplyHistory = false; //Wird vom Crawler auf true gesetzt, wenn er fertig ist
  
  public Game(Chord network) {
    this.chord = network;
    Game.instance = this;
  }
  
  /** Returns a player object for the given node id.
   *  If the player isn't already known, then a new unknown player is created
   *  and inserted into the player map.
   * 
   * @param nodeID the ID of the player's node
   * 
   * @return a player object.
   */
  public Player getPlayer(ID nodeID) {
    if (self.getID().equals(nodeID))
      return self;
    
    if (playerMap.containsKey(nodeID))
      return playerMap.get(nodeID);
    
    //Neuer Spieler entdeckt
    Player p = new UnknownPlayer(nodeID);
    playerMap.put(nodeID, p);
    return p;    
  }
  
  /** Called by the node crawler during discovery.
   */
  public void addKnownPlayer(ID nodeID, IDInterval range) {
    if (playerMap.containsKey(nodeID)) {
      Player p = playerMap.get(nodeID);
      if (!p.isKnown()) { //Wenn es sich um einen UnknownPlayer handelt, dann kovertieren
        playerMap.put(nodeID, p.makeKnown(range));
      }
    } else { //Neuen Spieler anlegen.
      playerMap.put(nodeID, new KnownPlayer(nodeID, range));
    }
  }
  
  public void start() {
    if (JOptionPane.showConfirmDialog(null, "Start Game?", "User action", JOptionPane.YES_NO_OPTION) != 0) {
      log.warn("Game start aborted.");
      return;
    }
    
    try {
      if (USE_NODE_CRAWLER)
        new NodeCrawler(chord).start();
      
      IDInterval idRange = getNodeRange();
      self = createSelfPlayer(idRange);
      distributeShips(idRange);      
      setReady();
      
      if (isBeginningPlayer(idRange)) {
        log.info("I am staring the Game in 3 seconds...");
        try {
          Thread.sleep(3000);
        } catch (InterruptedException ex) {}
        //TODO ersten Schuss abgegben
      }
    } catch (GameError e) {
      log.error("Game aborted!\n", e);
    }
  }
  
  private boolean isBeginningPlayer(IDInterval range) {
    BigInteger maxID = BigInteger.valueOf(2).pow(160).subtract(BigInteger.ONE);
    return range.getIntervalID(ID.valueOf(maxID)) != null;      
  }
  
  private IDInterval getNodeRange() throws GameError {
    ID predID = chord.getPredecessorID();
    ID localID = chord.getID();
    if (localID == null) {
      err("Can't start game! Client isn't connected to a chord network and can't retrieve own ID");
    } else if (localID.equals(predID) || predID == null) {
      err("Can't start game! Client is the only member of this chord network");
    } 
    
    log.info("Calculating distribution interval");
    return new IDInterval(predID, localID, INTERVALS); 
  }

  private void distributeShips(IDInterval interval) throws GameError {
    ID predID = chord.getPredecessorID();
    ID localID = chord.getID();
    if (localID == null) {
      err("Can't start game! Client isn't connected to a chord network and can't retrieve own ID");
    } else if (localID.equals(predID) || predID == null) {
      err("Can't start game! Client is the only member of this chord network");
    } else {
      log.info("Distributing ships");
      List<Integer> shipPositions = selectShipPositions();
      
      for(Integer shipIndex: shipPositions) { //Shiffe setzen
        ID shipID = interval.ids.get(shipIndex);
        self.setField(shipID, Field.SHIP);
      }
    }
  }
  
  private Player createSelfPlayer(IDInterval idrange) {
    ChordImpl cImpl = (ChordImpl) chord;
    Player player = new KnownPlayer(cImpl.getLocalNode().getNodeID(), idrange);
    
    for(ID id: idrange.ids) //Leeres Feld initialisieren
      player.setField(id, Field.NOTHING);
    
    return player;
  }
  
  private List<Integer> selectShipPositions() {
    List<Integer> slots = new ArrayList<>();
    for(int i = 0; i < INTERVALS; ++i) //Liste mit Indizies befüllen
      slots.add(i);
    
    Random rand = new Random();
    
    while (slots.size() > SHIPS)
      slots.remove(rand.nextInt(slots.size()));
    
    return slots;
  }
 
  private void err(String msg) throws GameError {
    throw new GameError(msg);
  }
  
  
  
  public void waitReady() {
    if (!ready) {
      try {
        log.info("Game instance is required, before full initialization");
        readyLock.acquire();
      } catch (InterruptedException ex) {
        log.error("Critical error: waitReady() interrupted!");
      }
    }
  }

  private void setReady() {
    ready = true;
    readyLock.release();
  }
  
  /** Is called inside the retrieve. This will fix all history issues(if any) caused
   *  by concurrent access to the playerMap by the node crawler.
   */
  public void fixHistory() {
    if (reapplyHistory) {
      reapplyHistory = false;
      
      for(HistoryEntry entry : history.getEntries()) {
        Player p = getPlayer(entry.dstPlayer);
        p.setField(entry.targetID, entry.hit ? Field.SHIP : Field.NOTHING);
      }
    }
  }
  
  /** This will be called by the node crawler to notify that the history should be checked.
   */
  public void reapplyHistory() {
    reapplyHistory = true;
  }
  
  public Chord getChord() {
    return chord;
  }
}
class GameError extends Exception {
  public GameError(String message) {
    super(message);
  }
}
package de.haw.ttvp.gamelogic;

import de.haw.ttvp.gamelogic.History.HistoryEntry;
import de.haw.ttvp.gamelogic.player.*;
import de.haw.ttvp.ui.Dialog;
import de.uniba.wiai.lspi.chord.data.ID;
import de.uniba.wiai.lspi.chord.service.Chord;
import de.uniba.wiai.lspi.chord.service.impl.ChordImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;



public class Game {
  public static final boolean USE_NODE_CRAWLER = true;
  public static final int INTERVALS = 100;
  public static final int SHIPS = 10;
  public static Game instance = null;
  public static final int TURN_DELAY_MS = 100;
  public static final boolean USE_SIMPLE_BROADCAST = false; //Nur über den Successor broadcasten
  public static final boolean USE_ASYNC_CHORD_CALLS = false; //Wenn true, dann wird jedes retrive() broadcast() in einem Thread gestartet
  
  private static final Logger log = Logger.getLogger(Game.class);
  private boolean ready = false;
  private final Semaphore readyLock = new Semaphore(0);
  private final Semaphore makeTurn = new Semaphore(0);
  
  public final Map<ID, Player> playerMap = new ConcurrentHashMap<>(); //Wird von dem Crawler-Thread mitbeschrieben
  public Player self;
  public final History history = new History();
  
  private final Chord chord;

  private boolean reapplyHistory = false; //Wird vom Crawler auf true gesetzt, wenn er fertig ist
  
  private TargetSelection targetSelection;
  
  // Counter der versenkten Schiffe
  private AtomicInteger lostShips = new AtomicInteger(0);
  
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
  
  public boolean start() {
    if (!Dialog.confirm("Start Game?", "User action")) {
      log.warn("Game start aborted.");
      return false;
    }
    
    try {
      IDInterval idRange = getNodeRange();
      self = createSelfPlayer(idRange);
      
      //Crawler erst starten, nachdem der Spieler mit dem ID-Intervall erstellt wurde.
      if (USE_NODE_CRAWLER)
        new NodeCrawler(chord).start();
      
      distributeShips(idRange);      
      setReady();
      
      if (isBeginningPlayer(idRange)) {
        log.info("I am starting the Game in 3 seconds...");
        try {
          Thread.sleep(3000);
        } catch (InterruptedException ex) {}
        
        shoot(); //Schuss abgeben
      } else { 
        log.info("Waiting for other player to start the game");
      }
      
      //Diesen Thread zur Zielwahl und zum Schießen nutzen.
      targetSelection = new TargetSelection(makeTurn, chord);
      targetSelection.run();
      
      // Ausgang des Spiels evaluieren
      evaluateGame();
      
      // return successful
      return true;
      
    } catch (GameError e) {
      log.error("Game aborted!\n", e);
      
      // return unsucessful
      return false;
    }
  }
  
  /** This method issues the responsible Thread to select a target and
   *  shoot at it. This is done by releasing a semaphore permit.
   */
  public void shoot() {
    makeTurn.release();
  }
  
  private boolean isBeginningPlayer(IDInterval range) {    
    log.debug(" ID-Range ");
    log.debug(" from: " + range.from.toHexString());
    log.debug("   to: " + range.to.toHexString());
    log.debug("maxID: " + ID.MAX_ID);
    
    return range.getIntervalID(ID.MAX_ID) != null;      
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
  
  /**
   * Suspend Game
   * @param losingPlayer
   */
  public void suspend(boolean losingPlayer){
	  log.info("Suspending TargetSelection from Game. losingPlayer="+losingPlayer);
	  targetSelection.suspend();
  }
  
  /**
   * Increment lost Ships of this Player
   */
  public void addLostShip(){
	 this.lostShips.getAndAdd(1);
  }
  
  /**
   * Get Count of lost Ships of this Player
   * @return
   */
  public int getLostShips(){
	  return this.lostShips.get();
  }
  
  
  /**
   * Evaluate the Result of the Game
   */
  private void evaluateGame(){
	  log.info("Evaluating Game-Success ...");
	  
	  //TODO Evaluate the Result of the Game
	  this.history.print();
  }
}

@SuppressWarnings("serial")
class GameError extends Exception {
  public GameError(String message) {
    super(message);
  }
}
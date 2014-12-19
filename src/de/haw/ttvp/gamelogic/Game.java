package de.haw.ttvp.gamelogic;

import de.uniba.wiai.lspi.chord.data.ID;
import de.uniba.wiai.lspi.chord.service.Chord;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Semaphore;
import javax.swing.JOptionPane;
import org.apache.log4j.Logger;

public class Game {
  public static final int INTERVALS = 100;
  public static final int SHIPS = 10;
  public static Game instance = null;
  
  private static final Logger log = Logger.getLogger(Game.class);
  private boolean ready = false;
  private final Semaphore readyLock = new Semaphore(0);
  
  public Map<ID, Player> playerMap = new HashMap<>();
  public Player self;
  public History history;
  
  private final Chord chord;
  //TODO müssen die Schiffe in einer Map gespeichert werden, oder sind die so in Chord gespeichert?
  //TODO wie speichern wir, welcher Knoten noch wie viele Schiffe besitzt und wo noch nicht geschossen wurde?

  public Game(Chord network) {
    this.chord = network;
    this.history = new History();
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
  
  public void start() {
    if (JOptionPane.showConfirmDialog(null, "Start Game?", "User action", JOptionPane.YES_NO_OPTION) != 0) {
      log.warn("Game start aborted.");
      return;
    }
    
    try {
      IDInterval idRange = getNodeRange();
      self = createSelfPlayer(idRange);
      distributeShips(idRange);
      //TODO Prüfen, ob man der erste Spieler ist
      
      setReady();
      
      if (isBeginningPlayer(idRange)) {
        log.info("I am staring the Game");
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
    Player player = new KnownPlayer(idrange);
    
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
}


class GameError extends Exception {
  public GameError(String message) {
    super(message);
  }
}
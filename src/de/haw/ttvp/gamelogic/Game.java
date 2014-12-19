package de.haw.ttvp.gamelogic;

import de.uniba.wiai.lspi.chord.data.ID;
import de.uniba.wiai.lspi.chord.service.Chord;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.swing.JOptionPane;
import org.apache.log4j.Logger;

public class Game {
  public static final int INTERVALS = 100;
  public static final int SHIPS = 10;
  public static Game instance = null;
  
  private static final Logger log = Logger.getLogger(Game.class);
  
  private Chord chord;
  //TODO müssen die Schiffe in einer Map gespeichert werden, oder sind die so in Chord gespeichert?
  //TODO wie speichern wir, welcher Knoten noch wie viele Schiffe besitzt und wo noch nicht geschossen wurde?

  public Game(Chord network) {
    this.chord = network;
    Game.instance = this;
  }
  
  public void start() {
    if (JOptionPane.showConfirmDialog(null, "Start Game?", "User action", JOptionPane.YES_NO_OPTION) != 0) {
      log.warn("Game start aborted.");
      return;
    }
    
    try {
      distributeShips();
      //TODO Auf Events warten, oder den ersten Schuss abgeben 
      //TODO Spielschleife
      
      //TODO wie wird das Ende des Spiels zuverlässig erkannt?
      //TODO wie muss das anderen Knoten mitgeteilt werden?
    } catch (GameError e) {
      log.error("Game aborted!\n", e);
    }
  }

  private void distributeShips() throws GameError {
    ID predID = chord.getPredecessorID();
    ID localID = chord.getID();
    if (localID == null) {
      err("Can't start game! Client isn't connected to a chord network and can't retrieve own ID");
    } else if (localID.equals(predID) || predID == null) {
      err("Can't start game! Client is the only member of this chord network");
    } else {
      log.info("Calculating distribution interval");
      IDInterval interval = new IDInterval(predID, localID, INTERVALS);
      //TODO schiffe auf das Intervall aufteilen
      
      
      log.info("Distributing ships");
      
    }
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
}


class GameError extends Exception {
  public GameError(String message) {
    super(message);
  }
}
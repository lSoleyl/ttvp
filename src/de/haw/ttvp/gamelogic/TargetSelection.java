package de.haw.ttvp.gamelogic;

import de.haw.ttvp.gamelogic.player.Player;
import de.haw.ttvp.gamelogic.strategy.PatternStrategy;
import de.haw.ttvp.gamelogic.strategy.WeakestKnownTarget;
import de.uniba.wiai.lspi.chord.data.ID;
import de.uniba.wiai.lspi.chord.service.Chord;
import de.uniba.wiai.lspi.chord.service.ServiceException;

import java.util.concurrent.Semaphore;

import org.apache.log4j.Logger;

public class TargetSelection {
  private final Logger log = Logger.getLogger(TargetSelection.class);
  private final Semaphore makeTurn;
  private final Chord chord;
  private boolean isRunning = true;

  public TargetSelection(Semaphore makeTurn, Chord chord) {
    this.makeTurn = makeTurn;
    this.chord = chord;
  }  
  
  public void run() throws GameError {
    log.info("Thread now running target selection");
    while(isRunning) {
      try {
        log.debug("Waiting for my next turn");
        makeTurn.acquire(); //Auf nächsten Zug warten
        
        // Handle end of Game
        if(!isRunning){
        	log.info("Game ended");
        	return;
        }
        
        Thread.sleep(Game.TURN_DELAY_MS); //Kurz warten
      } catch (InterruptedException ex) {
        throw new GameError("TargetSelection-Thread got interrupted while waiting for next turn");
      }

      //Ziel wählen und Schuss abgeben
      final ID target = findTarget();
      for (Player p : Game.instance.playerMap.values()) {
        if (p.isKnown() && p.known().getInterval().contains(target)) {
          log.info("Selected player: " + p.getID().shortString() + "  he has lost " + p.shipsLost() + " ships and has " + 
                  p.known().unknownSlots() + " UNKNOWN slots left");
          break;
        }
      }
      
      log.info("Shooting at ID: " + target);
      log.debug("calling retrive(" + target + ")");
      AsyncInvoke.invoke(() -> {
        try {
          chord.retrieve(target);
        } catch (ServiceException e) {
          log.error("Retrieve on ID : " + target + " failed with error:\n", e);
        }
      });
      log.debug("retrive() returned");
    }
    log.info("TargetSelection routine suspended...");
  }
  
  /**
   * <strong>Find Target</strong><br>
   * Finds Target by choosing an appropriate Strategy
   * for the current Game's situation
   * @return
   */
  private ID findTarget(){
	  // Choose Game-Strategy to determine Target
	  if(Game.USE_SIMPLE_STRATEGY){
		  return WeakestKnownTarget.instance().findTarget();
	  } else {
		  return PatternStrategy.instance().findTarget();
	  }
  }
  
  /**
   * <strong>Suspend</strong><br>
   * Thread-like suspension of Running-Loop
   */
  public void suspend(){
	  this.isRunning = false;
    makeTurn.release();
  }
}

package de.haw.ttvp.gamelogic;

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
      ID target = findTarget();
      log.info("Shooting at ID: " + target);
      try {
        log.debug("calling retrive(" + target + ")");
        AsyncInvoke.retrive(target);
        log.debug("retrive() returned");
      } catch (ServiceException e) {
        log.error("Retrieve on ID : " + target + " failed with error:\n", e);
        throw new GameError("TargetSelection can't proceed, retrieve() failed!");
      }
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
	  //TODO
	  return WeakestKnownTarget.instance().findTarget();
  }
  
  /**
   * <strong>Suspend</strong><br>
   * Thread-like suspension of Running-Loop
   */
  public void suspend(){
	  this.isRunning = false;
  }
}

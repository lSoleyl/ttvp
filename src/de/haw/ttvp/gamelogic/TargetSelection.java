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

  public TargetSelection(Semaphore makeTurn, Chord chord) {
    this.makeTurn = makeTurn;
    this.chord = chord;
  }  
  
  public void run() throws GameError {
    log.info("Thread now running target selection");
    while(true) { //TODO Schleife verlassen, wenn das Spiel gewonnen ist.
      try {
        log.debug("Waiting for my next turn");
        makeTurn.acquire(); //Auf nächsten Zug warten
        Thread.sleep(Game.TURN_DELAY_MS); //Kurz warten
      } catch (InterruptedException ex) {
        throw new GameError("TargetSelection-Thread got interrupted while waiting for next turn");
      }

      //TODO strategie wählen
      //Ziel wählen und Schuss abgeben
      ID target = WeakestKnownTarget.instance().findTarget();
      log.debug("Shooting at ID: " + target);
      try {
        chord.retrieve(target);
      } catch (ServiceException e) {
        log.error("Retrieve on ID : " + target + " failed with error:\n", e);
        throw new GameError("TargetSelection can't proceed, retrieve() failed!");
      }
    }
  }
}

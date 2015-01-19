package de.haw.ttvp.chord;

import de.haw.ttvp.gamelogic.AsyncInvoke;
import de.haw.ttvp.gamelogic.Field;
import de.haw.ttvp.gamelogic.Game;
import de.haw.ttvp.gamelogic.player.Player;
import de.uniba.wiai.lspi.chord.data.ID;
import de.uniba.wiai.lspi.chord.service.NotifyCallback;
import org.apache.log4j.Logger;

public class CallbackHandler implements NotifyCallback {
  private static Logger LOG = Logger.getLogger(CallbackHandler.class);
  
  public CallbackHandler() {}
  
  //TODO verbinden mit dem Spiel.  (Mit 'Game.instance' auf das Game zugreifen?)
  
  @Override
  public void retrieved(ID target) {
    LOG.debug("NotifyCallback.retrieved(" + target.toString() + ")");
    
    Game.instance.waitReady();
    Game.instance.fixHistory(); //History nachziehen, falls der Crawler fertig ist.
    
    Player self = Game.instance.self;
    
    final boolean hit = self.hasShipAt(target);
    if (hit)
      self.setField(target, Field.NOTHING); //Shiff als versenkt markieren
    
    LOG.debug("calling broadcast(" + target + ", "  + hit + ")");
    AsyncInvoke.invoke(() -> Game.instance.getChord().broadcast(target, hit));
    LOG.debug("broadcast() returned");
    
    Game.instance.shoot(); //Shoot kehrt sofort zurück und führt die Zielsuche 
        //und den Schuss von einem zweiten Thread durch. (Main-Thread)    
  }

  @Override
  public void broadcast(ID source, ID target, Boolean hit, int transactionID) {
    LOG.debug("NotifyCallback.broadcast(" + source + ", " + target + ", " + hit + ", " + transactionID + ")");
    //Hier nicht Game.waitReady(), da es nicht notwendig ist...
    Player dstPlayer = Game.instance.getPlayer(source);
    if (dstPlayer != Game.instance.self)
      dstPlayer.setField(target, hit ? Field.SHIP : Field.NOTHING);
    
    if (dstPlayer.shipsLost() == Game.SHIPS) 
      Game.instance.history.finalize(transactionID, source, target, hit);
    else 
      Game.instance.history.addEntry(transactionID, source, target, hit);
    
    if (dstPlayer.shipsLost() == Game.SHIPS) {
    	LOG.debug("Detected destruction of last Ship of Player with ID: "+dstPlayer.getID().toHexString());
    	
    	// Stop Game & trigger Evaluation of Game-Statistics
    	Game.instance.suspend();
    }
  }

}

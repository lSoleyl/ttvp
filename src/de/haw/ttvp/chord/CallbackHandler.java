package de.haw.ttvp.chord;

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
    
    boolean hit = false;
    if (self.hasShipAt(target)) {
      self.setField(target, Field.NOTHING); //Shiff als versenkt markieren
      hit = true;
    }
    
    Game.instance.getChord().broadcast(target, hit); //Alle anderen Knoten benachrichtigen
    Game.instance.shoot(); //Shoot kehrt sofort zurück und führt die Zielsuche 
                           //und den Schuss von einem zweiten Thread durch. (Main-Thread)
  }

  @Override
  public void broadcast(ID source, ID target, Boolean hit, int transactionID, String sourceHost) {
    //Hier nicht Game.waitReady(), da es nicht notwendig ist...
    Player dstPlayer = Game.instance.getPlayer(source);
    dstPlayer.setField(target, hit ? Field.SHIP : Field.NOTHING);
    
    //TODO erkennen, ob man gewonnen hat (man hat den Schuss für diesen Broadcast selbst abgegeben und der Spieler hat keine Schiffe mehr)
    //TODO wenn gewonnen, dann TargetSelection-Thread benachrichtigen und History ausgeben
    
    Game.instance.history.addEntry(transactionID, source, target, hit, sourceHost);
  }

}

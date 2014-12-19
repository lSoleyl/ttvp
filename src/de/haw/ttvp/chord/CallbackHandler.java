package de.haw.ttvp.chord;

import de.haw.ttvp.gamelogic.Game;
import de.haw.ttvp.gamelogic.Player;
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
    Player self = Game.instance.self;
    //TODO prüfen, ob getroffen (self.hasShipAt(target))
    
  }

  @Override
  public void broadcast(ID source, ID target, Boolean hit) {
    LOG.debug("NotifyCallback.broadcast(" + source.toString() + "," + target.toString() + "," + hit + ")");
  }

}

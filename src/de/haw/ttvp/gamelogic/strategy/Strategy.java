package de.haw.ttvp.gamelogic.strategy;

import de.haw.ttvp.gamelogic.Game;
import de.haw.ttvp.gamelogic.History;
import de.haw.ttvp.gamelogic.player.KnownPlayer;
import de.haw.ttvp.gamelogic.player.Player;
import de.uniba.wiai.lspi.chord.data.ID;
import java.util.Map;
import org.apache.log4j.Logger;

/**
 * <strong>Strategy</strong><br>
 * Abstract Class for Game Strategies
 * @author paul
 *
 */
public abstract class Strategy {
  private final Logger log = Logger.getLogger(Strategy.class);
  
	protected Map<ID,Player> playerMap;
	protected History history;
	
	public Strategy(){
		this.playerMap = Game.instance.playerMap;
		this.history = Game.instance.history;
	}
	
	/**
	 * <strong>Find Target</strong><br>
	 * Returns ID for a designated Target chosen by this Strategy
	 * @return Target ID
	 */
	public abstract ID findTarget();
  
  
  /** Tries to find the weakest player of the players known.
   *  
   * @return the weakest player or null if only unknown players exist
   */
  protected KnownPlayer selectWeakestKnownPlayer() {
    KnownPlayer weakestPlayer = null;
    
    for (Player player : playerMap.values()) {
      if (player.isKnown() ||  player != Game.instance.self) { //Unbekannte Spieler ignorieren
        if (weakestPlayer == null) { //Noch kein Spieler vorhanden zum Vergleich
          weakestPlayer = player.known();
          continue;
        }
        
        if (isWeaker(player.known(), weakestPlayer)) //Schwächerer Spieler gefunden
          weakestPlayer = player.known();
      }
    }
    
    if (weakestPlayer == null)
      log.warn("Couldn't find weakest player... no player known!");
    
    return weakestPlayer;
  }
  
  /** Small helper method to determine whether a player is weaker than another player.
   *  weaker = lost more ships AND has less unknown slots left
   */
  private boolean isWeaker(KnownPlayer p1, KnownPlayer p2) {
    return p1.shipsLost() >= p2.shipsLost() &&
           p1.unknownSlots() < p2.unknownSlots();
  }
}

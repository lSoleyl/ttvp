package de.haw.ttvp.gamelogic.strategy;

import de.haw.ttvp.gamelogic.History;
import de.haw.ttvp.gamelogic.player.Player;
import de.uniba.wiai.lspi.chord.data.ID;
import java.util.Map;

/**
 * <strong>Strategy</strong><br>
 * Abstract Class for Game Strategies
 * @author paul
 *
 */
public abstract class Strategy {

	protected Map<ID,Player> playerMap;
	protected History history;
	
	public Strategy(Map<ID,Player> playerMap, History history){
		this.playerMap = playerMap;
		this.history = history;
	}
	
	/**
	 * <strong>Find Target</strong><br>
	 * Returns ID for a designated Target chosen by this Strategy
	 * @return Target ID
	 */
	public abstract ID findTarget();
}

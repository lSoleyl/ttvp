package de.haw.ttvp.gamelogic;

import java.util.List;

import de.uniba.wiai.lspi.chord.data.ID;

/**
 * <strong>Strategy</strong><br>
 * Abstract Class for Game Strategies
 * @author paul
 *
 */
public abstract class Strategy {

	protected List<Player> player;
	protected History history;
	
	public Strategy(List<Player> player, History history){
		this.player = player;
		this.history = history;
	}
	
	/**
	 * <strong>Find Target</strong><br>
	 * Returns ID for a designated Target chosen by this Strategy
	 * @return Target ID
	 */
	public abstract ID findTarget();
}

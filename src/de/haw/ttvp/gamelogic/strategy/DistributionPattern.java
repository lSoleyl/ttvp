package de.haw.ttvp.gamelogic.strategy;

import java.util.List;

import de.haw.ttvp.gamelogic.player.KnownPlayer;
import de.uniba.wiai.lspi.chord.data.ID;

/**
 * <strong>DistributionPattern</strong><br>
 * Abstract Class representing a distribution Pattern of Ships
 *
 */
public abstract class DistributionPattern {
	 
	 /**
	  * <strong>Detect Pattern</strong><br>
	  * Determines if this specific distribution Pattern can be applied to a given Player
	  * @return success
	  */
	 public abstract boolean detectPattern(KnownPlayer player);
	 
	 /**
	  * <strong>Find Target</strong><br>
	  * Searches for possible targets for a given player considering a valid Cluster-Pattern
	  * @param player
	  * @return List of target IDs
	  * @throws IllegalArgumentException
	  */
	 public abstract List<ID> findTarget(KnownPlayer player) throws IllegalArgumentException;

}
	
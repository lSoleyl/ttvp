package de.haw.ttvp.gamelogic.strategy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import org.apache.log4j.Logger;

import de.haw.ttvp.gamelogic.Field;
import de.haw.ttvp.gamelogic.Game;
import de.haw.ttvp.gamelogic.History.HistoryEntry;
import de.haw.ttvp.gamelogic.player.KnownPlayer;
import de.haw.ttvp.gamelogic.player.Player;
import de.uniba.wiai.lspi.chord.data.ID;

public class PatternStrategy extends Strategy {
	private final static Logger LOG = Logger.getLogger(PatternStrategy.class);
	
	private static PatternStrategy instance;
	
	private DistributionPattern clusterPattern;
	private DistributionPattern linearPattern;
	
	public static Strategy instance() {
	    if (instance == null){
	      instance = new PatternStrategy();
	      
	      instance.clusterPattern = ClusterDistributionPattern.getInstance();
	      instance.linearPattern = LinearDistributionPattern.getInstance();
	    }
	    
	    return instance;    
	}

	@Override
	public ID findTarget() {
		LOG.info("Finding an appropriate Target");
		
		// Passive Mode if own Player is weakest Player in the Game
		if(isWeaker(Game.instance.self, selectWeakestKnownPlayer()) ){
			return findTargetPassive();
		// Active Mode if own Player is not weakest Player in the Game
		} else {
			return findTargetActive();
		}
	}
	
	/**
	 * <strong>Find Target Active</strong><br>
	 * Determines a Target in Active-Mode which means that own Player can
	 * afford to target other Players in an aggressive manner and cope with
	 * the loss of some Ships
	 * @return Field ID
	 */
	private ID findTargetActive(){
		LOG.info("Finding Target in active Mode");
		
		Map<Player, List<ID>> targets = new HashMap<Player, List<ID>>();
		
		for(Entry<ID, Player> p:playerMap.entrySet()){
			KnownPlayer player = (KnownPlayer) p.getValue();
			
			// Add targets if a Cluster-Pattern could be detected
			if(clusterPattern.detectPattern(player)){
				targets.put(player, clusterPattern.findTarget(player));
			
			// Add targets if a Linear-Pattern could be detected
			} else if(linearPattern.detectPattern(player)){
				targets.put(player, linearPattern.findTarget(player));
			
			// No pattern detected -> Field will be choosen randomly
			} else {
				LOG.info("No pattern detected for Player ID="+player.getID().toHexString());
			}
		}
		
		// Determined Player
		Player targetPlayer = null;
		
		// Try to find Player with only a single Ship left
		Optional<Player> dyingPlayer = playerMap.values().stream().filter((player) -> player.shipsLost()==(Game.SHIPS-1)).findFirst();
		
		if(dyingPlayer.isPresent()){
			targetPlayer = dyingPlayer.get();
		}
		
		
		// No dying Player could be detected therefore another target has to be determined
		if(targetPlayer == null){
		
			// Try to find Player who shoots back at origin
			List<Player> backShootingPlayers = new ArrayList<>();
			
			for(Entry<ID, Player> playerEntry:playerMap.entrySet()){
				
				// Iterate through History
				boolean shootsBack = true;
				ID lastAttacker = null;
				for(HistoryEntry entry:history.getEntries()){
					if(history.getAttacker(entry.transactionID).equals(playerEntry.getKey())
							&& !entry.dstPlayer.equals(lastAttacker)){
						shootsBack = false;
					}
					lastAttacker = history.getAttacker(entry.transactionID);
				}
				
				if(shootsBack){
					backShootingPlayers.add(playerEntry.getValue());
				}
			}
			
			// Get Weakest of possible back shooting Players
			Player weakestBackshooter = null;
			
			for(Player player:backShootingPlayers){
				
				// Check if player is weaker and therefore likely to be killed
				if(weakestBackshooter == null || isWeaker((KnownPlayer) targetPlayer, (KnownPlayer) player)){
					weakestBackshooter = player;
				}
			}
			
			// Setting Backshooting Player as new Target
			if(!isWeaker((KnownPlayer) targetPlayer, Game.instance.self)){
				targetPlayer = weakestBackshooter;
				LOG.info("Targeting backshooting Player: ID="+targetPlayer.getID().toHexString());
			}
			
		}
		
		// No backshooting Player detected or weakest backshooting Player stronger than own Player
		if(targetPlayer == null){
			
			// Find inactive Player
			Player mostInactivePlayer = null;
			for(Entry<ID, Player> entry:playerMap.entrySet()){
				
				if(mostInactivePlayer == null || entry.getValue().hitCount()<mostInactivePlayer.hitCount()){
					mostInactivePlayer = entry.getValue();
				}
			}
			
			// Set most inactive Player as new Target
			if(mostInactivePlayer != null){
				targetPlayer = mostInactivePlayer;
				LOG.info("Targeting Player as mostInactive: ID="+targetPlayer.getID().toHexString());
			}
		}
		
		// Fallback: select random Player
		if(targetPlayer == null){
			LOG.warn("Fallback to random Player selection due to no previously found target Player");
			targetPlayer = playerMap.entrySet().iterator().next().getValue();
		}
		
		// Return one of the possible pattern targets for the designated target Player
		if(targets.get(targetPlayer) != null && targets.get(targetPlayer).size()>0){
			return targets.get(targetPlayer).get(0);
		
		// Determine a random Field of the designated target Player
		} else {
			return getRandomTargetForPlayer(targetPlayer);
		}
	}
	
	private ID findTargetPassive(){
		LOG.info("Finding Target in passive Mode");
		
		//TODO
		
		return null;
	}
	
	
	/**
	 * <strong>Get random Target</strong><br>
	 * Returns a random field in the givens Players range which
	 * is UNKNOWN
	 * @param player
	 * @return Field ID
	 */
	private ID getRandomTargetForPlayer(Player player){
		LOG.info("Get random Target for Player with ID="+player.getID().toHexString());
		
		for(Entry<ID, Field> entry:player.getFieldMap().entrySet()){
			if(entry.getValue() == Field.UNKNOWN){
				return entry.getKey();
			}
		}
		
		// No Field found
		return null;
	}
	
}

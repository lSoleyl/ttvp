package de.haw.ttvp.gamelogic.strategy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.log4j.Logger;

import de.haw.ttvp.gamelogic.Field;
import de.haw.ttvp.gamelogic.Game;
import de.haw.ttvp.gamelogic.History.HistoryEntry;
import de.haw.ttvp.gamelogic.player.KnownPlayer;
import de.haw.ttvp.gamelogic.player.Player;
import de.uniba.wiai.lspi.chord.com.CommunicationException;
import de.uniba.wiai.lspi.chord.data.ID;
import de.uniba.wiai.lspi.chord.service.impl.ChordImpl;
import java.util.Random;

public class PatternStrategy extends Strategy {
	private final static Logger LOG = Logger.getLogger(PatternStrategy.class);
	
	private static PatternStrategy instance;
	
	private DistributionPattern clusterPattern;
	private DistributionPattern linearPattern;
	
	private PatternStrategy() {
		super();
	}
	
	public static PatternStrategy instance() {
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
		if(isWeaker(self, selectWeakestKnownPlayer()) ){
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

		// Check for Pattern in ship-distribution of Players
		Map<Player, List<ID>> targets = getPatternTargetMap();
		
		// Determined Player
		Player targetPlayer = null;
		
		// Try to find Player with only a single Ship left
		Optional<Player> dyingPlayer = playerMap.values().stream().filter((player) -> player.shipsLost()==(Game.SHIPS-1)).findFirst();
		
		if(dyingPlayer.isPresent()){
			targetPlayer = dyingPlayer.get();
		}
		
		
		// No dying Player could be detected therefore another target has to be determined
		if(targetPlayer == null){
			LOG.info("Determining backshooting Player");
		
			// Try to find Player who shoots back at origin
			List<Player> backShootingPlayers = new ArrayList<>();
			
			for(Entry<ID, Player> playerEntry:playerMap.entrySet()){
				
				// Iterate through History
				boolean hasEntries = false;
				boolean shootsBack = true;
				ID lastAttacker = null;
				for(HistoryEntry entry:history.getEntries()){
					// Check if entry describes shot by designated Player
          ID attacker = history.getAttacker(entry.transactionID);
          
					if(attacker != null && attacker.equals(playerEntry.getKey())){
						// Check if Player was shooting back at previous Attacker
						if(!entry.dstPlayer.equals(lastAttacker)){
							shootsBack = false;
						}
						hasEntries = true;
					}
					lastAttacker = attacker;
				}
				
				if(hasEntries && shootsBack){
					backShootingPlayers.add(playerEntry.getValue());
				}
			}
			
			// Get Weakest of possible back shooting Players
			Player weakestBackshooter = null;
			
			for(Player player:backShootingPlayers){
				
				// Check if player is weaker and therefore likely to be killed
				if(weakestBackshooter == null || isWeaker(self, (KnownPlayer) player)){
					weakestBackshooter = player;
				}
			}
			
			// Setting Backshooting Player as new Target
			if(weakestBackshooter != null && !isWeaker((KnownPlayer) weakestBackshooter, self)){
				targetPlayer = weakestBackshooter;
				LOG.info("Targeting backshooting Player: ID="+targetPlayer.getID().toHexString());
			} else LOG.info("No backshooting Player could be detected or own Player is too weak");
			
		}
		
		// No backshooting Player detected or weakest backshooting Player stronger than own Player
		if(targetPlayer == null){
			LOG.info("Determining the most inactive Player");
			
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
	
	/**
	 * <strong>Find Target Passive</strong><br>
	 * Determines a Target in passive mode considering the own Player is too weak
	 * to receive any further hits. Therefore the designated Target should not
	 * be aware of this player
	 * @return Target ID
	 */
	private ID findTargetPassive(){
		LOG.info("Finding Target in passive Mode");
		
		ID target = null;
		
		// Check for Pattern in ship-distribution of Players
		Map<Player, List<ID>> targets = getPatternTargetMap();
		
		// Determined Player
		Player targetPlayer = null;
		
		// Get Successor from Chord
		try {
			ChordImpl chord = (ChordImpl) Game.instance.getChord();
			ID successor = chord.getLocalNode().findSuccessor(self.getID().add(1)).getNodeID();
			targetPlayer = playerMap.get(successor);
			
		} catch (CommunicationException | NullPointerException e) {
			LOG.error("ERROR: Could not get this Player Successor from Chord: "+e.getLocalizedMessage(), e);
		}
		
		// If Successor could not be found, set any Player who has not shot at own player yet as target
		if(targetPlayer == null){
			
			// Array of all HistoryEntries witch where shot at own Player
			Stream<HistoryEntry> opt = history.getEntries().stream().filter((entry) -> entry.dstPlayer.equals(self.getID()));
			Object[] entryArr = opt.toArray();
			
			// Mapping of all possible targets
			Map<Player, Boolean> shotAtUs = new HashMap<>();
			
			for(Player p:playerMap.values())
				shotAtUs.put(p, false);
			
			// Check if those possible targets took a shot at own Player
			for(int i=0; i<entryArr.length; i++){
				HistoryEntry entry = (HistoryEntry) entryArr[i];
				Player p = Game.instance.getPlayer(Game.instance.history.getAttacker(entry.transactionID) );
				
				// Remove entry from Collection of possible Targets
				if(shotAtUs.containsKey(p)){
					LOG.info("Found Player who shoot at us: "+p.getID().toHexString());
					shotAtUs.put(p, true);
				}
			}
			
			// List of Targets who did not shot at us
			List<Player> possibleTargets = new ArrayList<>();
			for(Entry<Player, Boolean> e:shotAtUs.entrySet()){
				if(!e.getValue()){
					possibleTargets.add(e.getKey());
				}
			}
			
			// Select target from Collection of remaining possible Targets
			if(possibleTargets.size()>0){
				targetPlayer = possibleTargets.iterator().next();
			} else {
				LOG.warn("Fallback to random Player selection due to no previously found target Player (passive Mode)");
				targetPlayer = playerMap.entrySet().iterator().next().getValue();
			}
		}
		
		// Check if a pattern could be found for the targetPlayer
		if(targets.containsKey(targetPlayer) && targets.get(targetPlayer).size()>0 ){
			target = targets.get(targetPlayer).get(0);
		} else target = getRandomTargetForPlayer(targetPlayer);
		
		return target;
	}
	
	/**
	 * <strong>Get Pattern TargetMap</strong><br>
	 * Checking for Patterns in Ship-Distribution of Players
	 * and returns Mapping of possible Targets for Players
	 * @return Map of targetIDs for each Player
	 */
	private Map<Player, List<ID>> getPatternTargetMap(){
		LOG.info("Checking Players for Ship distribution Patterns");
		
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
		
		// Return mapping of Targets
		return targets;
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
		
    List<ID> targets = new ArrayList<>();
    
    for (Entry<ID, Field> e: player.getFieldMap().entrySet()) {
      if (e.getValue() == Field.UNKNOWN)
        targets.add(e.getKey());
    }
    
    if (!targets.isEmpty())
      return targets.get(new Random().nextInt(targets.size()));
		
		// No Field found
		return null;
	}
	
}

package de.haw.ttvp.gamelogic.strategy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import de.haw.ttvp.gamelogic.Field;
import de.haw.ttvp.gamelogic.player.KnownPlayer;
import de.uniba.wiai.lspi.chord.data.ID;

public class LinearDistributionPattern extends DistributionPattern {
	private static final Logger LOG = Logger.getLogger(LinearDistributionPattern.class);
	
	// Threshold for minimal Count of a minimal Distance
	private static final int VALID_DISTANCE_THRESHOLD = 2;
	
	// Instance of this Singleton
	private static LinearDistributionPattern instance;
	
	// Mapping if linear Distance for specific Players
	private Map<KnownPlayer, Integer> playerMap;
	
	protected LinearDistributionPattern(){}
	
	public static LinearDistributionPattern getInstance(){
		if(instance == null){
			instance = new LinearDistributionPattern();
			instance.playerMap = new HashMap<KnownPlayer, Integer>();
		}
		
		return instance;
	}

	@Override
	public boolean detectPattern(KnownPlayer player) {
		LOG.info("Detecting possible Linear-Pattern for Player: "+player.getID().toBinaryString());
		
		boolean success = false;
		
		// known distances for Player's distribution of Ships
		Integer linearDist = findLinearDistance(player);
		
		// Save distance in PlayerMap once found
		if(linearDist != null){
			success = true;
			playerMap.put(player, linearDist);
		}

		// Return if a distribution distance could be determined
		return success;
	}

	@Override
	public List<ID> findTarget(KnownPlayer player)
			throws IllegalArgumentException {
		LOG.info("FindTarget called for player ID: "+player.getID().toHexString());
		
		if(!playerMap.containsKey(player)){
			LOG.info("Player not checked for linear distribution Distance yet");
			
			if(!detectPattern(player)){
				throw new IllegalArgumentException("Could not find any Targets for Player due to no known Clusters. PlayerID: "+player.getID().toHexString());
			}
		}
		
		// List of all possible targets for this Player
		List<ID> targets = new ArrayList<>();
		
		// Detected linear distance between Ships in Players Intervall
		int linearDist = playerMap.get(player);
		
		// Flag to check if the first Ship in the Players Interval was found
		boolean firstShipFound = true;
		
		// Distance to the first known ship
		int distToFirstShip = 0;
		
		boolean firstShip = true;
		
		int distCnt = 0;
		for(Entry<ID, Field> entry:player.getFieldMap().entrySet()){
			
			// Already checked Fields previous to first Ship exceed known
			// distribution Distance so there has to be a ship within this range
			if(distCnt>linearDist){
				firstShipFound = false;
			}
			
			switch(entry.getValue()){
			case SHIP:
				
				// Save distance to first known Ship
				if(firstShip){
					distToFirstShip = distCnt;
					firstShip = false;
				}
				
				// reset distCnt
				distCnt = 0;
				break;

			case NOTHING:
				// Increment distance Counter
				distCnt++;
				break;
				
			case UNKNOWN:
				if(distCnt == linearDist){
					// distance matches linear Distance so here might be a target
					targets.add(entry.getKey());
				} else {
					// Increment distance Counter
					distCnt++;
				}
				break;
			}
		}
		
		// Find target for fist ship
		if(!firstShipFound){
			
			// Number of possible targets previous to fist known ship
			int shipsPrevToFirst = (int) distToFirstShip/linearDist;
			int offset = distToFirstShip%linearDist;
			
			int idxCnt = 0;
			int lastCnt = -1;
			for(Entry<ID, Field> entry:player.getFieldMap().entrySet()){
				if(entry.getValue() == Field.UNKNOWN){
					
					// locate Targets
					if( (idxCnt == offset) || 
						( (shipsPrevToFirst>0) && (idxCnt-lastCnt == linearDist) )){
						targets.add(entry.getKey());
						lastCnt = idxCnt;
						shipsPrevToFirst--;
					
					// All possible ships previous to first located
					} else if(shipsPrevToFirst == 0){
						break;
					}
				}
				idxCnt++;
			}
			
		}
		
		return targets;
	}
	
	/**
	 * <strong>Find linear Distance</strong><br>
	 * Determines the minimal linear distance between Ships in the players Cluster
	 * @param player
	 * @return linear distribution Distance
	 */
	private Integer findLinearDistance(KnownPlayer player){
		LOG.info("Trying to find a linear distribution Distance for Player with ID="+player.getID().toHexString());
		
		// List of known Ship-Positions
		List<Integer> shipPos = new ArrayList<>();
		
		int idxCnt = 0;
		for(Entry<ID, Field> entry:player.getFieldMap().entrySet()){
			
			// Save Positions of Ships
			if(entry.getValue() == Field.SHIP){
				shipPos.add(idxCnt);
			}
			
			// Increment index Counter
			idxCnt++;
		}
		
		// smallest Distance between known ships
		int minDist = shipPos.get(0);
		int lastIdx = -1;
		
		for(Integer idx:shipPos){
			minDist = ( (idx-lastIdx) < minDist)? (idx-lastIdx) : minDist;
			lastIdx = idx;
		}
		
		// Check if valid minimal Distance was found
		if(minDist > 0){
		
			// Calculate occurrence of minimal Distance
			int cnt = 0;
			lastIdx = -1;
			for(Integer idx:shipPos){
				if((idx-lastIdx) == minDist)
					cnt++;
				lastIdx = idx;
			}
			
			// Check if minimal Distance occurs often enough
			if(cnt>=VALID_DISTANCE_THRESHOLD){
				return minDist;
			} else return null;
			
		// No valid minimal Distance found
		} else return null;
	}

}

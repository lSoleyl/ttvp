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

/**
 * <strong>Cluster Distribution Pattern</strong><br>
 * Represents a Pattern where chunks of Ships are Clustered within the Players Range
 *
 */
public class ClusterDistributionPattern extends DistributionPattern {
	private static final Logger LOG = Logger.getLogger(ClusterDistributionPattern.class);
	
	// Threshold for minimal size of a Cluster
	private static final int CLUSTER_SIZE_THRESHOLD = 2;
	
	// Instance of this Singleton
	private static ClusterDistributionPattern instance = null;
	
	// Mapping of ShipClusters known for specific Players
	private Map<KnownPlayer, List<ShipCluster>> playerMap;
	
	protected ClusterDistributionPattern(){}
	
	public static ClusterDistributionPattern getInstance(){
		if(instance == null){
			instance = new ClusterDistributionPattern();
			instance.playerMap = new HashMap<KnownPlayer, List<ShipCluster>>();
		}
		return instance;
	}

	@Override
	public boolean detectPattern(KnownPlayer player) {
		LOG.info("Detecting possible Cluster-Pattern for Player: "+player.getID().toBinaryString());
		
		boolean success = false;
		
		// Known Cluster Position
		List<ShipCluster> clusters = findClusters(player);
		
		// If clusters have been found, save in PlayerMap
		if(clusters.size()>0){
			success = true;
			playerMap.put(player, clusters);
		}
		
		// Return if Cluster has been detected
		return success;
	}
	
	@Override
	public List<ID> findTarget(KnownPlayer player) throws IllegalArgumentException {
		LOG.info("FindTarget called for player ID: "+player.getID().toHexString());
		
		if(!playerMap.containsKey(player)){
			LOG.info("Player not checked for ShipClusters yet");
			
			if(!detectPattern(player)){
				throw new IllegalArgumentException("Could not find any Targets for Player due to no known Clusters. PlayerID: "+player.getID().toHexString());
			}
		}

		// List of all possible targets for this Player
		List<ID> targets = new ArrayList<>();
		
		for(ShipCluster cl:playerMap.get(player)){
			if(cl.isOpen()){
				
				int cnt = 0;
				// Iterate through the Players Fields and compare with known Clusters
				for(Entry<ID, Field> entry:player.getFieldMap().entrySet()){
					
					// Add Field as Target if the next Field is beginning of known Cluster 
					// and current Field is unknown
					if(cl.startIdx == (cnt+1) && entry.getValue() == Field.UNKNOWN){
						targets.add(entry.getKey());
						
					// Add Field as Target if the previous Field is end of known Cluster 
					// and current Field is unknown
					} else if(cl.endIdx == (cnt-1) && entry.getValue() == Field.UNKNOWN){
						targets.add(entry.getKey());
					}
					
					// Increment Field-Counter
					cnt++;
				}
			}
		}
		
		// Return list of possible Targets
		return targets;
	}
	
	/**
	 * <strong>Find Clusters</strong><br>
	 * Searches for Clusters of known Ships in Range of given Player
	 * @return List of ShipClusters
	 */
	private List<ShipCluster> findClusters(KnownPlayer player){
		LOG.info("Finding Clusters for Player ID: "+player.getID().toHexString());
		
		List<ShipCluster> clusters = new ArrayList<>();
		
		// Field counter
		int cnt = 0;
		
		// Currently detected Cluster
		ShipCluster currentCluster = null;
		Field prevField = Field.NOTHING;
		
		// Iterate through players Fields and find Clusters
		for(Entry<ID, Field> entry:player.getFieldMap().entrySet()){
			switch(entry.getValue()){
			case UNKNOWN:
				
				// End of Cluster found
				if(currentCluster != null){
					
					// Field previous to Clusters starting Field is 
					// not unknown
					if(!currentCluster.isOpen()){
						currentCluster.open = true;
					}
					
					// Add Cluster if its size meets the cluster size requirement
					if(currentCluster.size()>=CLUSTER_SIZE_THRESHOLD){
						clusters.add(currentCluster);
					}

					// Reset CurrentCluster
					currentCluster = null;
				}
				break;
			case SHIP:
				
				// Extend current Cluster
				if(currentCluster != null){
					currentCluster.endIdx = cnt;
				
				// New possible Cluster found
				} else {
					currentCluster = new ShipCluster(cnt, cnt);
					
					// Check if previous Field is Unknown
					// then set Cluster to open as it might extend
					// to previous Field
					if(prevField == Field.UNKNOWN){
						currentCluster.open = true;
					}
				}
				break;
			case NOTHING:
				
				// End of Cluster detected
				if(currentCluster != null){
					
					// Add Cluster if its size meets the cluster size requirement
					if(currentCluster.size()>=CLUSTER_SIZE_THRESHOLD){
						clusters.add(currentCluster);
					}
					
					// Reset CurrentCluster
					currentCluster = null;
				}
				break;
			}
			
			// Increment Field-Counter
			cnt++;
			
			// Set previous Field
			prevField = entry.getValue();
		}
		
		return clusters;
	}
	
	/**
	 * <strong>Ship Cluster</strong><br>
	 * Private inner Class to describe Clusters of 0...n Ships
	 * where startIdx is the Clusters first Ship-position relative to Game.INTERVALS
	 * and endIdx is the Clusters last Ship-position
	 */
	@SuppressWarnings("unused")
	private class ShipCluster {
		
		// Indexes relative to the Players Fields
		// startIdx and endIdx are inclusive
		int startIdx;
		int endIdx;
		
		// Cluster might be bigger as currently known
		// when Field next to the Clusters boundaries is unknown
		boolean open;
		
		public ShipCluster(int startIdx, int endIdx){
			this.startIdx = startIdx;
			this.endIdx = endIdx;
			
			// Cluster is not open by default
			this.open = false;
		}
		
		/**
		 * <strong>Is Open</strong><br>
		 * Returns true if Cluster might be extended as 
		 * the Fields next to it are unknown
		 * @return isOpen
		 */
		public boolean isOpen(){
			return this.open;
		}
		
		/**
		 * <strong>Size</strong><br>
		 * Returns Size of Cluster
		 * @return size
		 */
		public int size(){
			return (this.endIdx-this.startIdx)+1;
		}
		
		/**
		 * <strong>Distance to</strong><br>
		 * Calculates the distance measured in Fields to another Cluster
		 * @param s
		 * @return distance in Fields
		 */
		public int distanceTo(ShipCluster s){
			if(this.startIdx<s.startIdx){
				return (s.startIdx-this.endIdx)-1;
			} else {
				return (this.startIdx-s.endIdx)-1;
			}
		}
		
	}

}

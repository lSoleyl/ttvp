package de.haw.ttvp.gamelogic.strategy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

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
		
		Map<Player, List<ID>> targets = new HashMap<Player, List<ID>>();
		
		for(Entry<ID, Player> p:playerMap.entrySet()){
			KnownPlayer player = (KnownPlayer) p.getValue();
			
			// Add targets if a Cluster-Pattern could be detected
			if(clusterPattern.detectPattern(player)){
				targets.put(player, clusterPattern.findTarget(player));
			
			// Add targets if a Linear-Pattern could be detected
			} else if(linearPattern.detectPattern(player)){
				targets.put(player, linearPattern.findTarget(player));
			
			// TODO Fallback
			} else {
				LOG.info("No pattern detected for Player ID="+player.getID().toHexString());
			}
		}
		
		//TODO determine Player
		
		return null;
	}

}

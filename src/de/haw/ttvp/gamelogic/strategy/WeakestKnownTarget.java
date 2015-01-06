package de.haw.ttvp.gamelogic.strategy;

import de.haw.ttvp.gamelogic.Field;
import de.haw.ttvp.gamelogic.player.KnownPlayer;
import de.uniba.wiai.lspi.chord.data.ID;
import java.util.Collection;
import java.util.Map.Entry;
import org.apache.log4j.Logger;

/** This strategy fires on the weakest player which is a known player.
 *  weakest = has lost most ships AND has fewest unknown slots left
 * 
 */
public class WeakestKnownTarget extends Strategy {
  public final Logger log = Logger.getLogger(WeakestKnownTarget.class);
  
  @Override
  public ID findTarget() {
     KnownPlayer player = selectWeakestKnownPlayer();
     ID target = null;
     
     if (player != null) {
       log.info("Targeting player: " + player.getID());
       
       //Find first unknown slot of the player
       for (Entry<ID,Field> entry : player.getFieldMap().entrySet())
         if (entry.getValue() == Field.UNKNOWN)
           return player.getInterval().getTargetID(entry.getKey());
       
       //Alle Schiffe des Spielers scheinen versenkt worden sein
       log.info("No UNKNOWN slot found for selected player.");
     }    
       
     //Kein Ziel gefunden mit dieser Strategie
     log.warn("Strategy failed to determine a target");
     return null;
  }

  
  
}
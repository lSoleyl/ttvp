/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.haw.ttvp.gamelogic;

import static de.haw.ttvp.gamelogic.Field.*;
import de.uniba.wiai.lspi.chord.data.ID;
import java.util.Map;

public class KnownPlayer implements Player {
  private IDInterval interval;
  private Map<ID, Field> shipMap;
  
  public KnownPlayer(IDInterval interval) {
    this.interval = interval;
  }
  
  
  @Override
  public boolean hasShipAt(ID target) {
    ID mapID = interval.getIntervalID(target);
    if (mapID != null)
      return shipMap.get(mapID) == SHIP;
    return false;
  }

  @Override
  public void setField(ID target, Field type) {
    ID mapID = interval.getIntervalID(target);
    if (mapID != null)
      shipMap.put(mapID, type);
  }

  @Override
  public Field getField(ID target) {
    ID mapID = interval.getIntervalID(target);
    if (mapID != null) {
      if (shipMap.containsKey(mapID))
        return shipMap.get(mapID);
      return UNKNOWN;
    }
    return null; //Feld nicht im Spielerintervall      
  }
  
}

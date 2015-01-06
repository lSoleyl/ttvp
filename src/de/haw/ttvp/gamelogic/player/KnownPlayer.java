/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.haw.ttvp.gamelogic.player;

import de.haw.ttvp.gamelogic.Field;
import static de.haw.ttvp.gamelogic.Field.*;
import de.haw.ttvp.gamelogic.IDInterval;
import de.uniba.wiai.lspi.chord.data.ID;

public class KnownPlayer extends Player {
  private final IDInterval interval;
  
  public KnownPlayer(ID nodeID, IDInterval interval) {
    super(nodeID);
    this.interval = interval;
  }
  
  
  @Override
  public boolean hasShipAt(ID target) {
    ID mapID = interval.getIntervalID(target);
    if (mapID != null)
      return fieldMap.get(mapID) == SHIP;
    return false;
  }

  @Override
  public void setField(ID target, Field type) {
    ID mapID = interval.getIntervalID(target);
    if (mapID != null)
      fieldMap.put(mapID, type);
  }

  @Override
  public Field getField(ID target) {
    ID mapID = interval.getIntervalID(target);
    if (mapID != null) {
      if (fieldMap.containsKey(mapID))
        return fieldMap.get(mapID);
      return UNKNOWN;
    }
    return null; //Feld nicht im Spielerintervall      
  }
  
}

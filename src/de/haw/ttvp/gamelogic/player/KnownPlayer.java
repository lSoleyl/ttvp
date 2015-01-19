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
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class KnownPlayer extends Player {
  protected final IDInterval interval;
  
  public KnownPlayer(ID nodeID, IDInterval interval) {
    super(nodeID);
    this.interval = interval;
    initFields();
  }
  
  /** Initialize all fields with UNKNOWN value
   */
  private void initFields() {
    for(ID slotID : interval.ids)
      fieldMap.put(slotID, UNKNOWN);
  }
  
  @Override
  public boolean hasShipAt(ID target) {
    return getField(target) == SHIP;
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
  
  @Override
  public boolean isKnown() {
    return true;
  }
  
  /** Returns the amount of unknown slots.
   */ 
  public int unknownSlots() {
    int unknown = 0;
    
    for (Field field : fieldMap.values())
      if (field == UNKNOWN)
        ++unknown;
    
    return unknown;
  }
  
  @Override
  public KnownPlayer known() {
    return this;
  }
  
  public IDInterval getInterval() {
    return interval;
  }
  
  @Override
  public boolean isInitialPlayer() {
    return interval.getIntervalID(ID.MAX_ID) != null;
  }
  
  @Override
  public String summary(boolean verbose) {
    List<Integer> knownShips = new ArrayList<>();
    
    for (int c = 0; c < interval.ids.size(); ++c)
      if (hasShipAt(interval.ids.get(c)))
        knownShips.add(c);
    
    
    return "-Known Player-\n" + 
      "ID-Range from: " + interval.from + "\n" + 
      "ID-Range to  : " + interval.to + "\n" +
      "known ships  : " + knownShips + "\n" + 
      super.summary(verbose);
  }
}

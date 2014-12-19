/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.haw.ttvp.gamelogic;

import de.uniba.wiai.lspi.chord.data.ID;


public class UnknownPlayer extends Player { 
  public UnknownPlayer(ID nodeID) {
    super(nodeID);
  }
  
  
  /** This implementation will only return true if the player is known to have a
   *  ship at exactly that ID.
   *
   * @param target the ship position
   * 
   * @return true if this player is know to have a ship at 'target'
   */
  @Override
  public boolean hasShipAt(ID target) {
    return fieldMap.get(target) == Field.SHIP;
  }

  /** Due to missing range information, the information is only entered into the field map
   */
  @Override
  public void setField(ID target, Field type) {
    fieldMap.put(target, type);
  }

  @Override
  public Field getField(ID target) {
    if (fieldMap.containsKey(target))
      return fieldMap.get(target);
    return Field.UNKNOWN;
  }
}

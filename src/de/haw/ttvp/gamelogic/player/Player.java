/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.haw.ttvp.gamelogic.player;

import de.haw.ttvp.gamelogic.Field;
import de.haw.ttvp.gamelogic.IDInterval;
import de.uniba.wiai.lspi.chord.data.ID;
import java.util.HashMap;
import java.util.Map;

public abstract class Player {
  protected ID nodeID;
  protected Map<ID, Field> fieldMap = new HashMap<>();

  public Player(ID nodeID) {
    this.nodeID = nodeID;
  }
  
  /** Returns true if the player is known to have a ship at this position. 
   *  This is known either because the ship got hit or because the ships belong
   *  to yourself.
   * 
   * @param target
   * @return 
   */
  public abstract boolean hasShipAt(ID target);
  
  /** Set type of a field. If the given ID doesn't belong to this player
   *  nothing happens.
   * 
   * @param target the field position
   * @param type the field type to set
   */
  public abstract void setField(ID target, Field type);
  
  /** Returns the field type for a given ID.
   * 
   * @param target the ID to search for
   * 
   * @return the field type of the selected ID. 
   *         If this ID doesn't belong to the player's interval
   *         the result is null!
   */
  public abstract Field getField(ID target);
  
  /** Return the Id of the player's node.
   * 
   * @return the player's id
   */
  public ID getID() {
    return nodeID;
  }
  
  public boolean isKnown() {
    return false;
  }
  
  public Player makeKnown(IDInterval range) {
    return this;
  }
}

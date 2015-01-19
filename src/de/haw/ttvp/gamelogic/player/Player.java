/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.haw.ttvp.gamelogic.player;

import de.haw.ttvp.gamelogic.Field;
import de.haw.ttvp.gamelogic.Game;
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
  
  /** This returns how many ships this player has already lost.
   *  This implementation works for both kown and unknown players thanks to the
   *  fact that no ship can be sunken twice.
   * 
   * @return number of ships, which have been destroyed
   */
  public int shipsLost() {
    int lostShips = 0;
    
    for (Field field : fieldMap.values()) //Einfach alle "Schiff"-Felder zählen
      if (field == Field.SHIP)
        ++lostShips;
      
    return lostShips;
  }
  
  /** Returns the number of how often other player have shot into this player's range
   * 
   * @return hit count
   */
  public int hitCount() {
    return Game.instance.history.getHitCount(this);
  }
  
  /** The amount of shots the player has made
   * 
   * @return shot count
   */
  public int shotCount() {
    int shots = (isInitialPlayer()) ? 1 : 0; //Als erster Spieler hat man einen Schuss mehr
    if (nodeID.equals(Game.instance.history.getLoser())) //Als letzter Spieler hat man einen Schuss weniger
      --shots;
    
    shots += hitCount();
    return shots;
  }
  
  /** Returns the number of ships this player has destroyed
   * 
   * @return the number of ships this node has destroyed
   */
  public int destructionCount() {
    return Game.instance.history.getDestructionCount(this);
  }
  
  /** Returns the ratio of (remaining ships)/(hits received)
   * 
   * @return the ships hit ratio
   */ 
  public double shipsHitsRatio() {
    return (Game.SHIPS - shipsLost()) / ((double)hitCount());
  }
  
  /** Returns the ratio of (shots made)/(ships destroyed)
   * 
   * @return the shots destruction ratio
   */
  public double shotsDestructsRatio() {
    return shotCount() / ((double) destructionCount());
  }
  
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
  
  public KnownPlayer known() {
    return null;
  }
  
  public Map<ID, Field> getFieldMap() {
    return fieldMap;
  }
  
  /** Returns a string representation of the currently known data of the player
   * 
   * @param verbose output more information
   * 
   * @return a string representation of the player
   */
  public String summary(boolean verbose) {
    return "Node at: " + nodeID;
  }
  
  public boolean isInitialPlayer() {
    return false;
  }
}

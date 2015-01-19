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
import java.util.Map.Entry;


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
   *  Synchronized, because it might conflict with makeKnown()
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
  
  /** Converts the UnknownPlayer into a KnownPlayer by providing the range and reapplying the
   *  fieldMap to the new player.
   *  This method is synchronized due to possible conflicts with setField()
   * 
   * @param range the interval Range, which defines this node
   * @return a new KnownPlayer
   */
  @Override
  public Player makeKnown(IDInterval range) {
    Player knownPlayer = new KnownPlayer(nodeID, range);
    
    //Bekannte Informationen auf neuen Spieler übertragen
    fieldMap.entrySet().stream().forEach((info) -> {
      knownPlayer.setField(info.getKey(), info.getValue());
    });
    
    return knownPlayer;
  }
  
  @Override
  public String summary(boolean verbose) {
    int hitCount = Game.instance.history.getHitCount(this);
    
    return "-Unknown Player-\n" + 
      super.summary(verbose) + "\n" + 
      "ships lost  : " + shipsLost() + "\n" + 
      "was shot at : " + hitCount + " times\n" + 
      "ships/hits  : " + ((double)shipsLost() / hitCount) + "\n";
      
  }
}

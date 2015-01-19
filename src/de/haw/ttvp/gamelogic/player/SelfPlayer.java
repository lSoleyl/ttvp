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


public class SelfPlayer extends KnownPlayer {
  private int shipsLost = 0;
  
  public SelfPlayer(ID id, IDInterval range) {
    super(id, range);
  }
  
  @Override
  public void setField(ID target, Field type) {
    if (getField(target) == SHIP && type == NOTHING) //Shiff versenken
      ++shipsLost;
    
    super.setField(target, type);
  }
  
  @Override
  public int shipsLost() {
    return shipsLost;
  }
}

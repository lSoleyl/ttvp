package de.haw.ttvp.gamelogic;

import de.uniba.wiai.lspi.chord.data.ID;
import de.uniba.wiai.lspi.chord.service.ServiceException;

public class AsyncInvoke {
  public static void broadcast(ID target, boolean hit) {
    if (Game.USE_ASYNC_CHORD_CALLS)
      new Thread(() -> callBroadcast(target,hit)).start();
    else
      callBroadcast(target, hit);
  }
  
  private static void callBroadcast(ID target, boolean hit) {
    Game.instance.getChord().broadcast(target, hit);
  }
  
  public static void retrive(ID key) throws ServiceException {
    if (Game.USE_ASYNC_CHORD_CALLS)
      new Thread(() -> {
        try { Game.instance.getChord().retrieve(key); } 
        catch (ServiceException ex) {} }).start();
    else 
      Game.instance.getChord().retrieve(key);
  }
}
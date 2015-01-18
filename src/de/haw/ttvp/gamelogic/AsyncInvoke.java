package de.haw.ttvp.gamelogic;

public class AsyncInvoke {
  public static void invoke(Runnable code) {
    if (Game.USE_ASYNC_CHORD_CALLS)
      new Thread(code).start();
    else
      code.run();          
  }
}
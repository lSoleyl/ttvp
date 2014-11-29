package de.haw.ttvp;

import org.apache.log4j.Logger;

/** Klasse zur Verwaltung der Transaktions-IDs
 */
public final class Transaction {
  private static int currentID = 0;
  private static Logger logger = Logger.getLogger(Transaction.class);
  
  public static void updateID(int id) {
    if (id != currentID + 1)
      logger.warn("! Skipping IDs. Received (" + id + ") but nextID should be(" + (currentID + 1) + ")");
    currentID = id;
  }
  
  /** Checks whether the given ID has already been seen and a broadcast having this ID should be dropped
   */
  public static boolean validID(int id) {
    if (!(id >= currentID || currentID > (id + Integer.MAX_VALUE/2))) {
      logger.warn("! Received transaction ID is lower than the last ID. Received ID: (" + 
              id + ")  last ID: (" + currentID + ")");
      return false;
    }
    return true;
  }
  
  public static int nextID() {
    return ++currentID;
  }
  
  public static int current() {
    return currentID;
  }
}

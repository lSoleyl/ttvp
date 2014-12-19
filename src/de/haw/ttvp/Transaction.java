package de.haw.ttvp;

import org.apache.log4j.Logger;

/** Klasse zur Verwaltung der Transaktions-IDs
 */
public final class Transaction {
  //Auf false setzen, wenn es clients gibt, die die ID um mehr als 1 inkrementieren
  private final static boolean ASSUME_SINGLE_INCREMENT = true;
  private static Logger logger = Logger.getLogger(Transaction.class);
  
  public static boolean validIDFrom(int from, int to) {
    if (ASSUME_SINGLE_INCREMENT) { //ID wird strikt um 1 inkrementiert (einfacher für uns!)
      return to == from+1 || 
            (from+1 == Integer.MIN_VALUE && to == 0); //Falls Überlauf zu 0 hochgerundet wurde.
    } else {
      if (!(to >= from || from > (to + Integer.MAX_VALUE/2)))
        return false;
      return true;
    }
  }
}

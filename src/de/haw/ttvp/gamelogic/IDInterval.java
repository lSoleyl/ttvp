package de.haw.ttvp.gamelogic;

import de.uniba.wiai.lspi.chord.data.ID;
import java.math.BigInteger;
import java.util.ArrayList;



/** Klasse, die einen ID-Bereich äquidistante Intervalle unterteilt
 *  Die untere Intervallgrenze ist dabei exklusiv, die obere inklusiv
 * 
 * Bsp: Int(0,93,10) -> [1-10][11-20][21-30]...[81-83] (Rest wird auf das letzte Intervall gelegt)
 *      Als ID-Array -> 1,11,21,...,81 (Das ID-Array enthält die kleineren Werte des Intervals)
 * 
 * Bsp2: Int(20,10,10) [Bei einem ID-Ring von 100 Elementen]
 *          -> [21-29][30-38][39-47][48-56][57-65] [66-74][75-83][84-92][93-99&0-1][2-10]
 *      Als ID-Array -> 21,30,39,48,57,66,75,84,93,2
 */
public class IDInterval {
  public final ID from;
  public final ID to;
  
  private BigInteger halfStepSize;
  
  public ArrayList<ID> ids = new ArrayList<>(); //Liste mit unteren Intervallgrenzen der Teilintervalle

  public IDInterval(ID from, ID to, int intervals) {
    this.from = from;
    this.to = to;
    populateIds(intervals);
  }
  
  public IDInterval(ID from, ID to) {
    this.from = from;
    this.to = to;
    populateIds(Game.INTERVALS);
  }
  
  private void populateIds(int intervals) {
    BigInteger intervalSize = from.distanceTo(to);
    BigInteger stepSize = intervalSize.divide(BigInteger.valueOf(intervals));
    halfStepSize = stepSize.divide(BigInteger.valueOf(2));
    
    ID currentID = from.add(1); //Startwert aus dem Interval ausnehmen
    for (int c = 0; c < intervals; ++c) {
      ids.add(currentID);
      currentID = currentID.add(stepSize);
    }
  }
  
  /** Looks up the given ID inside the intervallist and returns the ID which 
   *  identifies the interval itself.
   *
   * @param target the ID to look up
   * 
   * @return the ID which identifies the interval(see 'ids'). Or null, if outside the interval
   */
  public ID getIntervalID(ID target) {
    ID current = null;
    for(ID i : ids) {
      if (i.compareTo(target) > 0)
        return current;
      if (i.compareTo(target) == 0)
        return i;
      
      current = i;
    }
    return null;
  }
  
  /**
   * <strong>Get TargetID</strong><br>
   * Returns the Target-ID for a given Element in this Interval
   * @param idx
   * @return Target ID
   */
  public ID getTargetID(int idx){
	  ID targetID = ids.get(idx);
	  
	  // Calculate Target
	  return targetID.add(halfStepSize);
  }
  
  public ID getTargetID(ID rangeBegin) {
    return rangeBegin.add(halfStepSize);
  }
}

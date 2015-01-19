package de.haw.ttvp.gamelogic;

import de.haw.ttvp.Transaction;
import de.haw.ttvp.gamelogic.player.Player;
import de.uniba.wiai.lspi.chord.data.ID;
import de.uniba.wiai.lspi.util.logging.Logger;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class History {
  private static final Logger log = Logger.getLogger(History.class);
  
	private LinkedList<HistoryEntry> entries;
  
  private ID loserNode;
  private ID winnerNode;
  private boolean gameOver = false;
	
	public History(){
		this.entries = new LinkedList<>(); //Linked list performanter beim Einfügen.
	}
	
	public class HistoryEntry {
		public int transactionID;
		public ID dstPlayer; 
		public ID targetID;
		public boolean hit;
		
		public HistoryEntry(int transactionID, ID dstPlayer, ID targetID, boolean hit) {
			this.transactionID = transactionID;
			this.dstPlayer = dstPlayer;
			this.targetID = targetID;
			this.hit = hit;
		}
		
	}
  
  //Einfache Duplikatsprüfung für Broadcast
  public boolean isSimpleDuplicate(int transactionID) {
    return !entries.isEmpty() && entries.getLast().transactionID == transactionID;
  }
	
	public void addEntry(HistoryEntry entry){
    if (gameOver) {
      log.warn("Trying to insert entry into History, after game has finished!");
      return;
    }
    
    if (entries.isEmpty() || Transaction.validIDFrom(entries.getLast().transactionID, entry.transactionID)) {
      entries.add(entry);
      return;
    }
    
    //ID ist "out-of-order" oder ein Duplikat
    for(int i = entries.size()-1; i >= 0; --i) {
      HistoryEntry e = entries.get(i);
      if (e.transactionID == entry.transactionID) {
        log.warn("Dropping duplicate broadcast with Transaction-ID: " + e.transactionID);
        //TODO prüfen, ob Einträge identisch sind und Unterschiede ausgeben
        return;
      }
      if (Transaction.validIDFrom(e.transactionID, entry.transactionID)) {
        entries.add(i, entry);
        log.info("Added delayed Entry with Transaction-ID: " + entry.transactionID + ". Arrived out of order");
        return;
      }
    } 
		
	}
  
  public void addEntry(int transactionID,	ID dstPlayer, ID targetID, boolean hit) {
    addEntry(new HistoryEntry(transactionID, dstPlayer, targetID, hit));
  }
	
	public List<HistoryEntry> getEntries(){
		return this.entries;
	}
  
  /** Returns the next transaction id according to the history.
   * 
   * @return a new transaction id
   */
  public int getNextID() {
    if (entries.isEmpty())
      return 0; //Keine ID bekannt, neue starten
    
    //Da entries nach transactionIds sortiert sind, liefert getLast() die höchste ID
    return entries.getLast().transactionID + 1;
  }
  
  public void print() {
    log.info("---------- History Content ------------");
    Player starter = Game.instance.getInitialPlayer();
    String attacker = (starter != null) ? starter.getID().toString() : "???";
    int lastID = -1;
    
    for(HistoryEntry e : entries) {
      String hit = (e.hit) ? "X" : " ";
      Player p = Game.instance.getPlayer(e.dstPlayer);
      String slot = "ID:" + e.targetID;
      if (p.isKnown())
        slot = "Slot#" + p.known().getInterval().getIntervalIndex(e.targetID);
      
      if (lastID != e.transactionID - 1)
        attacker = "???";
      
      log.info(String.format("%4d - %s  (%s) -> (%s)[%s]", e.transactionID, hit, 
              attacker, e.dstPlayer, slot));
      
      lastID = e.transactionID;
      attacker = e.dstPlayer.toString();
    }
    
  }
  
  /** Returns the NodeID of the attacker for the corresponding transaction ID
   * 
   * @param transactionID the entry-id for which the attacker should be retrived
   *  
   * @return the ID of the attacker, or null, if he isn't known due to a missing entry
   */
  public ID getAttacker(int transactionID) {
    for (int c = entries.size() - 1; c >= 0; --c) 
      if (entries.get(c).transactionID == transactionID - 1)
        return entries.get(c).dstPlayer;
    
    if (transactionID == 0) {
      Player starter = Game.instance.getInitialPlayer();
      if (starter != null)
        return starter.getID();
    }
      
    return null;
  }
  
  /** Returns the amount of entries in which the player has been shot at.
   * 
   * @param player the player to get the info of
   * 
   * @return the amount of ships, this player has lost
   */
  public int getHitCount(Player player) {
    ID nodeID = player.getID();
    return (int)entries.stream().filter((entry) -> entry.dstPlayer.equals(nodeID)).count();
  }
  
  /** Returns a set of node ids of players, which attacked the given player
   * 
   * @param player the player whose attacker should be retrieved
   * 
   * @return the set containing all players, who attacked the given player
   */
  public Set<ID> getAttackers(ID player) {
    Set<ID> attackers = new HashSet<>();
    for (HistoryEntry entry : entries) {
      if (entry.dstPlayer.equals(player)) {
        ID attacker = getAttacker(entry.transactionID);
        if (attacker != null)
          attackers.add(attacker);
      }
    }
    
    return attackers;
  }
  
  /** Returns the amout of ships the given player has destroyed.
   * 
   * @param player the player whose information should be queried
   * 
   * @return the amount of ships the player has destroyed
   */
  public int getDestructionCount(Player player) {
    ID nodeID = player.getID();
    int destructions = 0;
    
    Player starter = Game.instance.getInitialPlayer();
    ID lastAttacker = (starter != null) ? starter.getID() : null;
    int lastTid = -1;
    
    for (HistoryEntry entry : entries) {
      if (entry.transactionID == lastTid + 1)  //Angreifer bekannt
        if (entry.hit && lastAttacker != null && lastAttacker.equals(nodeID)) //Ziel getroffen und korrekter Angreifer
          ++destructions;
      
      lastTid = entry.transactionID;
      lastAttacker = entry.dstPlayer;
    }
    
    return destructions;
  }
  
  /** Make the history read only and enter the winner and loser
   */
  public void finalize(int transactionID, ID source, ID target, Boolean hit) {
    addEntry(transactionID, source, target, true);
    gameOver = true;
    
    loserNode = source;
    winnerNode = getAttacker(transactionID);
  }
  
  public ID getWinner() {
    return winnerNode;
  }
  
  public ID getLoser() {
    return loserNode;
  }
}

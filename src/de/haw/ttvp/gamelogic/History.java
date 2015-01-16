package de.haw.ttvp.gamelogic;

import de.haw.ttvp.Transaction;
import de.haw.ttvp.gamelogic.player.Player;
import de.uniba.wiai.lspi.chord.data.ID;
import de.uniba.wiai.lspi.util.logging.Logger;
import java.util.LinkedList;
import java.util.List;

public class History {
  private static final Logger log = Logger.getLogger(History.class);
  
	private LinkedList<HistoryEntry> entries;
	
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
    String attacker = "???";
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
}

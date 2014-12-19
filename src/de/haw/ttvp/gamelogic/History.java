package de.haw.ttvp.gamelogic;

import de.haw.ttvp.Transaction;
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
		public Player dstPlayer;
		public int slot;
		public boolean hit;
		
		public HistoryEntry(int transactionID, Player dstPlayer, int slot, boolean hit) {
			this.transactionID = transactionID;
			this.dstPlayer = dstPlayer;
			this.slot = slot;
			this.hit = hit;
		}
		
	}
  
  //Einfache Duplikatsprüfung für Broadcast
  public boolean isSimpleDuplicate(int transactionID) {
    return !entries.isEmpty() && entries.getLast().transactionID == transactionID;
  }
	
	public void addEntry(HistoryEntry entry){
    if (entries.isEmpty() || Transaction.validIDFrom(entries.getLast().transactionID, entry.transactionID))
      entries.add(entry);
    
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
  
  public void addEntry(int transactionID,	Player dstPlayer, int slot, boolean hit) {
    addEntry(new HistoryEntry(transactionID, dstPlayer, slot, hit));
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
}

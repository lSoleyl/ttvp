package de.haw.ttvp.gamelogic;

import java.util.ArrayList;
import java.util.List;

public class History {

	private List<HistoryEntry> entries;
	
	public History(){
		this.entries = new ArrayList<>();
	}
	
	class HistoryEntry{
		public int transactionID;
		public Player srcPlayer;
		public Player dstPlayer;
		public int slot;
		public boolean hit;
		
		public HistoryEntry(int transactionID, Player srcPlayer,
				Player dstPlayer, int slot, boolean hit) {
			super();
			this.transactionID = transactionID;
			this.srcPlayer = srcPlayer;
			this.dstPlayer = dstPlayer;
			this.slot = slot;
			this.hit = hit;
		}
		
	}
	
	public void addEntry(HistoryEntry entry){
		this.entries.add(entry);
	}
	
	public List<HistoryEntry> getEntries(){
		return this.entries;
	}
}

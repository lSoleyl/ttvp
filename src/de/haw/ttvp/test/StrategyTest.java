package de.haw.ttvp.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Test;

import de.haw.ttvp.gamelogic.Field;
import de.haw.ttvp.gamelogic.History;
import de.haw.ttvp.gamelogic.IDInterval;
import de.haw.ttvp.gamelogic.player.KnownPlayer;
import de.haw.ttvp.gamelogic.player.Player;
import de.haw.ttvp.gamelogic.player.SelfPlayer;
import de.haw.ttvp.gamelogic.strategy.ClusterDistributionPattern;
import de.haw.ttvp.gamelogic.strategy.PatternStrategy;
import de.haw.ttvp.gamelogic.strategy.WeakestKnownTarget;
import de.uniba.wiai.lspi.chord.data.ID;

/**
 * <strong>Strategy Test</strong><br>
 * JUnit attempt to Test the Game strategies
 * 
 * TODO Static Game References are not working with this Tests
 * For successful testing the static references have to be removed from 
 * the Classes to be tested
 */
public class StrategyTest {
	
	// Player Self
	private final static String ID_STRING_1 = "E5 6E 90 1B FE E2 0F C4 37 14 08 4F 7B 51 93 DE 0F FD 25 D1 ";
	
	// Other Players
	private final static String ID_STRING_2 = "52 61 4D 1D C3 1E DD 5D 3E 0E 07 0D 15 EB 6F 58 C2 AF 20 BE ";
	private final static String ID_STRING_3 = "76 AF 20 32 69 BD F2 41 CF 51 DB F8 75 4C C5 C7 9B 7E 33 1E ";
	private final static String ID_STRING_4 = "BD EC E7 50 AB 73 3C 8C CE 21 9F 04 31 C5 72 EA 6B 39 5C 81 ";
	
	@Test 
	public void testWeakestKnownTargetStrategy(){
		
		Map<ID, Player> playerMap = new HashMap<>();
//		History history = new History();
		
		// Self Player
		ID selfID = new ID(hexStringToByteArray(ID_STRING_1.replaceAll(" ", "")));
		assertTrue(ID_STRING_1.equals(selfID.toHexString()));
		
		ID playerID2 = new ID(hexStringToByteArray(ID_STRING_2.replaceAll(" ", "")));
		assertTrue(ID_STRING_2.equals(playerID2.toHexString()));
		ID playerID3 = new ID(hexStringToByteArray(ID_STRING_3.replaceAll(" ", "")));
		assertTrue(ID_STRING_3.equals(playerID3.toHexString()));
		ID playerID4 = new ID(hexStringToByteArray(ID_STRING_4.replaceAll(" ", "")));
		assertTrue(ID_STRING_4.equals(playerID4.toHexString()));
		
		// Setup 4 Players including own Player
//		SelfPlayer self = new SelfPlayer(selfID, new IDInterval(playerID4, selfID));
		KnownPlayer p2 = new KnownPlayer(playerID2, new IDInterval(selfID, playerID2));
		KnownPlayer p3 = new KnownPlayer(playerID3, new IDInterval(playerID2, playerID3));
		KnownPlayer p4 = new KnownPlayer(playerID4, new IDInterval(playerID3, playerID4));
		
		// Add players to Map
		playerMap.put(playerID2, p2);
		playerMap.put(playerID3, p3);
		playerMap.put(playerID4, p4);
		
		// Initialize Player 2: 2 Hits 3 misses
		int cnt = 0;
		for(Entry<ID, Field> entry:p2.getFieldMap().entrySet()){
			if(cnt == 3 || cnt == 16 || cnt == 53){
				entry.setValue(Field.NOTHING);
			} else if(cnt == 6 || cnt == 77){
				entry.setValue(Field.SHIP);
			}
			cnt++;
		}
		
		// Initialize Player 3: 3 Hits 2 misses
		cnt = 0;
		for(Entry<ID, Field> entry:p3.getFieldMap().entrySet()){
			if(cnt == 2 || cnt == 33 || cnt == 12){
				entry.setValue(Field.SHIP);
			} else if(cnt == 6 || cnt == 77){
				entry.setValue(Field.NOTHING);
			}
			cnt++;
		}
		
		// Initialize Player 3: 4 Hits 2 misses
		cnt = 0;
		for(Entry<ID, Field> entry:p4.getFieldMap().entrySet()){
			if(cnt == 6 || cnt == 23 || cnt == 12 || cnt == 77){
				entry.setValue(Field.SHIP);
			} else if(cnt == 13 || cnt == 97){
				entry.setValue(Field.NOTHING);
			}
			cnt++;
		}
		
		// Get WeakestPlayer
//		ID target = WeakestKnownTarget.instance(playerMap, history, self).findTarget();
		ID target = WeakestKnownTarget.instance().findTarget();
		assertTrue("Field not in Player Range", p4.getField(target) != null);
		
	}
	
	@Test
	public void testPassiveMode(){
		System.out.println("\n\n\n\n # TESTING PASSIVE MODE \n\n");
		Map<ID, Player> playerMap = new HashMap<>();
		History history = new History();
		
		// Self Player
		ID selfID = new ID(hexStringToByteArray(ID_STRING_1.replaceAll(" ", "")));
		assertTrue(ID_STRING_1.equals(selfID.toHexString()));
		
		ID playerID2 = new ID(hexStringToByteArray(ID_STRING_2.replaceAll(" ", "")));
		assertTrue(ID_STRING_2.equals(playerID2.toHexString()));
		ID playerID3 = new ID(hexStringToByteArray(ID_STRING_3.replaceAll(" ", "")));
		assertTrue(ID_STRING_3.equals(playerID3.toHexString()));
		ID playerID4 = new ID(hexStringToByteArray(ID_STRING_4.replaceAll(" ", "")));
		assertTrue(ID_STRING_4.equals(playerID4.toHexString()));
		
		// Setup 4 Players including own Player
		SelfPlayer self = new SelfPlayer(selfID, new IDInterval(playerID4, selfID));
		KnownPlayer p2 = new KnownPlayer(playerID2, new IDInterval(selfID, playerID2));
		KnownPlayer p3 = new KnownPlayer(playerID3, new IDInterval(playerID2, playerID3));
		KnownPlayer p4 = new KnownPlayer(playerID4, new IDInterval(playerID3, playerID4));
		
		// Add players to Map
		playerMap.put(playerID2, p2);
		playerMap.put(playerID3, p3);
		playerMap.put(playerID4, p4);
		
		// Initialize Player 2: 2 Hits 3 misses
		int cnt = 0;
		for(Entry<ID, Field> entry:p2.getFieldMap().entrySet()){
			if(cnt == 3 || cnt == 16 || cnt == 53){
				entry.setValue(Field.NOTHING);
			} else if(cnt == 6 || cnt == 77){
				entry.setValue(Field.SHIP);
			}
			cnt++;
		}
		
		// Initialize Player 3: 3 Hits 2 misses
		cnt = 0;
		for(Entry<ID, Field> entry:p3.getFieldMap().entrySet()){
			if(cnt == 2 || cnt == 33 || cnt == 12){
				entry.setValue(Field.SHIP);
			} else if(cnt == 6 || cnt == 77){
				entry.setValue(Field.NOTHING);
			}
			cnt++;
		}
		
		// Initialize Player 3: 4 Hits 2 misses
		cnt = 0;
		for(Entry<ID, Field> entry:p4.getFieldMap().entrySet()){
			if(cnt == 6 || cnt == 23 || cnt == 12 || cnt == 77){
				entry.setValue(Field.SHIP);
			} else if(cnt == 13 || cnt == 97){
				entry.setValue(Field.NOTHING);
			}
			cnt++;
		}
		
		// Initialize own Player: 5 Hits 0 misses
		cnt = 0;
		for(Entry<ID, Field> entry:self.getFieldMap().entrySet()){
			if(cnt == 6 || cnt == 23 || cnt == 12 || cnt == 77 || cnt == 2){
				self.setField(entry.getKey(), Field.SHIP);
				self.setField(entry.getKey(), Field.NOTHING);
			} else if(cnt == 13 || cnt == 97 || cnt == 11 || cnt == 14){
				self.setField(entry.getKey(), Field.NOTHING);
			}
			cnt++;
		}
		
		// Add History entries for Player 3 & 4 who shoot at this Player
		history.addEntry(0, p4.getID(), p4.getID(), false);
		history.addEntry(1, self.getID(), self.getID(), false);
		history.addEntry(2, p3.getID(), p3.getID(), false);
		history.addEntry(3, self.getID(), self.getID(), false);
		history.addEntry(4, p4.getID(), p4.getID(), false);
		history.addEntry(5, self.getID(), self.getID(), false);
		
		// Make Sure Player 4 is weakest Player
//		assertTrue("Player 4 not weakest", PatternStrategy.instance(playerMap, history, self).selectWeakestKnownPlayer().getID().equals(p4.getID()));
//		assertTrue("Self Player not weaker than Player 4", PatternStrategy.instance(playerMap, history, self).isWeaker(self, p4));
		
//		ID target = PatternStrategy.instance(playerMap, history, self).findTarget();
		ID target = PatternStrategy.instance().findTarget();
		
		assertNotNull("Target is Null", target);
		assertTrue("Field not in Player Range", p2.getField(target) != null);
		
		history.addEntry(6, p2.getID(), p2.getID(), false);
		history.addEntry(7, self.getID(), self.getID(), false);
		
//		target = PatternStrategy.instance(playerMap, history, self).findTarget();
		target = PatternStrategy.instance().findTarget();
		assertNotNull("Target is Null", target);
	}
	
	@Test
	public void testActiveMode(){
		System.out.println("\n\n\n\n # TESTING ACTIVE MODE \n\n");
		Map<ID, Player> playerMap = new HashMap<>();
		History history = new History();
		
		// Self Player
		ID selfID = new ID(hexStringToByteArray(ID_STRING_1.replaceAll(" ", "")));
		assertTrue(ID_STRING_1.equals(selfID.toHexString()));
		
		ID playerID2 = new ID(hexStringToByteArray(ID_STRING_2.replaceAll(" ", "")));
		assertTrue(ID_STRING_2.equals(playerID2.toHexString()));
		ID playerID3 = new ID(hexStringToByteArray(ID_STRING_3.replaceAll(" ", "")));
		assertTrue(ID_STRING_3.equals(playerID3.toHexString()));
		ID playerID4 = new ID(hexStringToByteArray(ID_STRING_4.replaceAll(" ", "")));
		assertTrue(ID_STRING_4.equals(playerID4.toHexString()));
		
		// Setup 4 Players including own Player
		SelfPlayer self = new SelfPlayer(selfID, new IDInterval(playerID4, selfID));
		KnownPlayer p2 = new KnownPlayer(playerID2, new IDInterval(selfID, playerID2));
		KnownPlayer p3 = new KnownPlayer(playerID3, new IDInterval(playerID2, playerID3));
		KnownPlayer p4 = new KnownPlayer(playerID4, new IDInterval(playerID3, playerID4));
		
		// Add players to Map
		playerMap.put(playerID2, p2);
		playerMap.put(playerID3, p3);
		playerMap.put(playerID4, p4);
		
		// Initialize Player 2: 2 Hits 3 misses
		int cnt = 0;
		for(Entry<ID, Field> entry:p2.getFieldMap().entrySet()){
			if(cnt == 3 || cnt == 16 || cnt == 53){
				entry.setValue(Field.NOTHING);
			} else if(cnt == 6 || cnt == 77){
				entry.setValue(Field.SHIP);
			}
			cnt++;
		}
		
		// Initialize Player 3: 3 Hits 2 misses
		cnt = 0;
		for(Entry<ID, Field> entry:p3.getFieldMap().entrySet()){
			if(cnt == 2 || cnt == 33 || cnt == 12){
				entry.setValue(Field.SHIP);
			} else if(cnt == 6 || cnt == 77){
				entry.setValue(Field.NOTHING);
			}
			cnt++;
		}
		
		// Initialize Player 3: 9 Hits 2 misses
		// -> Dying Player
		cnt = 0;
		for(Entry<ID, Field> entry:p4.getFieldMap().entrySet()){
			if(cnt == 6 || cnt == 23 || cnt == 12 || cnt == 77 || cnt == 10 ||
					cnt == 16 || cnt == 24 || cnt == 25 || cnt == 78){
				entry.setValue(Field.SHIP);
			} else if(cnt == 13 || cnt == 97){
				entry.setValue(Field.NOTHING);
			}
			cnt++;
		}
		
		// Initialize own Player: 5 Hits 0 misses
		cnt = 0;
		for(Entry<ID, Field> entry:self.getFieldMap().entrySet()){
			if(cnt == 6 || cnt == 23 || cnt == 12){
				self.setField(entry.getKey(), Field.SHIP);
				self.setField(entry.getKey(), Field.NOTHING);
			} else if(cnt == 13 || cnt == 97 ){
				self.setField(entry.getKey(), Field.NOTHING);
			} else {
				self.setField(entry.getKey(), Field.UNKNOWN);
			}
			cnt++;
		}
		
		// Add History entries for Player 3 & 4 who shoot at this Player
		history.addEntry(0, p3.getID(), p3.getID(), false);
		history.addEntry(1, p2.getID(), p2.getID(), false);
		history.addEntry(2, p3.getID(), p3.getID(), false);
		history.addEntry(3, p4.getID(), p4.getID(), false);
		history.addEntry(4, p2.getID(), p2.getID(), false);
		history.addEntry(5, p4.getID(), p4.getID(), false);
		
		// Make Sure Player 4 is weakest Player
//		assertTrue("Player 4 not weakest", PatternStrategy.instance(playerMap, history, self).selectWeakestKnownPlayer().getID().equals(p4.getID()));
//		assertTrue("Self Player weaker than Player 4", PatternStrategy.instance(playerMap, history, self).isWeaker(p4, self));
		
//		ID target = PatternStrategy.instance(playerMap, history, self).findTarget();
		ID target = PatternStrategy.instance().findTarget();
		
		// Make sure dying Player will be targetted
		assertNotNull("Target is Null", target);
		assertTrue("Field not in Player Range", p4.getField(target) != null);
		
		// Initialize Player 3: 4 Hits 2 misses
		cnt = 0;
		for(Entry<ID, Field> entry:p4.getFieldMap().entrySet()){
			if(cnt == 6 || cnt == 23 || cnt == 12 || cnt == 77 ){
				entry.setValue(Field.SHIP);
			} else if(cnt == 13 || cnt == 97){
				entry.setValue(Field.NOTHING);
			} else {
				entry.setValue(Field.UNKNOWN);
			}
			cnt++;
		}
		
//		target = PatternStrategy.instance(playerMap, history, self).findTarget();
		target = PatternStrategy.instance().findTarget();
		
		// Make Backshooting target will be choosen
		assertNotNull("Target is Null", target);
		assertTrue("Field not in Player Range", p2.getField(target) != null);
		
		// former backshooting Target is now invalid
		history.addEntry(6, p2.getID(), p2.getID(), false);
		history.addEntry(7, p3.getID(), p3.getID(), false);
		
//		target = PatternStrategy.instance(playerMap, history, self).findTarget();
		target = PatternStrategy.instance().findTarget();
		
		// Make Backshooting target will be choosen
		assertNotNull("Target is Null", target);
		assertTrue("Field not in Player Range", p2.getField(target) == null);
		
		//TODO Testing of most inactive Player pending due to static Game references
	}
	
	@Test
	public void testClusterDistributionPattern(){
		System.out.println("\n\n\n\n # TESTING Cluster DistributionPattern \n\n");
		
		// Player
		ID pID = new ID(hexStringToByteArray(ID_STRING_1.replaceAll(" ", "")));
		assertTrue(ID_STRING_1.equals(pID.toHexString()));
		ID pID2 = new ID(hexStringToByteArray(ID_STRING_4.replaceAll(" ", "")));
		assertTrue(ID_STRING_4.equals(pID2.toHexString()));
		
		KnownPlayer player = new KnownPlayer(pID, new IDInterval(pID, pID2));
		
		// Initialize Player
		int cnt = 0;
		for(Entry<ID, Field> entry:player.getFieldMap().entrySet()){
			if(cnt == 3 || cnt == 16 || cnt == 53){
				entry.setValue(Field.NOTHING);
			} else if(cnt == 6 || cnt == 77){
				entry.setValue(Field.SHIP);
			} else {
				entry.setValue(Field.UNKNOWN);
			}
			cnt++;
		}
		
		ClusterDistributionPattern pattern = ClusterDistributionPattern.getInstance();
		assertTrue("Detected Cluster where there should not be any", !pattern.detectPattern(player));
		
		// Initialize Player
		cnt = 0;
		for(Entry<ID, Field> entry:player.getFieldMap().entrySet()){
			if(cnt == 3 || cnt == 44 || cnt == 55){
				entry.setValue(Field.NOTHING);
			} else if(cnt == 65 || cnt == 66){
				entry.setValue(Field.SHIP);
			} else {
				entry.setValue(Field.UNKNOWN);
			}
			cnt++;
		}
		assertTrue("Did not detect Cluster", pattern.detectPattern(player));
		
		List<ID> targetList = pattern.findTarget(player);
		assertTrue("TargetList of wrong size", targetList.size()==2);
		
		ID testID = null;
		cnt = 0;
		for(Entry<ID, Field> entry:player.getFieldMap().entrySet()){
			if(cnt == 64){
				testID = entry.getKey();
			}
			cnt++;
		}
		assertTrue("TargetList contains wrong value", targetList.get(0).equals(testID));
		
		testID = null;
		cnt = 0;
		for(Entry<ID, Field> entry:player.getFieldMap().entrySet()){
			if(cnt == 67){
				testID = entry.getKey();
			}
			cnt++;
		}
		assertTrue("TargetList contains wrong value", targetList.get(1).equals(testID));
		
		
		// Initialize Player
		cnt = 0;
		for(Entry<ID, Field> entry:player.getFieldMap().entrySet()){
			if(cnt == 3 || cnt == 44 || cnt == 67){
				entry.setValue(Field.NOTHING);
			} else if(cnt == 65 || cnt == 66){
				entry.setValue(Field.SHIP);
			} else {
				entry.setValue(Field.UNKNOWN);
			}
			cnt++;
		}
		assertTrue("Did not detect Cluster", pattern.detectPattern(player));
		
		targetList = pattern.findTarget(player);
		assertTrue("TargetList of wrong size", targetList.size()==1);
	}
	
	
	private static byte[] hexStringToByteArray(String s) {
	    int len = s.length();
	    byte[] data = new byte[len / 2];
	    for (int i = 0; i < len; i += 2) {
	        data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
	                             + Character.digit(s.charAt(i+1), 16));
	    }
	    return data;
	}

}

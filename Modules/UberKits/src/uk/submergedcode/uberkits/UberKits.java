package uk.submergedcode.uberkits;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import uk.submergedcode.SubmergedCore.SubmergedCore;
import uk.submergedcode.SubmergedCore.module.Module;
import uk.submergedcode.uberkits.commands.KitCommand;
import uk.submergedcode.uberkits.commands.KitSaveCommand;
import uk.thecodingbadgers.bDatabaseManager.Database.BukkitDatabase;

/**
 * The Class UberKits.
 * Main entry point to the module.
 */
public class UberKits extends Module {

	private final TreeMap<String, Kit> m_kits = new TreeMap<String, Kit>();
	
	/**
	 * Gets the database associated with this module.
	 * 
	 * @return the database associated with this module.
	 */
	public static BukkitDatabase getDatabase() {
		return m_database;
	}
	
	@Override
	public void onDisable() {
		log(Level.INFO,  getName() + " disabled.");
	}
	
	@Override
	public void onEnable() {
				
		loadLanguageFile();
		registerCommand(new KitCommand(this));
		registerCommand(new KitSaveCommand(this));
		createDatabase();
		loadKits();
		log(Level.INFO,  getName() + " enabled.");	
		
	}

	/**
	 * 
	 * @return 
	 */
	public Map<String, Kit> getKits() {		
		return Collections.synchronizedSortedMap(m_kits);
	}
	
	/**
	 * 
	 */
	public void reloadKits() {
		m_kits.clear();
		loadKits();
	}
	
	/**
	 * 
	 */
	public void loadKits() {
		
		String configPath = this.getDataFolder() + File.separator + "kits.json";
		
		// Make sure the kit file exists
		File file = new File(configPath);
		if (!file.exists()) {
			return;
		}
		
		try {
			JSONParser parser = new JSONParser();
			JSONObject kitsJson = (JSONObject) parser.parse(new FileReader(configPath));
			
			JSONArray kits = (JSONArray) kitsJson.get("kits");
			for (Object kitRaw : kits) {
				JSONObject jsonKit = (JSONObject) kitRaw;
				Kit newKit = new Kit();
				newKit.loadKit(jsonKit);
				m_kits.put(newKit.getName().toLowerCase(), newKit);
			}
			
		} catch (IOException ex) {
			Bukkit.getLogger().log(Level.WARNING, "Error loading '" + configPath + "'", ex);
		} catch (ParseException ex) {
			Bukkit.getLogger().log(Level.WARNING, "Error parsing '" + configPath + "'", ex);
		}
		
	}
	
	/**
	 * 
	 * @param kits 
	 */
	public void saveKits(JSONObject kits) {
		
		String configPath = this.getDataFolder() + File.separator + "kits.json";
		
		try {			
			
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			JsonParser jp = new JsonParser();
			JsonElement je = jp.parse(kits.toJSONString());
			String prettyJsonString = gson.toJson(je);
			
			FileWriter file = new FileWriter(configPath);
			file.write(prettyJsonString);
			file.flush();
			file.close();
			
		} catch (IOException e) {
			Bukkit.getLogger().log(Level.WARNING, "Failed to save kits to '" + configPath + "'.", e);
		}
		
	}
	
	/**
	 * 
	 * @param kitName
	 * @param newKit 
	 */
	public void addNewKit(String kitName, JSONObject newKit) {
		
		if (m_kits.containsKey(kitName)) {
			m_kits.get(kitName).loadKit(newKit);
		} else {
			Kit kit = new Kit();
			kit.loadKit(newKit);
			m_kits.put(kitName, kit);
		}
		
		JSONObject kitsRoot = new JSONObject();
		JSONArray kitsArray = new JSONArray();		
		
		for (Kit kit : m_kits.values()) {
			kitsArray.add(kit.getJson());
		}
		
		kitsRoot.put("kits", kitsArray);
		
		saveKits(kitsRoot);
	}
	
	/**
	 * Create the kits database
	 */
	private void createDatabase() {
		BukkitDatabase db = SubmergedCore.getBukkitDatabase();
		if (!db.tableExists("perks_kits_claimed")) {			
			String query = "CREATE TABLE perks_kits_claimed (" +
					"uuid VARCHAR(64)," +	
					"playername VARCHAR(64)," +	
					"time BIGINT," +	
					"kitname VARCHAR(64)" +
					");";
			
			db.query(query, true);
		}
	}
	
	/**
	 * 
	 * @param player
	 * @param kit
	 * @return 
	 */
	public KitClaim canPlayerClaimKit(final Player player, final Kit kit) {
		
		KitClaim newClaim = new KitClaim();
		
		BukkitDatabase db = SubmergedCore.getBukkitDatabase();
		
		String query = "SELECT * FROM perks_kits_claimed WHERE `uuid`='" 
						+ player.getUniqueId().toString() + "' AND `kitname`='"
						+ kit.getName() + "'";
		
		boolean foundKit = false;
		
		ResultSet result = db.queryResult(query);
		if (result != null) {
			
			long currentTime = System.currentTimeMillis();
					
			try {
				// while we have another result, read in the data
				while (result.next()) {
					
					long time = result.getLong("time");
					long timeDiff = currentTime - time;
					long timeout = (long)(kit.getTimeout() * 60.0 * 1000.0);
					if (timeDiff > timeout) {
						foundKit = true;
						break;
					}	
					
					long timeLeft = timeout - timeDiff;
					int hours = 0;
					int minutes = 0;
					
					final long oneHour = 1000 * 60 * 60;
					final long oneMinute = 1000 * 60;
					
					while (timeLeft > oneHour) {
						timeLeft -= oneHour;
						hours++;
					}
					
					minutes = (int)(Math.ceil(timeLeft / oneMinute)) + 1;
					
					newClaim.timeLeft = hours + " hours, " + minutes + " minutes until available...";
					db.freeResult(result);
					return newClaim;
		        }
			} catch (SQLException e) {
				e.printStackTrace();
				return newClaim;
			}
			
			db.freeResult(result);
			
		}
		
		if (foundKit) {
			removeEntry(player, kit);
		}
		
		newClaim.canClaim = true;
		return newClaim;
	}

	/**
	 * 
	 * @param player
	 * @param kit 
	 */
	private void removeEntry(Player player, Kit kit) {
		
		BukkitDatabase db = SubmergedCore.getBukkitDatabase();
			
		String query = "DELETE FROM `perks_kits_claimed` WHERE `uuid`='" 
						+ player.getUniqueId().toString() + "' AND `kitname`='"
						+ kit.getName() + "'";
		
		db.query(query, true);
		
	}
	
	/**
	 * 
	 * @param player
	 * @param kitname
	 * @param currentTimeMillis 
	 */
	public void logKitClaim(Player player, String kitname, long currentTimeMillis) {
		
		BukkitDatabase db = SubmergedCore.getBukkitDatabase();
		
		String query = "INSERT INTO `perks_kits_claimed` " +
				"(`uuid`,`playername`,`time`,`kitname`) VALUES (" + 
				"'" + player.getUniqueId().toString() + "'," +
				"'" + player.getName() + "'," +
				"'" + currentTimeMillis + "'," +
				"'" + kitname + 
				"');";
		
		db.query(query, true);
		
	}

	/**
	 * 
	 * @param player
	 * @param kit 
	 */
	public void giveKit(Player player, Kit kit) {
		
		// Check if has enough room
		final PlayerInventory invent = player.getInventory();
		int freeSpace = 0;
		for (ItemStack item : invent.getContents()) {
			if (item == null) {
				freeSpace++;
			}
		}

		Map<Integer, ItemStack> kitItems = kit.getItems();
		if (kitItems.size() > freeSpace) {
			Module.sendMessage("Kits", player, "You do not have enough space in your inventory! " + 
				(kitItems.size() == 1 ? "1 free slot is required" : (kitItems.size() + " free slots are required.")));
			return;
		}

		// Give items
		for (ItemStack item : kitItems.values()) {
			invent.addItem(item.clone());
		}
		player.updateInventory();

		// Add to database
		this.logKitClaim(player, kit.getName(), System.currentTimeMillis());
		
		Module.sendMessage("Kits", player, "You have been given the kit '" + kit.getName() + "'");
		
	}
	
}

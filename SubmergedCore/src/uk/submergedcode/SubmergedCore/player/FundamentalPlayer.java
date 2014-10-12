/**
 * SubmergedCore 1.0
 * Copyright (C) 2014 CodingBadgers <plugins@mcbadgercraft.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.submergedcode.SubmergedCore.player;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.messaging.Messenger;
import uk.submergedcode.SubmergedCore.SubmergedCore;
import uk.submergedcode.SubmergedCore.backup.BackupFactory;
import uk.submergedcode.SubmergedCore.backup.PlayerBackup;
import uk.submergedcode.SubmergedCore.message.Message;

/**
 * The Basic Player class for all modules.
 */
public class FundamentalPlayer {

    protected Player m_player;
    // protected HashMap<Class<? extends PlayerData>, PlayerData> m_playerData = new HashMap<Class<? extends PlayerData>, PlayerData>();

    protected Map<String, Map<String, PlayerData>> m_playerData = new HashMap<String, Map<String, PlayerData>>();
                
    /**
     * Instantiates a new base player.
     *
     * @param player the bukkit player
     */
    public FundamentalPlayer(Player player) {
        m_player = player;
    }

    /**
     * Gets the bukkit player associated to this FundamentalPlayer.
     *
     * @return the bukkit player
     * @see Player
     */
    public Player getPlayer() {
        return m_player;
    }

    /**
     * 
     * @param group
     * @param name
     * @param data 
     */
    public void addPlayerData(String group, String name, PlayerData data) {
        
        Map<String, PlayerData> groups = null;
        if (m_playerData.containsKey(group)) {
            groups = m_playerData.get(group);
        } else {
            groups = new HashMap<String, PlayerData>();
        }
                
        groups.put(name, data);
        m_playerData.put(group, groups);
        
        data.onEnable(this);
    }
    
    /**
     * 
     * @param group
     * @param name
     * @return 
     */
    public PlayerData getPlayerData(String group, String name) {
        
        if (!m_playerData.containsKey(group)) {
            return null;
        }
        
        Map<String, PlayerData> dataGroup = m_playerData.get(group);
        
        if (!dataGroup.containsKey(name)) {
            return null;
        }
        
        return dataGroup.get(name);
    }
    
    /**
     * Gets any player data of the given data ID.
     *
     * @param <T>
     * @param dataID the data type to lookup
     * @return the player data if it exists, else null.
     * @see PlayerData
     */
    public <T extends PlayerData> T getPlayerData(Class<? extends T> dataID) {
        try {        
            T tempInstance = dataID.newInstance();
            return (T) getPlayerData(tempInstance.getGroup(), tempInstance.getName());
        }
        catch (IllegalAccessException ex) {
            return null;
        } catch (InstantiationException ex) {
            return null;
        }
    }
    
    /**
     * Gets any player data of the given data ID.
     *
     * @param <T>
     * @param dataID the data type to lookup
     * @return the player data if it exists, else null.
     * @see PlayerData
     */
    public <T extends PlayerData> List<T> getAllPlayerData(String group) {
        
        List<T> datas = new ArrayList<T>();

        if (m_playerData.containsKey(group)) {
            Map<String, PlayerData> dataGroup = m_playerData.get(group);
            for (PlayerData data : dataGroup.values()) {
                datas.add((T)data);
            }
        }            
        
        return datas;
    }
    
    /**
     * 
     * @param data
     * @return 
     */
    public boolean isDataOwner(PlayerData data) {
        
        for (Map<String, PlayerData> group : m_playerData.values()) {
            for (PlayerData playerData : group.values()) {
                if (playerData.equals(data)) {
                    return true;
                }                
            }            
        }        
        
        return false;
    }
            
    /**
     * Send message, will split at new line characters '\n' and send each part as a seperate message.
     *
     * @param msg the message to send
     */
    public void sendMessage(String msg) {
        String[] messages = msg.split("\n");

        for (String message : messages) {
            m_player.sendMessage(ChatColor.DARK_PURPLE + "[SubmergedCore] " + ChatColor.RESET + message);
        }
    }

    /**
     * Send plugin message.
     *
     * @param channel the plugin channel
     * @param message the message, will be split up into separate messages with
     *                header if the message is longer than 1024 bytes long
     * @return true if successful, false if channel was not registered
     */
    public boolean sendClientMessage(String channel, String message) {
        byte[] bytes = message.getBytes();

        if (!Bukkit.getMessenger().isOutgoingChannelRegistered(SubmergedCore.getInstance(), channel)) {
            return false;
        }

        if (bytes.length > Messenger.MAX_MESSAGE_SIZE) {
            String header = "Client Message; length:" + Math.ceil((double) bytes.length / (double) Messenger.MAX_MESSAGE_SIZE);
            m_player.sendPluginMessage(SubmergedCore.getInstance(), channel, header.getBytes());

            while (true) {
                byte[] toSend = Arrays.copyOf(bytes, Messenger.MAX_MESSAGE_SIZE);
                m_player.sendPluginMessage(SubmergedCore.getInstance(), channel, toSend);

                try {
                    bytes = Arrays.copyOfRange(bytes, Messenger.MAX_MESSAGE_SIZE, bytes.length);
                } catch (Exception ex) {
                    break;
                }
            }
        } else {
            m_player.sendPluginMessage(SubmergedCore.getInstance(), channel, bytes);
        }

        return true;
    }

    /**
     * Gets the location of the player.
     *
     * @return the location
     */
    public Location getLocation() {
        return m_player.getLocation();
    }

    /**
     * Release any stored data about the player, called on leave.
     */
    public void destroy() {
        destroyPlayerData();
        m_player = null;
    }

    /**
     * Clear all a players player data
     */
    public void destroyPlayerData() {
        for (Entry<String, Map<String, PlayerData>> entry : m_playerData.entrySet()) {
            for (PlayerData data : entry.getValue().values()) {
                data.onDisable();
            }            
        }
        m_playerData.clear();
    }
    
    /**
     * Backup a player to a file based on their name in a given folder.
     * Do not clear the players inventory though.
     *
     * @param BackupFolder The folder the backup should be created in
     */
    public PlayerBackup backupInventory(File backupFolder) {
        return backupInventory(backupFolder, false);
    }

    /**
     * Backup a player to a file based on their name in a given folder.
     *
     * @param BackupFolder The folder the backup should be created in
     * @param clearInv     clear the players inventory after backing up
     */
    public PlayerBackup backupInventory(File backupFolder, boolean clearInv) {

        File backupFile = new File(backupFolder + File.separator + m_player.getName() + ".json");
        PlayerBackup backup = BackupFactory.createBackup(backupFile, m_player);

        // clear the inventory only if the backup was successful
        if (clearInv && backup != null) {
            clearInventory(true);
        }

        return backup;
    }

    /**
     * Restore a player to a backup from the given folder
     *
     * @param backupFolder The folder that the players backup resides in
     * @return true if successful and false otherwise
     */
    public boolean restoreInventory(File restoreFolder) throws FileNotFoundException {

        clearInventory(true);

        File backupFile = new File(restoreFolder + File.separator + m_player.getName() + ".json");
        if (!backupFile.exists()) {
            throw (new FileNotFoundException("A backup file could not be found for the player " + m_player.getName() + " in the folder " + restoreFolder.getAbsolutePath()));
        }

        PlayerBackup backup = BackupFactory.readBackup(backupFile);

        if (backup != null) {
            backup.restore(m_player);
            backup.deleteFile();
            return true;
        }

        return false;
    }

    /**
     * Clear all items from a players inventory
     *
     * @param clearArmour Should we clear the players armour as well
     * @return true if successful and false otherwise
     */
    public void clearInventory(boolean clearArmour) {

        PlayerInventory inventory = m_player.getInventory();
        inventory.clear();
        if (clearArmour) {
            inventory.setHelmet(null);
            inventory.setChestplate(null);
            inventory.setLeggings(null);
            inventory.setBoots(null);
        }
    }

    /**
     * Send a custom formated message to a player
     *
     * @param message the messge to send
     */
    public void sendMessage(Message message) {
        String json = SubmergedCore.getGsonInstance().toJson(message);
        if (SubmergedCore.getConfigurationManager().isDebugEnabled()) {
            SubmergedCore.log(Level.INFO, json);
        }

        try {
            PacketContainer packet = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.CHAT);
            packet.getChatComponents().write(0, WrappedChatComponent.fromJson(json));
            ProtocolLibrary.getProtocolManager().sendServerPacket(this.m_player, packet);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}

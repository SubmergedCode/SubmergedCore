package uk.submergedcode.blocksPlus.commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.scheduler.BukkitTask;
import uk.submergedcode.blocksPlus.BlocksPlus;
import org.bukkit.util.Vector;
import uk.submergedcode.SubmergedCore.commands.ModuleCommand;
import uk.submergedcode.SubmergedCore.module.Module;

public class BlockPlusCommand extends ModuleCommand implements Listener {

	private final BlocksPlus m_module;
	private Map<Player, BukkitTask> m_playerFallTimers = new HashMap<Player, BukkitTask>();    
    
    private class BouncyData {
        public int Height = 1;        
        public int RandomOffset = 1;
    }
    
	/**
	 * 
	 * @param module 
	 */
	public BlockPlusCommand(BlocksPlus module) {
		super("blockplus", "/blockplus <type> [params...]");
		m_module = module;
	}

	/**
	 * 
	 * @param sender
	 * @param label
	 * @param args
	 * @return 
	 */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

		if (!(sender instanceof Player)) {
			Module.sendMessage("Blocks+", sender, "You have to be a player.");
			return true;
		}

        if (args.length == 0) {
            Module.sendMessage("Blocks+", sender, "Specify a type... [bouncy, randomid]");
            return true;
        }
        
		Player player = (Player) sender;
        ItemStack currentItems = player.getItemInHand();
        if (currentItems.getType() == Material.AIR) {
            Module.sendMessage("Blocks+", sender, "Have a block in your hand...");
            return true;
        }

        if (args[0].toLowerCase().equals("bouncy")) {
            MakeBlockBouncy(player, args);
        }
		        
		return true;
	}
    
    private void MakeBlockBouncy(Player player, String[] args) {
        
        int height = 1;
        int randomoffset = 0;
        
        if (args.length >= 2) {
            height = Integer.parseInt(args[1]);
        }
        
        if (args.length >= 3) {
            randomoffset = Integer.parseInt(args[2]);
        }
        
        ItemStack currentItems = player.getItemInHand();
        ItemMeta meta = currentItems.getItemMeta();
        meta.setDisplayName(ChatColor.GOLD + "Bouncy " + currentItems.getType().name().toLowerCase());
        List<String> lore = new ArrayList<String>();
        lore.add("Height: " + height);        
        lore.add("Random Offset: " + randomoffset);
        meta.setLore(lore);        
        currentItems.setItemMeta(meta);
        
    }
    
    @EventHandler
    public void OnBlockPlaced(BlockPlaceEvent event) {
        
        ItemStack currentItems = event.getItemInHand();
        ItemMeta meta = currentItems.getItemMeta();
        String blockName = meta.getDisplayName();
        
        if (blockName != null && blockName.equals(ChatColor.GOLD + "Bouncy " + currentItems.getType().name().toLowerCase())) {
            // Bouncy block
            List<String> lore = meta.getLore();
            String heightLore = lore.get(0);             
            String randomoffsetLore = lore.get(1); 

            BouncyData data = new BouncyData();
            data.Height = Integer.parseInt(heightLore.substring(heightLore.indexOf(":") + 1).trim());            
            data.RandomOffset = Integer.parseInt(randomoffsetLore.substring(randomoffsetLore.indexOf(":") + 1).trim());            
            
            final Block block = event.getBlock();
            block.setMetadata("Bouncy", new FixedMetadataValue(m_module.getPlugin(), data));
        }
        
    }
    
    @EventHandler
    public void OnPlayerMove(PlayerMoveEvent event) {
        
        final Player player = event.getPlayer();
        final Location location = player.getLocation();
        final Block block = location.getBlock();
        final Block floorBlock = block.getRelative(BlockFace.DOWN);
        final List<MetadataValue> meta = floorBlock.getMetadata("Bouncy");
        
        if (!meta.isEmpty() && !player.isSneaking()) {
            BouncyData data = (BouncyData)meta.get(0).value();
            
            Random r = new Random();
            float height = data.Height + (float)(r.nextDouble() * data.RandomOffset);
            
            Vector velocity = player.getVelocity();
            velocity.setY(height);
            player.setVelocity(velocity);
            
            player.setMetadata("NoFallDamage", new FixedMetadataValue(m_module.getPlugin(), data));
            
            BukkitTask task = Bukkit.getScheduler().runTaskLater(m_module.getPlugin(),
                new Runnable() {
                    @Override
                    public void run() {
                        player.removeMetadata("NoFallDamage", m_module.getPlugin());
                    }
                }    
            , 20L * 30);
            
            if (m_playerFallTimers.containsKey(player)) {
                BukkitTask oldTask = m_playerFallTimers.get(player);
                oldTask.cancel();
            }
            
            m_playerFallTimers.put(player, task);
        }        
    }
    
    @EventHandler
    public void OnEntityDamage(EntityDamageEvent event) {
        
        if (event.getEntityType() != EntityType.PLAYER) {
            return;
        }
        
        if (event.getCause() != EntityDamageEvent.DamageCause.FALL) {
            return;
        }
        
        final Player player = (Player)event.getEntity();
        final List<MetadataValue> meta = player.getMetadata("NoFallDamage");
        if (!meta.isEmpty()) {
            event.setCancelled(true);
        }        
    }
    
    @EventHandler
    public void OnBlockDestroy(BlockBreakEvent event) {
        
        final Block block = event.getBlock();
        final List<MetadataValue> meta = block.getMetadata("Bouncy");
        
        if (!meta.isEmpty()) {
            block.removeMetadata("Bouncy", m_module.getPlugin());
        }
    }
}

package uk.submergedcode.uberkits.commands;

import java.util.Map;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import uk.submergedcode.SubmergedCore.SubmergedCore;
import uk.submergedcode.SubmergedCore.commands.ModuleCommand;
import uk.submergedcode.SubmergedCore.gui.GuiInventory;
import uk.submergedcode.SubmergedCore.module.Module;
import uk.submergedcode.uberkits.Kit;
import uk.submergedcode.uberkits.KitClaim;
import uk.submergedcode.uberkits.UberKits;
import uk.submergedcode.uberkits.callbacks.KitGuiCallback;

public class KitCommand extends ModuleCommand {
	
	private static final String PERMISSION_KIT = "perks.UberKits";
	private static final String PERMISSION_KIT_GUI = "perks.UberKits.gui";

	private final UberKits m_module;
	
	/**
	 * 
	 * @param module 
	 */
	public KitCommand(UberKits module) {
		super("kit", "/kit <name>");
		m_module = module;
	}

	/**
	 * 
	 * @param sender
	 * @param labe
	 * @param args
	 * @return 
	 */
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

		if (!(sender instanceof Player)) {
			Module.sendMessage("Kits", sender, m_module.getLanguageValue("not-player"));
			return true;
		}

		Player player = (Player) sender;

		if (args.length == 0) {
			handleKitGUI(player);
			return true;
		}

		String kitname = args[0].toLowerCase();
		if (!m_module.getKits().containsKey(kitname)) {
			Module.sendMessage("Kits", player, m_module.getLanguageValue("unknown-kit-name"));
			return true;
		}
		
		Kit kit = m_module.getKits().get(kitname);
		KitClaim claim = m_module.canPlayerClaimKit(player, kit);
		
		if (!claim.canClaim) {
			Module.sendMessage("Kits", player, claim.timeLeft);
			return true;
		}
		
		m_module.giveKit(player, kit);
				
		return true;
	}
	
	/**
	 * 
	 * @param player 
	 */
	private void handleKitGUI(final Player player) {
		
		if (!Module.hasPermission(player, PERMISSION_KIT_GUI)) {
			Module.sendMessage("Kits", player, m_module.getLanguageValue("COMMAND-UberKits-GUI-NO-PERMISSION"));
			return;
		}
		
		Map<String, Kit> kits = m_module.getKits();
		final int noofKits = kits.size();
		final int ROW_COUNT = (int) Math.ceil(noofKits / 9.0f);

        GuiInventory inventory = new GuiInventory(SubmergedCore.getInstance());
        inventory.createInventory("Kit Selection", ROW_COUNT);
		
		for (Kit kit : kits.values()) {
			
			if (!Module.hasPermission(player, PERMISSION_KIT + "." + (kit.getName().toLowerCase()))) {
				continue;
			}
			
			ItemStack item = new ItemStack(kit.getIcon());
			String[] details = new String[3];
			
			KitClaim claim = m_module.canPlayerClaimKit(player, kit);
			
			if (claim.canClaim) {
				details[0] = ChatColor.GREEN + "Available";
				details[1] = ChatColor.GOLD + "Left click to claim kit";
			} else {
				details[0] = ChatColor.RED + claim.timeLeft;
				details[1] = "";
			}
			
			details[2] = ChatColor.GOLD + "Right click to preview kit";
			
			inventory.addMenuItem(kit.getName(), item, details, new KitGuiCallback(m_module, player, kit, claim.canClaim));
		}
		
		inventory.open(player);
		
	}
}

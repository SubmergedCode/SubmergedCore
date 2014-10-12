package uk.submergedcode.blocksPlus;

import uk.submergedcode.SubmergedCore.module.Module;
import uk.submergedcode.blocksPlus.commands.BlockPlusCommand;

/**
 * The Class bKits.
 * Main entry point to the module.
 */
public class BlocksPlus extends Module {

	@Override
	public void onDisable() {

	}
	
	@Override
	public void onEnable() {

        BlockPlusCommand command = new BlockPlusCommand(this);
        registerCommand(command);
        register(command);
        
	}

}

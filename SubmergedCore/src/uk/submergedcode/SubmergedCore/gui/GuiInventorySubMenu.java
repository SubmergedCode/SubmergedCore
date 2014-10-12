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
package uk.submergedcode.SubmergedCore.gui;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import uk.submergedcode.SubmergedCore.gui.callbacks.GuiReturnCallback;

public class GuiInventorySubMenu extends GuiSubInventory {

    public GuiInventorySubMenu(GuiInventory ownerMenu, String title, int rowCount) {
        super(ownerMenu, title, rowCount);
        this.addMenuItem("Back", new ItemStack(Material.NETHER_STAR), new String[]{"Return to", ownerMenu.getTitle()}, (rowCount - 1) * 9, new GuiReturnCallback(ownerMenu));
    }

}

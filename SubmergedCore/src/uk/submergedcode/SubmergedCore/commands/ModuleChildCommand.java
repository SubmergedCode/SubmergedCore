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
package uk.submergedcode.SubmergedCore.commands;

import uk.submergedcode.SubmergedCore.module.Module;

public class ModuleChildCommand extends ModuleCommand {

    protected ModuleCommand m_parent;

    public ModuleChildCommand(ModuleCommand parent, String label) {
        super(label, parent.getUsage());
        m_parent = parent;
    }

    public ModuleCommand getParent() {
        return m_parent;
    }

    @Override
    public Module getModule() {
        return m_parent.getModule();
    }
}

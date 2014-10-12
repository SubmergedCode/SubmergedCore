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
package uk.submergedcode.SubmergedCore.module;

import com.google.common.base.Preconditions;
import uk.submergedcode.SubmergedCore.module.loader.LoadableDescriptionFile;

import java.io.File;
import java.util.jar.JarFile;

/**
 * Stub module class, used to place the module in the correct place in the load
 * queue so dependencies can be handled nicely at load time.
 */
public class ModuleInfo implements Comparable<ModuleInfo> {

    private final LoadableDescriptionFile description;
    private final JarFile jar;
    private final File file;

    public ModuleInfo(File file, JarFile jar, LoadableDescriptionFile ldf) {
        this.file = file;
        this.jar = jar;
        this.description = ldf;
    }

    public String getName() {
        return description.getName();
    }

    public JarFile getJar() {
        return jar;
    }

    public File getFile() {
        return file;
    }

    public LoadableDescriptionFile getDescription() {
        return description;
    }

    public boolean hasDependency(ModuleInfo other) {
        return this.getDescription().getDependencies().contains(other.getName());
    }

    @Override
    public int compareTo(ModuleInfo o) {
        Preconditions.checkNotNull(o);

        if (this.hasDependency(o)) {
            return 1;
        } else if(o.hasDependency(this)) {
            return -1;
        }

        return (int) Math.signum(this.getDescription().getLoadPriority().ordinal() - o.getDescription().getLoadPriority().ordinal());
    }
}

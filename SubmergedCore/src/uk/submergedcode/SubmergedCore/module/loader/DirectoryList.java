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
package uk.submergedcode.SubmergedCore.module.loader;

import com.google.common.base.Preconditions;

import java.io.File;
import java.util.ArrayList;

/**
 * A special list for holding the directories that modules are loaded from by
 * the module loaded.
 * <p />
 * If this list is set to read only ({@link #isReadonly()} == {@code true})
 * then new entries cannot be added to the list until it is set back to read
 * write mode by the loader.
 * This is so directories cannot be added to the loader after the loader has
 * computed the list of modules to be loaded.
 * <p />
 * As well as this only directories can be added to the list, if you try and
 * add any other type of file a {@link IllegalArgumentException} will be thrown
 * by the add method.
 */
public class DirectoryList extends ArrayList<File> {

    private boolean readonly = true;

    @Override
    public boolean add(File element) {
        Preconditions.checkState(readonly, "Directory list currently read only.");
        Preconditions.checkArgument(element.isDirectory(), "The file '" + element.getAbsolutePath() + "' is not a directory.");

        return super.add(element);
    }

    @Override
    public void add(int index, File element) {
        Preconditions.checkState(readonly, "Directory list currently read only.");
        Preconditions.checkArgument(element.isDirectory(), "The file '" + element.getAbsolutePath() + "' is not a directory.");

        super.add(index, element);
    }

    @Override
    public File set(int index, File element) {
        Preconditions.checkState(readonly, "Directory list currently read only.");
        Preconditions.checkArgument(element.isDirectory(), "The file '" + element.getAbsolutePath() + "' is not a directory.");

        return super.set(index, element);
    }

    void setReadonly(boolean readonly) {
        this.readonly = readonly;
    }

    public boolean isReadonly() {
        return this.readonly;
    }
}

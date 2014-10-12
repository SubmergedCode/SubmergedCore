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
package uk.submergedcode.SubmergedCore.message;

import org.bukkit.Achievement;
import org.bukkit.inventory.ItemStack;

public class HoverEvent implements Cloneable {

    private final HoverEventType action;
    private final Object value;

    public HoverEvent(HoverEventType type, Object value) {
        if (type == HoverEventType.SHOW_TOOLTIP && !(value instanceof String)) {
            throw new IllegalArgumentException("Value for a tooltip must be a string");
        } else if (type == HoverEventType.SHOW_ITEM && !(value instanceof ItemStack)) {
            throw new IllegalArgumentException("Value for a item tooltip must be a item");
        } else if (type == HoverEventType.SHOW_ACHIEVEMENT && !(value instanceof Achievement)) {
            throw new IllegalArgumentException("Value for a achievement tooltip must be a achievement");
        }

        this.action = type;
        this.value = value;
    }

    public HoverEventType getAction() {
        return action;
    }

    public Object getValue() {
        return value;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((action == null) ? 0 : action.hashCode());
        result = prime * result + ((value == null) ? 0 : value.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ClickEvent && hashCode() == obj.hashCode();
    }

    @Override
    public String toString() {
        return "ClickEvent [action=" + action + ", value=" + value + "]";
    }

    @Override
    public HoverEvent clone() {
        return new HoverEvent(this.action, this.value);
    }

}

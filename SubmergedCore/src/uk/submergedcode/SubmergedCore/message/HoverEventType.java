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

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public enum HoverEventType {
    SHOW_TOOLTIP("show_text"),
    SHOW_ITEM("show_item"),
    SHOW_ACHIEVEMENT("show_achievement"),;

    private static final Map<String, HoverEventType> BY_ID = new HashMap<String, HoverEventType>();

    static {
        for (HoverEventType type : values()) {
            BY_ID.put(type.id, type);
        }
    }

    private String id;

    private HoverEventType(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public static class HoverEventSerializer implements JsonSerializer<HoverEventType>, JsonDeserializer<HoverEventType> {

        @Override
        public HoverEventType deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            if (!json.isJsonPrimitive()) {
                return null;
            }

            return BY_ID.get(json.getAsString());
        }

        @Override
        public JsonElement serialize(HoverEventType src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.id.toLowerCase());
        }

    }
}

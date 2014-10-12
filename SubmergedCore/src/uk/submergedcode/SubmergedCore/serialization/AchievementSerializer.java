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
package uk.submergedcode.SubmergedCore.serialization;

import java.lang.reflect.Type;

import org.bukkit.Achievement;

import com.google.common.base.CaseFormat;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class AchievementSerializer implements JsonSerializer<Achievement>, JsonDeserializer<Achievement> { // Copied from org.bukkit.craftbukkit.CraftAchievement
    private final BiMap<String, Achievement> achievements;

    public AchievementSerializer() {
        ImmutableMap<String, Achievement> specialCases = ImmutableMap.<String, Achievement>builder()
                .put("achievement.buildWorkBench", Achievement.BUILD_WORKBENCH)
                .put("achievement.diamonds", Achievement.GET_DIAMONDS)
                .put("achievement.portal", Achievement.NETHER_PORTAL)
                .put("achievement.ghast", Achievement.GHAST_RETURN)
                .put("achievement.theEnd", Achievement.END_PORTAL)
                .put("achievement.theEnd2", Achievement.THE_END)
                .put("achievement.blazeRod", Achievement.GET_BLAZE_ROD)
                .put("achievement.potion", Achievement.BREW_POTION)
                .build();

        ImmutableBiMap.Builder<String, Achievement> builder = ImmutableBiMap.<String, Achievement>builder();
        for (Achievement achievement : Achievement.values()) {
            if (specialCases.values().contains(achievement)) {
                continue;
            }
            builder.put("achievement." + CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, achievement.name()), achievement);
        }

        builder.putAll(specialCases);

        achievements = builder.build();
    }

    public String getAchievementName(Achievement material) {
        return achievements.inverse().get(material);
    }

    public Achievement getAchievement(String name) {
        return achievements.get(name);
    }

    @Override
    public Achievement deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return json.isJsonPrimitive() ? getAchievement(json.getAsString()) : null;
    }

    @Override
    public JsonElement serialize(Achievement src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(getAchievementName(src));
    }

}
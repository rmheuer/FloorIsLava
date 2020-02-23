/*
 * This file is part of FloorIsLava.
 *
 * FloorIsLava is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FloorIsLava is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with FloorIsLava.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.gmail.tracebachi.floorislava.utils;

import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Trace Bachi (tracebachi@gmail.com, BigBossZee) on 8/26/16.
 */
public class ItemUseDelay {
    private Map<String, Long> delays;

    public ItemUseDelay(FileConfiguration config) {
        this.delays = new HashMap<>();
        this.set("tnt", config.getInt("ItemUseDelays.ThrowingTNT"));
        this.set("hook", config.getInt("ItemUseDelays.PlayerLauncher"));
        this.set("web", config.getInt("ItemUseDelays.Webber"));
        this.set("invis", config.getInt("ItemUseDelays.RodOfInvisibility"));
        this.set("boost", config.getInt("ItemUseDelays.Boost"));
        this.set("chikun", config.getInt("ItemUseDelays.Chikun"));
        this.set("steal", config.getInt("ItemUseDelays.Steal"));
    }

    public long valueOf(String name) {
        return delays.get(name);
    }

    private void set(String name, long delay) {
        this.delays.put(name, delay);
    }
}

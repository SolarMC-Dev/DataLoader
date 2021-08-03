/*
 *
 *  * dataloader
 *  * Copyright © 2021 SolarMC Developers
 *  *
 *  * dataloader is free software: you can redistribute it and/or modify
 *  * it under the terms of the GNU Affero General Public License as
 *  * published by the Free Software Foundation, either version 3 of the
 *  * License, or (at your option) any later version.
 *  *
 *  * dataloader is distributed in the hope that it will be useful,
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  * GNU Affero General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU Affero General Public License
 *  * along with dataloader. If not, see <https://www.gnu.org/licenses/>
 *  * and navigate to version 3 of the GNU Affero General Public License.
 *
 */

package gg.solarmc.loader.kitpvp;

import java.time.Duration;
import java.util.Set;

/**
 * Represents a kit and its contents
 *
 */
public class Kit {

    private final int id;
    private final String name;
    private final Set<ItemInSlot> contents;
    private final Duration cooldown;

    Kit(int id, String name, Set<ItemInSlot> contents, Duration cooldown) {
        this.id = id;
        this.name = name;
        this.contents = contents;
        this.cooldown = cooldown;
    }

    public int getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public Set<ItemInSlot> getContents() {
        return this.contents;
    }

    public Duration getCooldown() {
        return cooldown;
    }

    /**
     * A kit is equal to another according to its ID only
     *
     * @param o the other object
     * @return true if equal, false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Kit kit = (Kit) o;
        return id == kit.id;
    }

    @Override
    public int hashCode() {
        return id;
    }
}

/*
 *
 *  * dataloader
 *  * Copyright © $DateInfo.year SolarMC Developers
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

package gg.solarmc.loader.kitpvp.kit;

import java.util.Set;

/**
 * Represents a kit, please fill out later
 */
public class Kit {

    private final int id;
    private final String name;
    private final Set<KitItemPair> contents;

    public Kit(int id, String name, Set<KitItemPair> contents) {
        this.id = id;
        this.name = name;
        this.contents = contents;
    }

    public int getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public Set<KitItemPair> getContents() {
        return this.contents;
    }

}

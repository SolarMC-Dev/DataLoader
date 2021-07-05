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

public record ItemInSlot(int slot, KitItem item) {

    /**
     * Leftover from before this class was a record
     *
     * @return the item
     * @deprecated use the record component
     */
    @Deprecated
    public KitItem getItem() {
        return item;
    }

    /**
     * Leftover from before this class was a record
     *
     * @return the slot
     * @deprecated use the record component
     */
    @Deprecated
    public int getSlot() {
        return slot;
    }
}

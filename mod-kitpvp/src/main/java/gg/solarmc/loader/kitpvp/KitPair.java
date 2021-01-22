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

package gg.solarmc.loader.kitpvp;

public class KitPair {

    public KitItem<?> getItem() {
        return item;
    }

    public Byte getSlot() {
        return slot;
    }

    private final Byte slot;
    private final KitItem<?> item;

    KitPair(Byte slot, KitItem<?> item) {
        this.item = item;
        this.slot = slot;
    }
}

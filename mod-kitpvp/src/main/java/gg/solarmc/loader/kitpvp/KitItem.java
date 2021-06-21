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

/**
 * Interface representing something that can be stored in a kit
 */
public interface KitItem {

    /**
     * Informative string for the item material
     *
     * @return the material
     */
    String getMaterial();

    /**
     * Informative string for the display name
     *
     * @return the display name
     */
    String getDisplayName();

    /**
     * Informative number for the amount of the item
     *
     * @return the amount
     */
    int getAmount();

    /**
     * Gets the backing item object
     *
     * @return the item object itself
     */
    Object getItem();

    /**
     * Gets the backing item object, assuming it is of a given type
     *
     * @param <T> the expected type of the item object
     * @param type the expected type class of the item object
     * @return the item object itself
     */
    <T> T getItem(Class<T> type);

}

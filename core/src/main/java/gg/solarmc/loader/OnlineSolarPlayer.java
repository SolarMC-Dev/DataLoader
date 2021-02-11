/*
 * DataLoader
 * Copyright © 2021 SolarMC Developers
 *
 * DataLoader is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * DataLoader is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with DataLoader. If not, see <https://www.gnu.org/licenses/>
 * and navigate to version 3 of the GNU Affero General Public License.
 */

package gg.solarmc.loader;

import gg.solarmc.loader.data.DataKey;
import gg.solarmc.loader.data.DataObject;

import java.util.Optional;

/**
 * A solar player who is assuredly online. {@link #isLive()} will always be {@code true}
 * and {@link #toLivePlayer()} returns this. <br>
 * <br>
 * Testing that a {@code SolarPlayer} is an instance of this object is <b>NOT</b> an acceptable
 * way to determine whether the player is online. A {@code SolarPlayer} may either directly
 * implement this interface, or it may delegate to an instance of this interface.
 *
 */
public interface OnlineSolarPlayer extends SolarPlayer {

	/**
	 * Whether this player is live (always true)
	 *
	 * @return true
	 */
	@Override
	default boolean isLive() {
		return true;
	}

	/**
	 * Returns this player
	 *
	 * @return an optional of this player
	 */
	@Override
	default Optional<OnlineSolarPlayer> toLivePlayer() {
		return Optional.of(this);
	}

	/**
	 * Gets a container of online data attached to this player.
	 *
	 * @param key the data key
	 * @param <D> the data object type
	 * @param <O> the offline data object type
	 * @return the online data object
	 */
	@Override
	<D extends O, O extends DataObject> D getData(DataKey<D, O, ?> key);

}

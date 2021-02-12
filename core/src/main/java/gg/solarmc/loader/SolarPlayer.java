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
import java.util.UUID;

/**
 * A player whose data may be manipulated. <br>
 * <br>
 * Instances of {@code SolarPlayer}, whether online or offline, should not be retained
 * in memory by API users. Only framework implementors should do so.
 *
 */
public interface SolarPlayer {

	/**
	 * Gets the user ID for this player. Database schema is typically keyed by this ID.
	 *
	 * @return the user ID
	 */
	int getUserId();

	/**
	 * Gets the minecraft UUID of this player. <br>
	 * <br>
	 * Note: It is more effective to utilize the ID of the player returned from {@link #getUserId()}
	 * than to use the UUID
	 *
	 * @return the player's minecraft UUID
	 */
	UUID getMinecraftUUID();

	/**
	 * Determines whether the underlying player is online in the running server instance. <br>
	 * <br>
	 * It is possible for this method to return {@code false} if the user is online
	 * on another server or proxy.
	 *
	 * @return true if online, false otherwise
	 */
	boolean isLive();

	/**
	 * If this solar player is online, converts to a {@code OnlineSolarPlayer}. Otherwise
	 * returns an empty optional.
	 *
	 * @return the online solar player if possible
	 */
	Optional<OnlineSolarPlayer> toLivePlayer();

	/**
	 * Gets a container of data attached to this player.
	 *
	 * @param key the data key
	 * @param <D> the data object type
	 * @param <O> the offline data object type
	 * @return the offline data object, may be the online object if this player is live
	 */
	<D extends O, O extends DataObject> O getData(DataKey<D, O, ?> key);

}

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

package gg.solarmc.loader.impl.player;

import gg.solarmc.loader.OnlineSolarPlayer;
import gg.solarmc.loader.SolarPlayer;
import gg.solarmc.loader.data.DataKey;
import gg.solarmc.loader.data.DataObject;
import gg.solarmc.loader.impl.PlayerTracker;

import java.util.Optional;
import java.util.UUID;

public final class DelegatingSolarPlayer implements SolarPlayer {

	private final SolarPlayerId id;
	private final SolarPlayerData offlineData;
	private final PlayerTracker playerTracker;

	public DelegatingSolarPlayer(SolarPlayerId id, SolarPlayerData offlineData, PlayerTracker playerTracker) {
		this.id = id;
		this.offlineData = offlineData;
		this.playerTracker = playerTracker;
	}

	@Override
	public int getUserId() {
		return id.userId();
	}

	@Override
	public UUID getMcUuid() {
		return id.mcUuid();
	}

	@Override
	public String getMcUsername() {
		return id.mcUsername();
	}

	@Override
	public boolean isLive() {
		return toLivePlayer().isPresent();
	}

	@Override
	public Optional<OnlineSolarPlayer> toLivePlayer() {
		return playerTracker.getOnlinePlayerForUuid(getMcUuid());
	}

	@Override
	public <D extends O, O extends DataObject> O getData(DataKey<D, O, ?> key) {
		Optional<OnlineSolarPlayer> onlineDelegate = playerTracker.getOnlinePlayerForUuid(getMcUuid());
		if (onlineDelegate.isPresent()) {
			return onlineDelegate.get().getData(key);
		}
		return offlineData.getDataOffline(key);
	}

}

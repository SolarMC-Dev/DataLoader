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
import gg.solarmc.loader.data.DataKey;
import gg.solarmc.loader.data.DataObject;

import java.util.UUID;

public final class OnlineSolarPlayerImpl implements OnlineSolarPlayer {

	private final SolarPlayerId id;
	private final SolarPlayerData onlineData;

	public OnlineSolarPlayerImpl(SolarPlayerId id, SolarPlayerData onlineData) {
		this.id = id;
		this.onlineData = onlineData;
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
	public <D extends O, O extends DataObject> D getData(DataKey<D, O, ?> key) {
		return onlineData.getDataOnline(key);
	}
}

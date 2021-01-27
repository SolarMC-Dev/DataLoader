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

package gg.solarmc.loader.authentication;

import space.arim.omnibus.util.UUIDUtil;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

final class UUIDOperations {

	private UUIDOperations() {}

	static boolean isPremium(UUID uuid) {
		// Premium UUIDs are v4. See OpenJDK implementation of UUID.randomUUID()
		byte[] uuidBytes = UUIDUtil.toByteArray(uuid);
		return (uuidBytes[6] & 0x40) != 0;
	}

	static UUID computeOfflineUuid(String name) {
		return UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(StandardCharsets.UTF_8));
	}
}

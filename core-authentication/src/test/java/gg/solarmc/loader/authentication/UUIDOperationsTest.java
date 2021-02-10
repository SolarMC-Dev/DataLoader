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

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UUIDOperationsTest {

	@Test
	public void isPremium() {
		assertTrue(UUIDOperations.isPremium(UUID.fromString(
				"ed5f12cd-6007-45d9-a4b9-940524ddaecf")), "Premium");
		assertFalse(UUIDOperations.isPremium(UUID.fromString(
				"0aef255c-11e0-3879-9dba-1c530ab70323")), "Cracked");

		assertFalse(UUIDOperations.isPremium(UUIDOperations.computeOfflineUuid("A248")));
	}

	@Test
	public void computeOfflineUuid() {
		assertEquals(
				UUID.fromString("0b58c22d-56f5-3296-87b8-c0155a071d4d"),
				UUIDOperations.computeOfflineUuid("McStorm_MlyK11qF"));
	}

}

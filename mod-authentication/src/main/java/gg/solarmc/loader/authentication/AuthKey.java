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

import de.mkammerer.argon2.Argon2Factory;
import gg.solarmc.loader.Transaction;
import gg.solarmc.loader.data.DataKey;
import gg.solarmc.loader.data.DataKeyInitializationContext;
import gg.solarmc.loader.data.DataLoader;

import java.security.SecureRandom;

public final class AuthKey implements DataKey<Auth, AuthManager> {

	public static final AuthKey INSTANCE = new AuthKey();

	private AuthKey() {}

	@Override
	public DataLoader<Auth> createLoader(AuthManager dataManager, DataKeyInitializationContext context) {
		return new DataLoader<>() {

			@Override
			public Auth createDefaultData(Transaction transaction, int userId) {
				return new Auth();
			}

			@Override
			public Auth loadData(Transaction transaction, int userId) {
				return new Auth();
			}
		};
	}

	@Override
	public AuthManager createDataManager(DataKeyInitializationContext context) {
		var argon = Argon2Factory.createAdvanced(
				Argon2Factory.Argon2Types.ARGON2i,
				SaltGenerator.SALT_LENGTH,
				PasswordHasher.HASH_LENGTH);
		return new AuthManager(
				new PasswordHasher(argon),
				new SaltGenerator(new SecureRandom()));
	}
}

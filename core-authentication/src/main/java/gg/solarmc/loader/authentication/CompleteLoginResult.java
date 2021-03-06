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

public enum CompleteLoginResult {

	/**
	 * The user logged in as cracked
	 */
	NORMAL,
	/**
	 * The user's cracked data was migrated to the premium account they are now logging in with
	 */
	MIGRATED_TO_PREMIUM,
	/**
	 * User ID is missing. This can only happen if someone else migrated this user's account.
	 * Is that considered a forced migration?
	 *
	 */
	USER_ID_MISSING;

}

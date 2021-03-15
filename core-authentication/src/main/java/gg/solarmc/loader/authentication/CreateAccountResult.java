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

/**
 * Possible outcomes of attempted account creation
 *
 */
public enum CreateAccountResult {

	/**
	 * Successfully created account. The user can be considered logged in
	 */
	CREATED,
	/**
	 * The account for the username already exists. This is a likely race condition,
	 * assuming the caller has implemented everything correctly. The implementor
	 * should be sure to implement rate limiting so a user cannot spam account creation.
	 */
	CONFLICT

}

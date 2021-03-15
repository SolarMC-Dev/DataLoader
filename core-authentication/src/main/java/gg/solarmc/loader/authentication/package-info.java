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

/**
 * Everything related to authenticating users, premium and cracked, through
 * their accounts on SolarMC. <br>
 * <br>
 * For the sake of consistency, "authenticating" refers to the process of logging in
 * to a SolarMC account. The "login protocol" refers to the protocol of encryption
 * request and response employed by premium clients. An "autologin" refers to an
 * attempt to detect a premium user and authenticate them automatically without
 * any effort by the user to enter a password. Documentation often refers to "joining
 * <b>a</b> proxy" because it is entirely possible for multiple proxies to be in
 * concurrent operation and have users joining them simultaneously. <br>
 * <br>
 * While players are not permitted to play, they are placed in a limbo server in
 * which they have nothing to do but log in when prompted for a password. <br>
 * <br>
 * <b>Login Process</b> <br>
 * First, it must be determined whether the user is premium or cracked. This is done by
 * searching for an existing user with the same name. <br>
 * <br>
 * If an existing user is found, the joining user is assumed to be that user. If the existing user
 * is premium, the joining user is verified via the login protocol as premium. If in this case
 * the joining user is actually cracked, they are unfortunately kicked with "Invald session".
 * If the joining user is indeed premium, the user is permitted to play. <br>
 * <br>
 * If the existing user is cracked, the joining user must enter the password of the existing user.
 * If the joining user is unsuccessful, they are kicked. If they are successful, and their account
 * is cracked, they are permitted to play normally. If the joining user is actually premium,
 * and has thus just logged in to a formerly cracked account, their user data is migrated to
 * their new UUID. This migration occurs only for our premier data stored with dataloader; it
 * cannot be helped to migrate third party plugin data. <br>
 * <br>
 * If there is no existing user, then a lookup to the Mojang API will be performed to best judge
 * whether the user is premium or cracked. If there is a premium user according to the Mojang API
 * with the username, then the joining user is assumed to be that premium user, and they must
 * complete the login protocol. If the user is actually cracked but they were detected as premium,
 * they are kicked with "invalid session." If the user is indeed premium, there are two possible
 * outcomes. Either a cracked account has been created in the intervening time in which the login
 * protocol was completed (a very small window of time), or not. If that happens, the premium user
 * must enter the account password. Otherwise, the user data is migrated to the new premium UUID,
 * following the same migration process as if a premium user logged in to the account of an existing
 * cracked user, and then the user is permitted to play. <br>
 * <br>
 * The "Invalid Session" kick message is not changeable. This is the only user-facing weakness
 * of the system, which occurs when a player is best thought to be premium, but in actuality
 * is a cracked user. <br>
 * <br>
 * <b>Case Sensitivity of Names</b> <br>
 * It cannot be permitted to have multiple cracked users with the same name ignoring case.
 * Cracked users who have the same name ignoring case but a different name including case
 * will have different UUIDs.
 *
 */
package gg.solarmc.loader.authentication;
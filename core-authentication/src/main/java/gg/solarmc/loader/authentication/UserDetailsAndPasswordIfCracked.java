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

import java.util.Objects;
import java.util.Optional;

/**
 * Holds user details, and if the player is cracked, the user's verifiable password
 *
 */
public final class UserDetailsAndPasswordIfCracked {

    private final UserDetails userDetails;
    private final VerifiablePassword verifiablePassword;

    private UserDetailsAndPasswordIfCracked(UserDetails userDetails, VerifiablePassword verifiablePassword) {
        this.userDetails = Objects.requireNonNull(userDetails, "userDetails");
        this.verifiablePassword = verifiablePassword;
    }

    static UserDetailsAndPasswordIfCracked forPremiumUser(UserDetails userDetails) {
        assert userDetails.isPremium();
        return new UserDetailsAndPasswordIfCracked(userDetails, null);
    }

    static UserDetailsAndPasswordIfCracked forCrackedUser(UserDetails userDetails,
                                                          VerifiablePassword verifiablePassword) {
        assert !userDetails.isPremium();
        return new UserDetailsAndPasswordIfCracked(userDetails,
                                                   Objects.requireNonNull(verifiablePassword, "verifiablePassword"));
    }

    public UserDetails userDetails() {
        return userDetails;
    }

    /**
     * The user's password or none if they are premium
     *
     * @return the user's password or none for premium users
     */
    public Optional<VerifiablePassword> verifiablePassword() {
        return Optional.ofNullable(verifiablePassword);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UserDetailsAndPasswordIfCracked that = (UserDetailsAndPasswordIfCracked) o;
        return userDetails.equals(that.userDetails) &&
               Objects.equals(verifiablePassword, that.verifiablePassword);
    }

    @Override
    public int hashCode() {
        int result = userDetails.hashCode();
        result = 31 * result + (verifiablePassword != null? verifiablePassword.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "UserDetailsAndPasswordIfCracked{" +
               "userDetails=" + userDetails +
               ", verifiablePassword=" + verifiablePassword +
               '}';
    }
}

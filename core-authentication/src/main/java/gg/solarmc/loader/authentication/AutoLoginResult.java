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

import gg.solarmc.loader.Transaction;

import java.util.Objects;

/**
 * An initial indicator of how to proceed
 *
 */
public final class AutoLoginResult {

    private final User user;
    private final VerifiablePassword verifiablePassword;
    private final ResultType resultType;

    private AutoLoginResult(User user,
                            VerifiablePassword verifiablePassword,
                            ResultType resultType) {
        this.user = user;
        this.verifiablePassword = verifiablePassword;
        this.resultType = Objects.requireNonNull(resultType, "resultType");
    }

    static AutoLoginResult forExistingPremiumUser(User user) {
        assert user.isPremium();
        return new AutoLoginResult(Objects.requireNonNull(user, "user"),
                                        null, ResultType.PREMIUM);
    }

    static AutoLoginResult forExistingCrackedUser(User user,
                                                  VerifiablePassword verifiablePassword,
                                                  boolean wantsMigration) {
        assert !user.isPremium();
        return new AutoLoginResult(
                Objects.requireNonNull(user, "user"),
                Objects.requireNonNull(verifiablePassword, "verifiablePassword"),
                (wantsMigration) ? ResultType.CRACKED_BUT_DESIRES_MIGRATION : ResultType.CRACKED);
    }

    static AutoLoginResult noneFound() {
        return new AutoLoginResult(null, null, ResultType.NONE_FOUND);
    }

    static AutoLoginResult deniedCaseSensitivityOfName() {
        return new AutoLoginResult(null, null, ResultType.DENIED_CASE_SENSITIVITY_OF_NAME);
    }

    /**
     * Gets the user details of the found user
     *
     * @return the user details of the found user
     * @throws IllegalStateException if the result type does not indicate a user was found
     */
    public User userDetails() {
        if (user == null) {
            throw new IllegalStateException("No user details present");
        }
        return user;
    }

    /**
     * Gets the user details of the found cracked user
     *
     * @return the user details of the found cracked user
     * @throws IllegalStateException if the result type does not indicate a cracked user
     */
    public VerifiablePassword verifiablePassword() {
        if (verifiablePassword == null) {
            throw new IllegalStateException("No verifiable password present");
        }
        return verifiablePassword;
    }

    public ResultType resultType() {
        return resultType;
    }

    /**
     * The possible result states. The "caller" in the documentation of each option
     * refers to the caller of {@link AuthenticationCenter#findExistingUserWithName(Transaction, String)}
     *
     */
    public enum ResultType {

        /**
         * An existing user was found and the existing user is premium. <br>
         * <br>
         * The caller should verify the user via the premium login protocol,
         * and once that is successful, confirm the user's authentication via
         * {@link AuthenticationCenter#attemptLoginOfIdentifiedUser(Transaction, UserWithDataNotYetLoaded)}
         *
         */
        PREMIUM,
        /**
         * An existing user was found and the existing user is cracked. <br>
         * <br>
         * The caller should proceed to authenticate the user against their password and
         * then call
         * {@link AuthenticationCenter#completeLoginAndPossiblyMigrate(Transaction, UserWithDataNotYetLoaded)}
         *
         */
        CRACKED,
        /**
         * An existing user was found and the existing user is cracked. However, the
         * user has specified that they are migrating to a premium account. <br>
         * <br>
         * The caller should verify the user via the premium login protocol, and once
         * that is successful, have the user authenticate against the existing account,
         * finally calling
         * {@link AuthenticationCenter#completeLoginAndPossiblyMigrate(Transaction, UserWithDataNotYetLoaded)}
         *
         */
        CRACKED_BUT_DESIRES_MIGRATION,
        /**
         * No existing user was found. <br>
         * <br>
         * The caller should proceed to lookup whether the username belongs to a premium user
         * according to the Mojang API. If that is the case, the user should be presumed
         * premium and verified via the premium login protocol. Otherwise the user is cracked.
         *
         */
        NONE_FOUND,
        /**
         * An existing user was found, but the name of the joining user is the same as
         * the existing name ignoring case but different than the existing name including case. <br>
         * <br>
         * The caller should deny the user.
         *
         */
        DENIED_CASE_SENSITIVITY_OF_NAME

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AutoLoginResult that = (AutoLoginResult) o;
        return Objects.equals(user, that.user) && Objects.equals(verifiablePassword, that.verifiablePassword)
                && resultType == that.resultType;
    }

    @Override
    public int hashCode() {
        int result = user != null ? user.hashCode() : 0;
        result = 31 * result + (verifiablePassword != null ? verifiablePassword.hashCode() : 0);
        result = 31 * result + resultType.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "AutoLoginResult{" +
                "user=" + user +
                ", verifiablePassword=" + verifiablePassword +
                ", resultType=" + resultType +
                '}';
    }
}

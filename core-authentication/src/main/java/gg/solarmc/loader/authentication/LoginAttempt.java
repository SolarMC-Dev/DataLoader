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

public final class LoginAttempt {

	private final ResultType resultType;
	private final VerifiablePassword verifiablePassword;

	private LoginAttempt(ResultType resultType, VerifiablePassword verifiablePassword) {
		this.resultType = Objects.requireNonNull(resultType);
		this.verifiablePassword = verifiablePassword;
	}

	static LoginAttempt premiumPermitted() {
		return new LoginAttempt(ResultType.PREMIUM_PERMITTED, null);
	}

	static LoginAttempt needsPassword(VerifiablePassword verifiablePassword) {
		return new LoginAttempt(ResultType.NEEDS_PASSWORD, Objects.requireNonNull(verifiablePassword));
	}

	static LoginAttempt needsAccount() {
		return new LoginAttempt(ResultType.NEEDS_ACCOUNT, null);
	}

	static LoginAttempt deniedPremiumTookName() {
		return new LoginAttempt(ResultType.DENIED_PREMIUM_TOOK_NAME, null);
	}

	static LoginAttempt deniedCaseSensitivityOfName() {
		return new LoginAttempt(ResultType.DENIED_CASE_SENSITIVITY_OF_NAME, null);
	}

	/**
	 * Gets the result type
	 *
	 * @return the result type
	 */
	public ResultType resultType() {
		return resultType;
	}

	/**
	 * Gets the hashed password which must be verified against
	 *
	 * @return the hashed password with instructions, or an empty optional if this is not {@code NEEDS_PASSWORD}
	 */
	public Optional<VerifiablePassword> verifiablePassword() {
		return Optional.ofNullable(verifiablePassword);
	}

	public enum ResultType {

		/**
		 * The user is permitted. Only possible for premium users
		 */
		PREMIUM_PERMITTED,
		/**
		 * The user needs to enter a password to login. The details
		 * relating to the password are given by {@link #verifiablePassword()}
		 */
		NEEDS_PASSWORD,
		/**
		 * The cracked user needs to create an account and then login
		 */
		NEEDS_ACCOUNT,
		/**
		 * The user is cracked, but a premium user already holds their username. <br>
		 * <br>
		 * This can only be encountered via a race condition. Usually the state
		 * {@link AutoLoginPreparation.ResultType#PREMIUM} is returned, and so
		 * the joining user is assumed to be premium, and is therefore unfortunately
		 */
		DENIED_PREMIUM_TOOK_NAME,
		/**
		 * This user is cracked, and has joined with a name that is the same as an existing name
		 * ignoring case but different than the existing name including case. <br>
		 * <br>
		 * As with {@code DENIED_PREMIUM_TOOK_NAME}, this can only happen via a race condition.
		 * This state should usually be prevented by {@link AutoLoginPreparation.ResultType#DENIED_CASE_SENSITIVITY_OF_NAME}
		 */
		DENIED_CASE_SENSITIVITY_OF_NAME

	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		LoginAttempt that = (LoginAttempt) o;
		return resultType == that.resultType && Objects.equals(verifiablePassword, that.verifiablePassword);
	}

	@Override
	public int hashCode() {
		int result = resultType.hashCode();
		result = 31 * result + (verifiablePassword != null ? verifiablePassword.hashCode() : 0);
		return result;
	}

	@Override
	public String toString() {
		return "LoginAttempt{" +
			   "resultType=" + resultType +
			   ", verifiablePassword=" + verifiablePassword +
			   '}';
	}
}

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

package gg.solarmc.loader.impl;

import space.arim.omnibus.util.UUIDUtil;

import java.net.InetAddress;
import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

public final class UserDetails {

    private final UUID mcUuid;
    private final String username;
    private final byte[] address;

    private UserDetails(UUID mcUuid, String username, byte[] address, Void signature) {
        this.mcUuid = Objects.requireNonNull(mcUuid);
        this.username = Objects.requireNonNull(username);
        this.address = address;
    }

    public UserDetails(UUID mcUuid, String username, byte[] address) {
        this(mcUuid, username, address.clone(), null);
        if (address.length != 4 && address.length != 16) {
            throw new IllegalArgumentException("Address is of illegal length");
        }
    }

    public UserDetails(UUID mcUuid, String username, InetAddress address) {
        this(mcUuid, username, address.getAddress(), null);
    }

    public UUID mcUuid() {
        return mcUuid;
    }

    public byte[] mcUuidAsBytes() {
        return UUIDUtil.toByteArray(mcUuid);
    }

    public String username() {
        return username;
    }

    public byte[] address() {
        return address.clone();
    }

    /**
     * Gets the uncloned address. The caller should be careful not to mutate it
     *
     * @return the uncloned address
     */
    public byte[] addressUncloned() {
        return address;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserDetails that = (UserDetails) o;
        return mcUuid.equals(that.mcUuid) && username.equals(that.username) && Arrays.equals(address, that.address);
    }

    @Override
    public int hashCode() {
        int result = mcUuid.hashCode();
        result = 31 * result + username.hashCode();
        result = 31 * result + Arrays.hashCode(address);
        return result;
    }

    @Override
    public String toString() {
        return "UserDetails{" +
                "mcUuid=" + mcUuid +
                ", username='" + username + '\'' +
                ", address=" + Arrays.toString(address) +
                '}';
    }
}

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

package gg.solarmc.loader.impl.login;

import gg.solarmc.loader.Transaction;
import gg.solarmc.loader.impl.UserDetails;
import org.jooq.DSLContext;

import static gg.solarmc.loader.schema.tables.LibertybansAddresses.LIBERTYBANS_ADDRESSES;
import static gg.solarmc.loader.schema.tables.LibertybansNames.LIBERTYBANS_NAMES;

interface NameAddressHistoryUpdate {

    void update(Transaction transaction, UserDetails userDetails);

    class NoOpImpl implements NameAddressHistoryUpdate {

        @Override
        public void update(Transaction transaction, UserDetails userDetails) {

        }
    }

    class FunctioningImpl implements NameAddressHistoryUpdate {

        @Override
        public void update(Transaction transaction, UserDetails userDetails) {
            DSLContext context = transaction.getProperty(DSLContext.class);
            long currentTime = System.currentTimeMillis() / 1_000L;
            byte[] uuid = userDetails.mcUuidAsBytes();
            context.insertInto(LIBERTYBANS_NAMES)
                    .columns(LIBERTYBANS_NAMES.UUID, LIBERTYBANS_NAMES.NAME, LIBERTYBANS_NAMES.UPDATED)
                    .values(uuid, userDetails.username(), currentTime)
                    .onDuplicateKeyUpdate()
                    .set(LIBERTYBANS_NAMES.UPDATED, currentTime)
                    .execute();
            context.insertInto(LIBERTYBANS_ADDRESSES)
                    .columns(LIBERTYBANS_ADDRESSES.UUID, LIBERTYBANS_ADDRESSES.ADDRESS, LIBERTYBANS_ADDRESSES.UPDATED)
                    .values(uuid, userDetails.addressUncloned(), currentTime)
                    .onDuplicateKeyUpdate()
                    .set(LIBERTYBANS_ADDRESSES.UPDATED, currentTime)
                    .execute();
        }
    }

}

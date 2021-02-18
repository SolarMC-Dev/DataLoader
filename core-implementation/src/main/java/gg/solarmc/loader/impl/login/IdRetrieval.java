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
import org.jooq.Record1;

import static gg.solarmc.loader.schema.Routines.insertOrGetUserId;
import static gg.solarmc.loader.schema.tables.UserIds.USER_IDS;

interface IdRetrieval {

    int retrieveUserId(Transaction transaction, UserDetails userDetails);

    class AssumeUserExists implements IdRetrieval {

        @Override
        public int retrieveUserId(Transaction transaction, UserDetails userDetails) {
            DSLContext context = transaction.getProperty(DSLContext.class);
            Integer userId = context
                    .select(USER_IDS.ID)
                    .from(USER_IDS)
                    .where(USER_IDS.UUID.eq(userDetails.mcUuidAsBytes()))
                    .fetchOne(USER_IDS.ID);
            if (userId == null) {
                throw new IllegalStateException("Unable to find user ID for " + userDetails);
            }
            return userId;
        }
    }

    class CreateUserIfNotExists implements IdRetrieval {

        @Override
        public int retrieveUserId(Transaction transaction, UserDetails userDetails) {
            DSLContext context = transaction.getProperty(DSLContext.class);
            Record1<Integer> idRecord = context
                    .select(insertOrGetUserId(userDetails.mcUuidAsBytes()))
                    .fetchOne();
            if (idRecord == null) {
                throw new IllegalStateException("Failed to generate user ID for " + userDetails);
            }
            return idRecord.value1();
        }
    }
}

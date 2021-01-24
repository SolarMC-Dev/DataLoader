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

package gg.solarmc.loader.kitpvp;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import gg.solarmc.loader.Transaction;
import gg.solarmc.loader.data.DataManager;
import gg.solarmc.loader.impl.SQLTransaction;
import gg.solarmc.loader.schema.tables.records.KitpvpKitsContentsRecord;
import gg.solarmc.loader.schema.tables.records.KitpvpKitsNamesRecord;
import org.jooq.DSLContext;
import org.jooq.Result;

import java.time.Duration;
import java.util.*;

import static gg.solarmc.loader.schema.tables.KitpvpKitsNames.KITPVP_KITS_NAMES;
import static gg.solarmc.loader.schema.tables.KitpvpKitsContents.KITPVP_KITS_CONTENTS;

public class KitPvpManager implements DataManager {

    private final Cache<Integer,Kit> existingKits = Caffeine.newBuilder().expireAfterAccess(Duration.ofMinutes(15)).build();
    private final ItemSerializer serializer;

    public KitPvpManager(ItemSerializer serializer) {
        this.serializer = serializer;
    }

    /**
     * Gets a kit based on it's kit ID
     * @param transaction how many times to i have to write this...?
     * @param id represents the kit ID
     * @return the kit from cache or table
     */
    public Kit getKit(Transaction transaction, Integer id) {
        return existingKits.get(id,num -> {
            var jooq = transaction.getProperty(DSLContext.class);

            KitpvpKitsNamesRecord result = jooq.fetchOne(KITPVP_KITS_NAMES,KITPVP_KITS_NAMES.KIT_ID.eq(num));

            assert result != null : "Invalid kit selected!";

            Result<KitpvpKitsContentsRecord> result1 = jooq.fetch(KITPVP_KITS_CONTENTS,KITPVP_KITS_CONTENTS.KIT_ID.eq(num));

            Set<KitPair> set = new HashSet<>(); //this does not have to be concurrent because it will just be reloaded

            for (KitpvpKitsContentsRecord record : result1) {
                set.add(new KitPair(record.getSlot(),this.serializer.deserialize(record.getItem())));
            }

            return new Kit(
                    num,
                    result.getKitName(),
                    Collections.unmodifiableSet(set)
                    );
        });
    }

    /**
     * Creates a kit with given arguments
     * @param transaction is the transaction
     * @param name name of the kit, please make it unique
     * @param contents of the kit
     */
    public void createKit(Transaction transaction, String name, Set<KitPair> contents) {
        var jooq = transaction.getProperty(DSLContext.class);

        KitpvpKitsNamesRecord result = jooq.insertInto(KITPVP_KITS_NAMES,KITPVP_KITS_NAMES.KIT_NAME)
                .values(name)
                .returning()
                .fetchOne();

        for (KitPair pair : contents) {
            jooq.insertInto(KITPVP_KITS_CONTENTS,KITPVP_KITS_CONTENTS.SLOT,KITPVP_KITS_CONTENTS.ITEM,KITPVP_KITS_CONTENTS.KIT_ID)
                    .values(pair.getSlot(), serializer.serialize(pair.getItem()),result.getKitId());
        }

        existingKits.put(result.getKitId(),new Kit(result.getKitId(),name,contents));
    }

    /**
     * Deletes a kit based on id.
     * @param transaction represents the transaction (the more times i type this the more sanity i lose)
     * @param id represents the id of the kit
     */
    public void deleteKit(Transaction transaction, Integer id) {
        KitpvpKitsNamesRecord result = transaction.getProperty(DSLContext.class)
                .fetchOne(KITPVP_KITS_NAMES,KITPVP_KITS_NAMES.KIT_ID.eq(id));

        assert result != null : "ID provided does not exist as a kit!";

        this.existingKits.invalidate(id);
        result.delete();
    }

}

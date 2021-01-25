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
import gg.solarmc.loader.schema.tables.records.KitpvpKitsIdsRecord;

import org.jooq.DSLContext;
import org.jooq.Query;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static gg.solarmc.loader.schema.Tables.KITPVP_KITS_IDS;
import static gg.solarmc.loader.schema.tables.KitpvpKitsContents.KITPVP_KITS_CONTENTS;

public class KitPvpManager implements DataManager {

	private final Cache<Integer,Kit> existingKits = Caffeine.newBuilder().expireAfterAccess(Duration.ofMinutes(15)).build();
	private final ItemSerializer serializer;

	KitPvpManager(ItemSerializer serializer) {
		this.serializer = serializer;
	}

	/**
	 * Gets a kit based on it's kit ID
	 *
	 * @param transaction the transaction
	 * @param id represents the kit ID
	 * @return the kit from cache or table
	 */
	Kit getKit(Transaction transaction, int id) {
		return existingKits.get(id, num -> {
			var jooq = transaction.getProperty(DSLContext.class);

			String kitName = jooq
					.select(KITPVP_KITS_IDS.KIT_NAME).from(KITPVP_KITS_IDS)
					.where(KITPVP_KITS_IDS.KIT_ID.eq(id)).fetchOne(KITPVP_KITS_IDS.KIT_NAME);
			if (kitName == null) {
				throw new IllegalStateException("Kit by id " + id + " does not exist");
			}

			Set<ItemInSlot> contents = jooq
                    .select(KITPVP_KITS_CONTENTS.ITEM, KITPVP_KITS_CONTENTS.SLOT).from(KITPVP_KITS_CONTENTS)
                    .where(KITPVP_KITS_CONTENTS.KIT_ID.eq(id)).fetchSet((itemInSlotRecord) -> {
                        return new ItemInSlot(
                        		itemInSlotRecord.get(KITPVP_KITS_CONTENTS.SLOT),
								serializer.deserialize(itemInSlotRecord.get(KITPVP_KITS_CONTENTS.ITEM)));
                    });
			return new Kit(id, kitName, Set.copyOf(contents));
		});
	}

	/**
	 * Creates a kit with given arguments
	 * @param transaction is the transaction
	 * @param name name of the kit, must be unique
	 * @param contents of the kit
	 * @return the kit which was created
	 */
	public Kit createKit(Transaction transaction, String name, Set<ItemInSlot> contents) {
		var jooq = transaction.getProperty(DSLContext.class);

		KitpvpKitsIdsRecord result = jooq.insertInto(KITPVP_KITS_IDS)
				.columns(KITPVP_KITS_IDS.KIT_NAME).values(name)
				.returning().fetchOne();
		if (result == null) {
			throw new IllegalStateException("Failed to insert kit by name " + name);
		}
		int kitId = result.getKitId();

		List<Query> queries = new ArrayList<>(contents.size());
		for (ItemInSlot itemInSlot : contents) {
			Query query = jooq
					.insertInto(KITPVP_KITS_CONTENTS)
					.columns(KITPVP_KITS_CONTENTS.KIT_ID, KITPVP_KITS_CONTENTS.SLOT, KITPVP_KITS_CONTENTS.ITEM)
					.values(kitId, (byte) itemInSlot.getSlot(), serializer.serialize(itemInSlot.getItem()));
			queries.add(query);
		}
		jooq.batch(queries).execute();

		Kit kit = new Kit(kitId, name, contents);
		existingKits.put(kitId, kit);
		return kit;
	}

	/**
	 * Deletes a kit based on id, if it exists
	 * @param transaction represents the transaction
	 * @param kit the kit
	 */
	public void deleteKit(Transaction transaction, Kit kit) {
		int kitId = kit.getId();
		transaction.getProperty(DSLContext.class)
				.deleteFrom(KITPVP_KITS_IDS).where(KITPVP_KITS_IDS.KIT_ID.eq(kitId))
				.execute();
		existingKits.invalidate(kitId);
	}

}

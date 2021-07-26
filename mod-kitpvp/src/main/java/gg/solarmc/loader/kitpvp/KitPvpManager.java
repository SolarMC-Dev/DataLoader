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
import org.jooq.BatchBindStep;
import org.jooq.DSLContext;
import org.jooq.DataType;
import org.jooq.Field;
import org.jooq.impl.DSL;

import java.time.Duration;
import java.util.Set;

import static gg.solarmc.loader.schema.Tables.KITPVP_KITS_IDS;
import static gg.solarmc.loader.schema.tables.KitpvpKitsContents.KITPVP_KITS_CONTENTS;

public class KitPvpManager implements DataManager {

	private final Cache<KitCacheKey,Kit> existingKits = Caffeine.newBuilder().expireAfterAccess(Duration.ofMinutes(15)).build();
	private final ItemSerializer serializer;

	KitPvpManager(ItemSerializer serializer) {
		this.serializer = serializer;
	}

	private DataType<KitItem> kitItemDataType() {
		return KITPVP_KITS_CONTENTS.ITEM.getDataType().asConvertedDataType(new ItemSerializerBinding(serializer));
	}

	/**
	 * Gets a kit based on it's kit ID
	 *
	 * @param transaction the transaction
	 * @param id represents the kit ID
	 * @return the kit from cache or table
	 */
	Kit getKitById(Transaction transaction, int id) {
		return existingKits.get(new KitKeyId(id), num -> {
			var jooq = transaction.getProperty(DSLContext.class);

			String kitName = jooq
					.select(KITPVP_KITS_IDS.KIT_NAME).from(KITPVP_KITS_IDS)
					.where(KITPVP_KITS_IDS.KIT_ID.eq(id)).fetchOne(KITPVP_KITS_IDS.KIT_NAME);

			if (kitName == null) {
				throw new IllegalStateException("Kit by id " + id + " does not exist");
			}

			return getKit(transaction, id, kitName);
		});
	}

	/**
	 * Gets a kit based on it's kit ID
	 *
	 * @param transaction the transaction
	 * @param name represents the kit ID
	 * @return the kit from cache or table
	 */
	Kit getKitByName(Transaction transaction, String name) {
		return existingKits.get(new KitKeyName(name), num -> {
			var jooq = transaction.getProperty(DSLContext.class);

			Integer kitId = jooq
					.select(KITPVP_KITS_IDS.KIT_ID).from(KITPVP_KITS_IDS)
					.where(KITPVP_KITS_IDS.KIT_NAME.eq(name)).fetchOne(KITPVP_KITS_IDS.KIT_ID);

			if (kitId == null) {
				throw new IllegalStateException("Kit by name " + name + " does not exist");
			}

			return getKit(transaction, kitId, name);
		});
	}

	Kit getKit(Transaction transaction, int id, String name){
		var jooq = transaction.getProperty(DSLContext.class);

		DataType<KitItem> itemType = kitItemDataType();
		Field<KitItem> itemColumn = DSL.field(KITPVP_KITS_CONTENTS.ITEM.getName(), itemType);
		Set<ItemInSlot> contents = jooq
				.select(KITPVP_KITS_CONTENTS.SLOT, itemColumn)
				.from(KITPVP_KITS_CONTENTS)
				.where(KITPVP_KITS_CONTENTS.KIT_ID.eq(id))
				.fetchSet((itemInSlotRecord) -> {
					return new ItemInSlot(
						itemInSlotRecord.value1(), itemInSlotRecord.value2());
				});
		return new Kit(id, name, Set.copyOf(contents));
	}

	/**
	 * Creates a kit with given arguments
	 * @param transaction is the transaction
	 * @param name name of the kit, must be unique
	 * @param contents of the kit
	 * @return the kit which was created
	 */
	public Kit createKit(Transaction transaction, String name, Set<ItemInSlot> contents) {
		DSLContext context = transaction.getProperty(DSLContext.class);

		KitpvpKitsIdsRecord result = context.insertInto(KITPVP_KITS_IDS)
				.columns(KITPVP_KITS_IDS.KIT_NAME).values(name)
				.returning().fetchOne();
		if (result == null) {
			throw new IllegalStateException("Failed to insert kit by name " + name);
		}
		int kitId = result.getKitId();

		if (!contents.isEmpty()) {
			DataType<KitItem> itemType = kitItemDataType();
			Field<KitItem> itemColumn = DSL.field(KITPVP_KITS_CONTENTS.ITEM.getQualifiedName(), itemType);
			BatchBindStep batch = context.batch(context
					.insertInto(KITPVP_KITS_CONTENTS)
					.columns(KITPVP_KITS_CONTENTS.KIT_ID, KITPVP_KITS_CONTENTS.SLOT, itemColumn)
					.values((Integer) null, null, null));
			for (ItemInSlot itemInSlot : contents) {
				batch.bind(kitId, itemInSlot.slot(), itemInSlot.item());
			}
			batch.execute();
		}

		Kit kit = new Kit(kitId, name, contents);
		existingKits.put(new KitKeyName(name), kit);
		return kit;
	}

	/**
	 * Deletes a kit based on id, if it exists
	 * @param transaction represents the transaction
	 * @param kitId the kit's id
	 */
	public void deleteKitById(Transaction transaction, int kitId) {
		transaction.getProperty(DSLContext.class)
				.deleteFrom(KITPVP_KITS_IDS).where(KITPVP_KITS_IDS.KIT_ID.eq(kitId))
				.execute();
		existingKits.invalidate(new KitKeyId(kitId));
	}

	/**
	 * Deletes a kit based on name, if it exists
	 * @param transaction represents the transaction
	 * @param kitName the kit's name
	 */
	public void deleteKitByName(Transaction transaction, String kitName) {
		transaction.getProperty(DSLContext.class)
				.deleteFrom(KITPVP_KITS_IDS).where(KITPVP_KITS_IDS.KIT_NAME.eq(kitName))
				.execute();
		existingKits.invalidate(new KitKeyName(kitName));
	}

}

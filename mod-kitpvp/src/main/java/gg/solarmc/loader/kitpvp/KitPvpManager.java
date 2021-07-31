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
import gg.solarmc.loader.SolarPlayer;
import gg.solarmc.loader.Transaction;
import gg.solarmc.loader.data.DataManager;
import org.jooq.BatchBindStep;
import org.jooq.DSLContext;
import org.jooq.DataType;
import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.impl.DSL;

import java.time.Duration;
import java.util.Optional;
import java.util.Set;

import static gg.solarmc.loader.schema.Routines.kitpvpCreateKit;
import static gg.solarmc.loader.schema.Tables.KITPVP_BOUNTY_LOGS;
import static gg.solarmc.loader.schema.Tables.KITPVP_KITS_IDS;
import static gg.solarmc.loader.schema.tables.KitpvpKitsContents.KITPVP_KITS_CONTENTS;

public class KitPvpManager implements DataManager {

	private final Cache<KitCacheKey, Kit> existingKits = Caffeine.newBuilder().expireAfterAccess(Duration.ofMinutes(15)).build();
	private final ItemSerializer serializer;

	KitPvpManager(ItemSerializer serializer) {
		this.serializer = serializer;
	}

	private DataType<KitItem> kitItemDataType() {
		return KITPVP_KITS_CONTENTS.ITEM.getDataType().asConvertedDataType(new ItemSerializerBinding(serializer));
	}

	/**
	 * Gets a kit based on its ID
	 *
	 * @param transaction the transaction
	 * @param id represents the kit ID
	 * @return the kit if found, an empty optional otherwise
	 */
	public Optional<Kit> getKitById(Transaction transaction, int id) {
		// IntelliJ needs to fix their broken inspection on this
		// Cache provides the desired behavior of not caching nulls but returning them from get()
		//noinspection ConstantConditions
		return Optional.ofNullable(existingKits.get(new KitKeyId(id), num -> {
			var jooq = transaction.getProperty(DSLContext.class);

			String kitName = jooq
					.select(KITPVP_KITS_IDS.KIT_NAME).from(KITPVP_KITS_IDS)
					.where(KITPVP_KITS_IDS.KIT_ID.eq(id)).fetchOne(KITPVP_KITS_IDS.KIT_NAME);

			if (kitName == null) {
				return null;
			}

			return getKit(transaction, id, kitName);
		}));
	}

	/**
	 * Gets a kit based on its name
	 *
	 * @param transaction the transaction
	 * @param name the name of the kit to find
	 * @return the kit if found, an empty optional otherwise
	 * @throws IllegalStateException if the kit does not exist
	 */
	public Optional<Kit> getKitByName(Transaction transaction, String name) {
		//noinspection ConstantConditions
		return Optional.ofNullable(existingKits.get(new KitKeyName(name), num -> {
			var jooq = transaction.getProperty(DSLContext.class);

			Integer kitId = jooq
					.select(KITPVP_KITS_IDS.KIT_ID).from(KITPVP_KITS_IDS)
					.where(KITPVP_KITS_IDS.KIT_NAME.eq(name)).fetchOne(KITPVP_KITS_IDS.KIT_ID);

			if (kitId == null) {
				return null;
			}

			return getKit(transaction, kitId, name);
		}));
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
	 * @param name name of the kit, should be unique in order for this method to succeed
	 * @param contents of the kit
	 * @return the kit if it was created, an empty optional if a kit by such name already exists
	 */
	public Optional<Kit> createKit(Transaction transaction, String name, Set<ItemInSlot> contents) {
		DSLContext context = transaction.getProperty(DSLContext.class);

		int kitId = context.select(kitpvpCreateKit(name))
				.fetchSingle().value1();
		if (kitId == -1) { // Special return value
			return Optional.empty();
		}

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
		existingKits.put(new KitKeyId(kitId), kit);
		return Optional.of(kit);
	}

	private void invalidateKit(int id, String name) {
		existingKits.invalidate(new KitKeyId(id));
		existingKits.invalidate(new KitKeyName(name));
	}

	/**
	 * Deletes a kit if it exists
	 *
	 * @param transaction represents the transaction
	 * @param kit the kit to delete
	 * @return true if the kit was deleted, false if the kit did not exist
	 */
	public boolean deleteKit(Transaction transaction, Kit kit) {
		int kitId = kit.getId();
		int updateCount = transaction.getProperty(DSLContext.class)
				.deleteFrom(KITPVP_KITS_IDS)
				.where(KITPVP_KITS_IDS.KIT_ID.eq(kitId))
				.execute();
		if (updateCount == 0) {
			return false;
		}
		invalidateKit(kitId, kit.getName());
		return true;
	}

	/**
	 * Deletes a kit based on id, if it exists
	 * @param transaction represents the transaction
	 * @param kitId the kit's id
	 * @return true if the kit was deleted, false if the kit did not exist
	 */
	public boolean deleteKitById(Transaction transaction, int kitId) {
		Record1<String> kitNameRecord = transaction.getProperty(DSLContext.class)
				.deleteFrom(KITPVP_KITS_IDS)
				.where(KITPVP_KITS_IDS.KIT_ID.eq(kitId))
				.returningResult(KITPVP_KITS_IDS.KIT_NAME)
				.fetchOne();
		if (kitNameRecord == null) {
			return false;
		}
		invalidateKit(kitId, kitNameRecord.value1());
		return true;
	}

	/**
	 * Deletes a kit based on name, if it exists
	 * @param transaction represents the transaction
	 * @param kitName the kit's name
	 * @return true if the kit was deleted, false if the kit did not exist
	 */
	public boolean deleteKitByName(Transaction transaction, String kitName) {
		Record1<Integer> kitIdRecord = transaction.getProperty(DSLContext.class)
				.deleteFrom(KITPVP_KITS_IDS)
				.where(KITPVP_KITS_IDS.KIT_NAME.eq(kitName))
				.returningResult(KITPVP_KITS_IDS.KIT_ID)
				.fetchOne();
		if (kitIdRecord == null) {
			return false;
		}
		invalidateKit(kitIdRecord.value1(), kitName);
		return true;
	}

	/**
	 * Logs the bounty to ~~ariel's private reserve~~ for tracking
	 * @param transaction transaction
	 * @param killer the killer
	 * @param killed the killed
	 */
	public void logBounty(Transaction transaction, SolarPlayer killer, SolarPlayer killed) {
		transaction.getProperty(DSLContext.class)
				.insertInto(KITPVP_BOUNTY_LOGS, KITPVP_BOUNTY_LOGS.KILLER_ID, KITPVP_BOUNTY_LOGS.KILLED_ID)
				.values(killer.getUserId(), killed.getUserId())
				.execute();
	}

	/**
	 * Clears all caches which may be in use. Primarily intended for testing purposes
	 *
	 */
	public void clearCaches() {
		existingKits.invalidateAll();
	}

}

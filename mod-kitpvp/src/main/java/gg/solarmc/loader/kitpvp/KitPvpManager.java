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
import gg.solarmc.loader.schema.tables.KitpvpBounties;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jooq.BatchBindStep;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.DataType;
import org.jooq.Field;
import org.jooq.OrderField;
import org.jooq.Record;
import org.jooq.Record1;
import org.jooq.Record2;
import org.jooq.Record3;
import org.jooq.ResultQuery;
import org.jooq.SelectConditionStep;
import org.jooq.SelectFieldOrAsterisk;
import org.jooq.SelectJoinStep;
import org.jooq.impl.DSL;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static gg.solarmc.loader.schema.Routines.kitpvpCreateKit;
import static gg.solarmc.loader.schema.tables.KitpvpBounties.KITPVP_BOUNTIES;
import static gg.solarmc.loader.schema.tables.KitpvpBountyLogs.KITPVP_BOUNTY_LOGS;
import static gg.solarmc.loader.schema.tables.KitpvpKitsContents.KITPVP_KITS_CONTENTS;
import static gg.solarmc.loader.schema.tables.KitpvpKitsIds.KITPVP_KITS_IDS;
import static gg.solarmc.loader.schema.tables.LatestNames.LATEST_NAMES;

public class KitPvpManager implements DataManager {

	private final Cache<KitCacheKey, Kit> existingKits = Caffeine.newBuilder().expireAfterAccess(Duration.ofMinutes(15)).build();
	private final ItemSerializer serializer;
	private final Clock clock;

	KitPvpManager(ItemSerializer serializer, Clock clock) {
		this.serializer = serializer;
		this.clock = clock;
	}

	Clock clock() {
		return clock;
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

			Record2<String, Integer> kitRecord = jooq
					.select(KITPVP_KITS_IDS.KIT_NAME, KITPVP_KITS_IDS.KIT_COOLDOWN)
					.from(KITPVP_KITS_IDS)
					.where(KITPVP_KITS_IDS.KIT_ID.eq(id))
					.fetchOne();

			if (kitRecord == null) {
				return null;
			}
			return getKit(transaction, id, kitRecord.value1(), kitRecord.value2());
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

			// Fetch name using correct case
			Record3<Integer, String, Integer> kitRecord = jooq
					.select(KITPVP_KITS_IDS.KIT_ID, KITPVP_KITS_IDS.KIT_NAME, KITPVP_KITS_IDS.KIT_COOLDOWN)
					.from(KITPVP_KITS_IDS)
					.where(KITPVP_KITS_IDS.KIT_NAME.eq(name))
					.fetchOne();

			if (kitRecord == null) {
				return null;
			}
			return getKit(transaction, kitRecord.value1(), kitRecord.value2(), kitRecord.value3());
		}));
	}

	Kit getKit(Transaction transaction, int id, String name, int cooldown) {
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
		return new Kit(id, name, Set.copyOf(contents), Duration.ofSeconds(cooldown));
	}

	/**
	 * Creates a kit with given arguments
	 * @param transaction is the transaction
	 * @param name name of the kit, should be unique in order for this method to succeed
	 * @param contents of the kit
	 * @return the kit if it was created, an empty optional if a kit by such name already exists
	 * @deprecated Use {@link #createKit(Transaction, KitBuilder.Built)} which allows for setting
	 * additional options such as the kit cooldown
	 */
	@Deprecated
	public Optional<Kit> createKit(Transaction transaction, String name, Set<ItemInSlot> contents) {
		return createKit(transaction, new KitBuilder().name(name).contents(contents).build());
	}

	/**
	 * Creates a kit from the given builder. Use {@link KitBuilder#KitBuilder()}
	 * to obtain a kit builder
	 *
	 * @param transaction the transaction
	 * @param kitBuilder the details of the kit, contained in the builder
	 * @return the kit if it was created, an empty optional if a kit by the desired name already exists
	 */
	public Optional<Kit> createKit(Transaction transaction, KitBuilder.Built kitBuilder) {
		DSLContext context = transaction.getProperty(DSLContext.class);

		String name = kitBuilder.name();
		int kitId = context.select(kitpvpCreateKit(name, (int) kitBuilder.cooldown().toSeconds()))
				.fetchSingle().value1();
		if (kitId == -1) { // Special return value
			return Optional.empty();
		}

		Set<ItemInSlot> contents = kitBuilder.contents();
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

		Kit kit = new Kit(kitId, name, contents, kitBuilder.cooldown());
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
	 * Begins listing bounties according to the given order. Gives the first page,
	 * from which it is possible to navigate to further pages
	 *
	 * @param tx the transaction
	 * @param listOrder the bounty list order
	 * @return the first page of bounties, or an empty optional if there are no pages
	 */
	public Optional<BountyPage> listBounties(Transaction tx, BountyListOrder.Built listOrder) {
		Objects.requireNonNull(listOrder, "listOrder");
		return listBounties(tx, listOrder, null);
	}

	private KitpvpBounties aliasedBounties(BountyCurrency currency) {
		return KITPVP_BOUNTIES.as("bounties_" + currency.name().toLowerCase(Locale.ROOT));
	}

	private Optional<BountyPage> listBounties(Transaction tx,
											  BountyListOrder.Built listOrder,
											  @Nullable List<Object> seekAfter) {
		DSLContext context = tx.getProperty(DSLContext.class);
		SelectJoinStep<org.jooq.Record> step1;
		{
			// Build SELECT columns
			Set<SelectFieldOrAsterisk> selections = new HashSet<>(1 + listOrder.includeCurrencies().size());
			selections.add(LATEST_NAMES.USERNAME);
			for (BountyCurrency currency : listOrder.includeCurrencies()) {
				selections.add(aliasedBounties(currency).BOUNTY_AMOUNT);
			}
			step1 = context.select(selections).from(LATEST_NAMES);
		}
		// LEFT JOIN bounties table for each currency
		for (BountyCurrency currency : listOrder.includeCurrencies()) {
			KitpvpBounties bounties = aliasedBounties(currency);
			step1 = step1.leftJoin(bounties)
					.on(bounties.USER_ID.eq(LATEST_NAMES.USER_ID))
					.and(bounties.BOUNTY_CURRENCY.eq(currency.serialize()));
		}
		SelectConditionStep<? extends Record> step2;
		{
			// WHERE at least one currency is non-null
			Condition anyCurrencyNotNull = null;
			for (BountyCurrency currency : listOrder.includeCurrencies()) {
				Condition thisCurrencyNotNull = aliasedBounties(currency).BOUNTY_AMOUNT.isNotNull();
				if (anyCurrencyNotNull == null) {
					anyCurrencyNotNull = thisCurrencyNotNull;
				} else {
					anyCurrencyNotNull = anyCurrencyNotNull.or(thisCurrencyNotNull);
				}
			}
			step2 = step1.where(anyCurrencyNotNull);
		}
		ResultQuery<? extends Record> step3;
		{
			// ORDER BY each currency and use SEEK AFTER for pagination
			List<OrderField<?>> orderBy = new ArrayList<>(listOrder.includeCurrencies().size());
			for (BountyCurrency currency : listOrder.includeCurrencies()) {
				orderBy.add(aliasedBounties(currency).BOUNTY_AMOUNT.desc());
			}
			//orderBy.add(LATEST_NAMES.USERNAME);
			if (seekAfter == null) {
				step3 = step2.orderBy(orderBy)
						.limit(listOrder.countPerPage());
			} else {
				if (orderBy.size() != seekAfter.size()) {
					throw new IllegalArgumentException("Invalid seekAfter values; internal error");
				}
				step3 = step2.orderBy(orderBy).seekAfter(seekAfter.toArray(Object[]::new))
						.limit(listOrder.countPerPage());
			}
		}
		List<Bounty> bounties = step3.fetch((record) -> {
			String target = record.get(LATEST_NAMES.USERNAME);
			List<BountyCurrency> includedCurrencies = listOrder.includeCurrencies();
			return switch (includedCurrencies.size()) {
				case 1 -> {
					BountyCurrency currency = listOrder.includeCurrencies().get(0);
					BigDecimal bountyValue = record.get(aliasedBounties(currency).BOUNTY_AMOUNT);
					yield new BountySingleCurrency(target, currency.createAmount(bountyValue));
				}
				case 2 -> BountyDoubleCurrency.from(target, record, (rec, currency) -> {
					BigDecimal bountyAmount = rec.get(aliasedBounties(currency).BOUNTY_AMOUNT);
					return Objects.requireNonNullElse(bountyAmount, BigDecimal.ZERO);
				});
				default -> throw new UnsupportedOperationException("Not implemented for more than 2 currencies");
			};
		});
		if (bounties.isEmpty()) {
			return Optional.empty();
		}
		record BountyPageImpl(KitPvpManager manager,
							  List<? extends Bounty> itemsOnPage, BountyListOrder.Built listOrder)
				implements BountyPage {

			@Override
			public Optional<BountyPage> nextPage(Transaction tx) {
				Bounty lastBounty = itemsOnPage.get(itemsOnPage.size() - 1);
				List</*String & BigDecimal*/ Object> seekAfter = new ArrayList<>(1 + listOrder.includeCurrencies().size());
				for (BountyCurrency currency : listOrder.includeCurrencies()) {
					BigDecimal afterBountyValue = lastBounty.amount(currency).value();
					seekAfter.add(afterBountyValue);
				}
				String target = lastBounty.target();
				//seekAfter.add(target);
				System.out.println("Proceeding to next page with seekAfter values: " + seekAfter);
				return manager.listBounties(tx, listOrder, seekAfter);
			}
		}
		return Optional.of(new BountyPageImpl(this, bounties, listOrder));
	}

	/**
	 * Logs the bounty to ~~ariel's private reserve~~ for tracking
	 * @param transaction transaction
	 * @param killer the killer
	 * @param victim the victim
	 * @param amount the bounty amount
	 * @throws IllegalArgumentException if the bounty amount is not positive
	 */
	public void logBounty(Transaction transaction, SolarPlayer killer, SolarPlayer victim, BountyAmount amount) {
		BigDecimal rawValue = amount.value();
		if (rawValue.compareTo(BigDecimal.ZERO) <= 0) {
			throw new IllegalArgumentException("Amount value must be positive");
		}
		transaction.getProperty(DSLContext.class)
				.insertInto(KITPVP_BOUNTY_LOGS)
				.columns(KITPVP_BOUNTY_LOGS.TIME_CLAIMED, KITPVP_BOUNTY_LOGS.KILLER_ID, KITPVP_BOUNTY_LOGS.VICTIM_ID,
						KITPVP_BOUNTY_LOGS.BOUNTY_AMOUNT, KITPVP_BOUNTY_LOGS.BOUNTY_CURRENCY)
				.values(clock.instant().getEpochSecond(), killer.getUserId(), victim.getUserId(),
						rawValue, amount.currency().serialize())
				.execute();
	}

	/**
	 * Clears all caches which may be in use. Primarily intended for testing purposes
	 *
	 */
	@Override
	public void clearCaches() {
		existingKits.invalidateAll();
	}

}

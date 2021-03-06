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

import gg.solarmc.loader.DataCenter;
import gg.solarmc.loader.OnlineSolarPlayer;
import gg.solarmc.loader.SolarPlayer;
import gg.solarmc.loader.data.DataKey;
import gg.solarmc.loader.data.DataManager;
import org.jooq.DSLContext;
import org.jooq.Record2;
import space.arim.omnibus.util.UUIDUtil;
import space.arim.omnibus.util.concurrent.CentralisedFuture;
import space.arim.omnibus.util.concurrent.FactoryOfTheFuture;

import java.util.Optional;
import java.util.UUID;

import static gg.solarmc.loader.schema.tables.LatestNames.LATEST_NAMES;

/**
 * Data center implementation
 *
 */
public final class SimpleDataCenter implements DataCenter {

	private final FactoryOfTheFuture futuresFactory;
	private final Icarus icarus;
	private final PlayerTracker playerTracker;
	private final LoginHandler loginHandler;

	/**
	 * Creates
	 *
	 * @param futuresFactory the futures factory
	 * @param icarus the icarus
	 * @param playerTracker the player tracker
	 * @param loginHandler the login handler used to create offline users
	 */
	public SimpleDataCenter(FactoryOfTheFuture futuresFactory, Icarus icarus,
							PlayerTracker playerTracker, LoginHandler loginHandler) {
		this.futuresFactory = futuresFactory;
		this.icarus = icarus;
		this.playerTracker = playerTracker;
		this.loginHandler = loginHandler;
	}

	@Override
	public CentralisedFuture<?> runTransact(DataCenter.TransactionRunner runner) {
		return icarus.transactionSource().runTransact(runner);
	}

	@Override
	public <R> CentralisedFuture<R> transact(DataCenter.TransactionActor<R> actor) {
		return icarus.transactionSource().transact(actor);
	}

	@Override
	public <M extends DataManager> M getDataManager(DataKey<?, ?, M> key) {
		return icarus.dataManagement().getDataManager(key);
	}

	@Override
	public CentralisedFuture<Optional<SolarPlayer>> lookupPlayer(String name) {
		Optional<OnlineSolarPlayer> instantPlayer = playerTracker.getOnlinePlayerForName(name);
		if (instantPlayer.isPresent()) {
			return futuresFactory.completedFuture(upcastOptional(instantPlayer));
		}
		return transact((transaction) -> {
			Record2<Integer, byte[]> latestNamesRecord = transaction.getProperty(DSLContext.class)
					.select(LATEST_NAMES.USER_ID, LATEST_NAMES.UUID)
					.from(LATEST_NAMES)
					.where(LATEST_NAMES.USERNAME.eq(name))
					.fetchOne();
			if (latestNamesRecord == null) {
				return Optional.empty();
			}
			int userId = latestNamesRecord.value1();
			UUID mcUuid = UUIDUtil.fromByteArray(latestNamesRecord.value2());
			return Optional.of(loginHandler.createOfflineUser(userId, mcUuid));
		});
	}

	@Override
	public CentralisedFuture<Optional<SolarPlayer>> lookupPlayer(UUID uuid) {
		Optional<OnlineSolarPlayer> instantPlayer = playerTracker.getOnlinePlayerForUuid(uuid);
		if (instantPlayer.isPresent()) {
			return futuresFactory.completedFuture(upcastOptional(instantPlayer));
		}
		return transact((transaction) -> {
			Integer userId = transaction.getProperty(DSLContext.class)
					.select(LATEST_NAMES.USER_ID)
					.from(LATEST_NAMES)
					.where(LATEST_NAMES.UUID.eq(UUIDUtil.toByteArray(uuid)))
					.fetchOne(LATEST_NAMES.USER_ID);
			if (userId == null) {
				return Optional.empty();
			}
			return Optional.of(loginHandler.createOfflineUser(userId, uuid));
		});
	}

	@SuppressWarnings({"OptionalUsedAsFieldOrParameterType", "unchecked"})
	private static <T extends R, R> Optional<R> upcastOptional(Optional<T> optional) {
		return (Optional<R>) optional;
	}

}

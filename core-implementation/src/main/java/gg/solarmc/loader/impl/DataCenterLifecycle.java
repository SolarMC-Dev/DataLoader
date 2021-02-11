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

import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

class DataCenterLifecycle implements AutoCloseable {

	private final ExecutorService executor;
	private final HikariDataSource dataSource;
	private final Set<DataGroup<?, ?, ?>> groups;

	DataCenterLifecycle(ExecutorService executor, HikariDataSource dataSource, Set<DataGroup<?, ?, ?>> groups) {
		this.executor = executor;
		this.dataSource = dataSource;
		this.groups = Set.copyOf(groups);
	}

	private Logger getLogger() {
		return LoggerFactory.getLogger(getClass());
	}

	@Override
	public void close() throws Exception {
		Set<Exception> exceptions = new HashSet<>();
		try {
			executor.shutdown();
			boolean awaited = executor.awaitTermination(10L, TimeUnit.SECONDS);
			if (!awaited) {
				getLogger().warn("Failed to await termination of thread pool operations");
			}
		} catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
			getLogger().warn("Interrupted while awaiting termination of thread pool", ex);
		} catch (RuntimeException ex) {
			exceptions.add(ex);
		}
		try {
			dataSource.close();
		} catch (RuntimeException ex) {
			exceptions.add(ex);
		}
		for (DataGroup<?, ?, ?> group : groups) {
			try {
				group.manager().close();
			} catch (Exception ex) {
				exceptions.add(ex);
			}
		}
		if (!exceptions.isEmpty()) {
			// Throw the first exception. Add the rest of the exceptions as suppressed exceptions
			Iterator<Exception> exceptionIterator = exceptions.iterator();
			Exception firstEx = exceptionIterator.next();
			exceptionIterator.remove();
			exceptions.forEach(firstEx::addSuppressed);
			throw firstEx;
		}
	}
}

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

import gg.solarmc.loader.data.DataKeyInitializationContext;
import space.arim.omnibus.Omnibus;
import space.arim.omnibus.util.concurrent.FactoryOfTheFuture;

import java.nio.file.Path;

class DataKeyInitializationContextImpl implements DataKeyInitializationContext {

	private final Omnibus omnibus;
	private final FactoryOfTheFuture futuresFactory;
	private final Path configFolder;

	DataKeyInitializationContextImpl(Omnibus omnibus, FactoryOfTheFuture futuresFactory, Path configFolder) {
		this.omnibus = omnibus;
		this.futuresFactory = futuresFactory;
		this.configFolder = configFolder;
	}

	@Override
	public Omnibus omnibus() {
		return omnibus;
	}

	@Override
	public FactoryOfTheFuture futuresFactory() {
		return futuresFactory;
	}

	@Override
	public Path configFolder() {
		return configFolder;
	}
}

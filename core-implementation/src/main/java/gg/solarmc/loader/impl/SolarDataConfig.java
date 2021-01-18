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

import space.arim.dazzleconf.annote.ConfComments;
import space.arim.dazzleconf.annote.ConfDefault;
import space.arim.dazzleconf.annote.ConfHeader;
import space.arim.dazzleconf.annote.ConfKey;

import java.util.Map;

@ConfHeader("MariaDB connection details")
public interface SolarDataConfig {

	@ConfDefault.DefaultString("localhost")
	String host();

	@ConfDefault.DefaultString("solardata")
	String database();

	@ConfDefault.DefaultInteger(3306)
	int port();

	@ConfDefault.DefaultString("username")
	String username();

	@ConfDefault.DefaultString("password")
	String password();

	@ConfKey("connection-properties")
	@ConfComments("Connection properties applied to the JDBC url")
	@ConfDefault.DefaultMap({
			"useUnicode", "true",
			"characterEncoding", "UTF-8",
			"useServerPrepStmts", "true",
			"cachePrepStmts", "true",
			"prepStmtCacheSqlLimit", "256"})
	Map<String, String> connectionProperties();

	@ConfKey("connection-timeout-seconds")
	@ConfComments({"Maximum time to wait when acquiring a connection from the pool"})
	@ConfDefault.DefaultInteger(14)
	int connectionTimeoutSeconds();

	@ConfKey("max-lifetime-minutes")
	@ConfComments({
			"Maximum lifetime of a connection in the pool. Per HikariCP,",
			"\"It should be several seconds shorter than any database or infrastructure imposed connection time limit\""})
	@ConfDefault.DefaultInteger(25)
	int maxLifetimeMinutes();

	@ConfKey("pool-size")
	@ConfComments("Connection pool size. More is not always better.")
	@ConfDefault.DefaultInteger(8)
	int poolSize();
}

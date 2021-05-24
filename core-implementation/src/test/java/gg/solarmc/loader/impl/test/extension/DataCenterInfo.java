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

package gg.solarmc.loader.impl.test.extension;

import gg.solarmc.loader.DataCenter;
import gg.solarmc.loader.OnlineSolarPlayer;
import gg.solarmc.loader.impl.Icarus;
import gg.solarmc.loader.impl.IcarusLauncher;
import gg.solarmc.loader.impl.SolarDataConfig;
import gg.solarmc.loader.impl.LoginHandler;
import gg.solarmc.loader.impl.PlayerTracker;
import gg.solarmc.loader.impl.SimpleDataCenter;
import gg.solarmc.loader.impl.UserDetails;
import space.arim.omnibus.DefaultOmnibus;
import space.arim.omnibus.util.concurrent.FactoryOfTheFuture;
import space.arim.omnibus.util.concurrent.impl.IndifferentFactoryOfTheFuture;

import java.nio.file.Path;
import java.util.concurrent.Executors;

public record DataCenterInfo(Icarus icarus, DataCenter dataCenter, LoginHandler loginHandler) {

    public OnlineSolarPlayer loginUser(UserDetails userDetails) {
        return loginHandler.loginUser(userDetails).join();
    }

    public static DataCenterInfo create(Path folder, SolarDataConfig.DatabaseCredentials credentials) {
        FactoryOfTheFuture futuresFactory = new IndifferentFactoryOfTheFuture();
        IcarusLauncher icarusLauncher = new IcarusLauncher(
                folder, futuresFactory,
                new DefaultOmnibus(), Executors::newFixedThreadPool);

        Icarus icarus = icarusLauncher.launch(credentials);

        PlayerTracker playerTracker = new EmptyPlayerTracker();
        SolarDataConfig.Logins logins = icarusLauncher.loadConfig().logins();
        assert !logins.createUserIfNotExists();
        LoginHandler loginHandler = icarus.loginHandlerBuilder(logins)
                .updateNameAddressHistory().build(playerTracker);
        return new DataCenterInfo(
                icarus,
                new SimpleDataCenter(futuresFactory, icarus, playerTracker, loginHandler),
                loginHandler);
    }

    public void close() {
        icarus.close();
    }

}

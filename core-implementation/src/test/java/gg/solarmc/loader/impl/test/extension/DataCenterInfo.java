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
import space.arim.omnibus.Omnibus;
import space.arim.omnibus.util.concurrent.FactoryOfTheFuture;
import space.arim.omnibus.util.concurrent.impl.IndifferentFactoryOfTheFuture;

import java.nio.file.Path;
import java.util.Objects;
import java.util.concurrent.Executors;

public record DataCenterInfo(Icarus icarus, DataCenter dataCenter, LoginHandler loginHandler) {

    /**
     * Logs in the player with the given details
     *
     * @param userDetails the user details
     * @return the online solar player
     */
    public OnlineSolarPlayer loginUser(UserDetails userDetails) {
        return loginHandler.loginUser(userDetails).join();
    }

    /**
     * Generates a new user with random details and logs the user in
     *
     * @return the online solar player
     */
    public OnlineSolarPlayer loginNewRandomUser() {
        return loginUser(DataGenerator.newRandomUser());
    }

    /**
     * Shortcut for {@link DataCenter#transact(DataCenter.TransactionActor)}
     *
     * @param actor the transaction actor
     * @param <T> the result type
     * @return the result
     */
    public <T> T transact(DataCenter.TransactionActor<T> actor) {
        return dataCenter().transact(actor).join();
    }

    /**
     * Shortcut for {@link DataCenter#runTransact(DataCenter.TransactionRunner)}
     *
     * @param runner the transaction runner
     */
    public void runTransact(DataCenter.TransactionRunner runner) {
        dataCenter().runTransact(runner).join();
    }

    public void close() {
        icarus.close();
    }

    /**
     * Begins creating a data center info
     *
     * @param folder the launch directory
     * @param credentials the database credentials
     * @return the builder
     */
    public static Builder builder(Path folder, SolarDataConfig.DatabaseCredentials credentials) {
        return new Builder(folder, credentials);
    }

    public static class Builder {

        private final Path folder;
        private final SolarDataConfig.DatabaseCredentials credentials;

        private Omnibus omnibus = new DefaultOmnibus();

        Builder(Path folder, SolarDataConfig.DatabaseCredentials credentials) {
            this.folder = folder;
            this.credentials = credentials;
        }

        public Builder omnibus(Omnibus omnibus) {
            this.omnibus = Objects.requireNonNull(omnibus);
            return this;
        }

        /**
         * Creates a data center. The associated {@code LoginHandler} will assume
         * users exist and will update their name and address history when called
         *
         * @return the data center info
         */
        public DataCenterInfo build() {
            FactoryOfTheFuture futuresFactory = new IndifferentFactoryOfTheFuture();
            IcarusLauncher icarusLauncher = new IcarusLauncher(
                    folder, futuresFactory,
                    omnibus, Executors::newFixedThreadPool);

            Icarus icarus = icarusLauncher.launch(credentials);

            PlayerTracker playerTracker = new EmptyPlayerTracker();
            SolarDataConfig.Logins logins = icarusLauncher.loadConfig().logins();
            assert !logins.createUserIfNotExists();
            LoginHandler loginHandler = icarus.loginHandlerBuilder(logins)
                    .createUserIfNotExists().updateNameAddressHistory().build(playerTracker);
            return new DataCenterInfo(
                    icarus,
                    new SimpleDataCenter(futuresFactory, icarus, playerTracker, loginHandler),
                    loginHandler);
        }
    }

}

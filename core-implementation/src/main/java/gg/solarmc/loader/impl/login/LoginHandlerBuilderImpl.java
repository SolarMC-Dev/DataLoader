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

package gg.solarmc.loader.impl.login;

import gg.solarmc.loader.impl.launch.DataGroup;
import gg.solarmc.loader.impl.LoginHandler;
import gg.solarmc.loader.impl.PlayerTracker;
import gg.solarmc.loader.impl.TransactionSource;

import java.util.Set;

public final class LoginHandlerBuilderImpl implements LoginHandler.Builder {

    private final TransactionSource transactionSource;
    private final Set<DataGroup<?, ?, ?>> groups;

    private boolean createUserIfNotExists;
    private boolean updateNameAddressHistory;

    public LoginHandlerBuilderImpl(TransactionSource transactionSource, Set<DataGroup<?, ?, ?>> groups) {
        this.transactionSource = transactionSource;
        this.groups = groups;
    }

    @Override
    public LoginHandler.Builder createUserIfNotExists() {
        createUserIfNotExists = true;
        return this;
    }

    @Override
    public LoginHandler.Builder updateNameAddressHistory() {
        updateNameAddressHistory = true;
        return this;
    }

    @Override
    public LoginHandler build(PlayerTracker playerTracker) {
        return new LoginHandlerImpl(
                transactionSource, groups, playerTracker,
                (createUserIfNotExists) ?
                        new IdRetrieval.CreateUserIfNotExists() : new IdRetrieval.AssumeUserExists(),
                (updateNameAddressHistory) ?
                        new NameAddressHistoryUpdate.FunctioningImpl() : new NameAddressHistoryUpdate.NoOpImpl());
    }

}

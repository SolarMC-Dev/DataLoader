/*
 *
 *  * dataloader
 *  * Copyright © 2021 SolarMC Developers
 *  *
 *  * dataloader is free software: you can redistribute it and/or modify
 *  * it under the terms of the GNU Affero General Public License as
 *  * published by the Free Software Foundation, either version 3 of the
 *  * License, or (at your option) any later version.
 *  *
 *  * dataloader is distributed in the hope that it will be useful,
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  * GNU Affero General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU Affero General Public License
 *  * along with dataloader. If not, see <https://www.gnu.org/licenses/>
 *  * and navigate to version 3 of the GNU Affero General Public License.
 *
 */

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.time.Duration;
import java.util.function.Function;
import java.util.function.Supplier;

public class ExpiringObject<T> {

    private final Cache<Boolean,T> internalCache;
    private final Supplier<T> function;

    public ExpiringObject(Supplier<T> function, Cache<Boolean,T> builder) {
        this.internalCache = builder;
        this.function = function;
    }

    public ExpiringObject(Supplier<T> function) {
        this.internalCache = Caffeine.newBuilder().expireAfterAccess(Duration.ofMinutes(15L)).build();
        this.function = function;
    }

    public T get() {
        return internalCache.get(Boolean.TRUE, (shutUp) -> {
            return function.get();
        });
    }

}

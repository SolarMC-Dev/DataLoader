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

import java.time.Duration;
import java.util.Objects;
import java.util.Set;

/**
 * Kit builder providing compile time safety through a series of steps.
 *
 */
public final class KitBuilder {

    private String name;
    private Set<ItemInSlot> contents;
    private Duration cooldown = Duration.ZERO;

    /**
     * Begins creating the kit builder
     *
     */
    public KitBuilder() {

    }

    public ContentsStep name(String name) {
        this.name = Objects.requireNonNull(name, "name");
        return new ContentsStep();
    }

    public final class ContentsStep {

        private ContentsStep() {}

        public AlmostReadyStep contents(Set<ItemInSlot> contents) {
            KitBuilder.this.contents = Objects.requireNonNull(contents, "contents");
            return new AlmostReadyStep();
        }
    }

    public final class AlmostReadyStep {

        private AlmostReadyStep() {}

        /**
         * Sets the cooldown. Zero (no cooldown) is the default
         *
         * @param cooldown the cooldown, or {@code Duration.ZERO} for none
         * @return this step
         */
        public AlmostReadyStep cooldown(Duration cooldown) {
            KitBuilder.this.cooldown = Objects.requireNonNull(cooldown, "cooldown");
            return this;
        }

        public Built build() {
            return new Built(name, contents, cooldown);
        }

    }

    public static final class Built {

        private final String name;
        private final Set<ItemInSlot> contents;
        private final Duration cooldown;

        private Built(String name, Set<ItemInSlot> contents, Duration cooldown) {
            this.name = Objects.requireNonNull(name, "name");
            this.contents = Objects.requireNonNull(contents, "contents");
            this.cooldown = Objects.requireNonNull(cooldown, "cooldown");
        }

        public String name() {
            return name;
        }

        public Set<ItemInSlot> contents() {
            return contents;
        }

        public Duration cooldown() {
            return cooldown;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Built built = (Built) o;
            return name.equals(built.name) && contents.equals(built.contents) && cooldown.equals(built.cooldown);
        }

        @Override
        public int hashCode() {
            int result = name.hashCode();
            result = 31 * result + contents.hashCode();
            result = 31 * result + cooldown.hashCode();
            return result;
        }

        @Override
        public String toString() {
            return "Built{" +
                    "name='" + name + '\'' +
                    ", contents=" + contents +
                    ", cooldown=" + cooldown +
                    '}';
        }
    }
}

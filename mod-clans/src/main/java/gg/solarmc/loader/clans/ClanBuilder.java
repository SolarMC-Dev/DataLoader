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

package gg.solarmc.loader.clans;

import gg.solarmc.loader.Transaction;
import org.jooq.Record6;

import java.util.Objects;
import java.util.Set;

final class ClanBuilder {

    private ClanManager manager;
    private int clanId;

    private String name;
    private ClanMember leader;

    private int kills;
    private int deaths;
    private int assists;

    private Set<ClanMember> members;

    private ClanBuilder() {}

    static ClanBuilder usingManager(ClanManager manager) {
        ClanBuilder builder = new ClanBuilder();
        builder.manager = Objects.requireNonNull(manager);
        return builder;
    }

    Step1 clanId(int clanId) {
        this.clanId = clanId;
        return new Step1();
    }

    final class Step1 {

        private Step1() {}

        Step2 nameAndLeader(String name, ClanMember leader) {
            ClanBuilder.this.name = Objects.requireNonNull(name, "name");
            ClanBuilder.this.leader = Objects.requireNonNull(leader, "leader");
            return new Step2();
        }

    }

    final class Step2 {

        private Step2() {}

        Step3 killsDeathsAssists(int kills, int deaths, int assists) {
            if (kills < 0 || deaths < 0 || assists < 0) {
                throw new IllegalArgumentException(
                        "Kills, deaths, and assists must be non-negative, but was "
                                + kills + ", " + assists + ", " + deaths);
            }
            ClanBuilder.this.kills = kills;
            ClanBuilder.this.deaths = deaths;
            ClanBuilder.this.assists = assists;
            return new Step3();
        }
    }

    final class Step3 {

        private Step3() {}

        Ready members(Set<ClanMember> members) {
            ClanBuilder.this.members = Set.copyOf(members);
            if (!ClanBuilder.this.members.contains(leader)) {
                throw new IllegalStateException("Members must include leader");
            }
            return new Ready();
        }

        Ready fetchMembers(Transaction transaction) {
            return members(ClanMember.fetchMembers(transaction, clanId));
        }

    }

    final class Ready {

        private Ready() {}

        Clan build() {
            return new Clan(manager, clanId, name, leader, kills, deaths, assists, members);
        }
    }

    static Clan fromRecordAndFetchMembers(ClanManager manager, Record6<Integer, String, Integer, Integer, Integer, Integer> record,
                             Transaction transaction) {
        return ClanBuilder.usingManager(manager)
                .clanId(record.value1())
                .nameAndLeader(record.value2(), new ClanMember(record.value3()))
                .killsDeathsAssists(record.value4(), record.value5(), record.value6())
                .fetchMembers(transaction)
                .build();
    }

}

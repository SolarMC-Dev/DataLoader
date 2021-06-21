package gg.solarmc.loader.kitpvp.test;

import gg.solarmc.loader.impl.SolarDataConfig;
import gg.solarmc.loader.impl.test.extension.DataCenterInfo;
import gg.solarmc.loader.impl.test.extension.DataGenerator;
import gg.solarmc.loader.impl.test.extension.DatabaseExtension;
import gg.solarmc.loader.kitpvp.ItemInSlot;
import gg.solarmc.loader.kitpvp.ItemSerializer;
import gg.solarmc.loader.kitpvp.Kit;
import gg.solarmc.loader.kitpvp.KitItem;
import gg.solarmc.loader.kitpvp.KitOwnershipResult;
import gg.solarmc.loader.kitpvp.KitPvp;
import gg.solarmc.loader.kitpvp.KitPvpKey;
import gg.solarmc.loader.kitpvp.KitPvpManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import space.arim.omnibus.DefaultOmnibus;
import space.arim.omnibus.Omnibus;

import java.nio.file.Path;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.mockito.Mockito.when;

@ExtendWith(DatabaseExtension.class)
@ExtendWith(MockitoExtension.class)
public class KitPvpIT {

    private final ItemSerializer itemSerializer;
    private DataCenterInfo dataCenterInfo;

    public KitPvpIT(@Mock ItemSerializer itemSerializer) {
        this.itemSerializer = itemSerializer;
    }

    @BeforeEach
    public void setDataCenter(@TempDir Path folder, SolarDataConfig.DatabaseCredentials credentials) {
        Omnibus omnibus = new DefaultOmnibus();
        omnibus.getRegistry().register(ItemSerializer.class, (byte) 0, itemSerializer, "Serializer");
        dataCenterInfo = DataCenterInfo.builder(folder, credentials).omnibus(omnibus).build();
    }

    private Kit newKit(String name, Set<ItemInSlot> items) {
       return dataCenterInfo.transact((tx) -> {
            KitPvpManager manager = dataCenterInfo.dataCenter().getDataManager(KitPvpKey.INSTANCE);
            return manager.createKit(tx, name, items);
       });
    }

    private void addKitAssumeSuccess(KitPvp data, Kit kit) {
        KitOwnershipResult kitOwnership = dataCenterInfo.transact((tx) -> {
            return data.addKit(tx, kit);
        });
        assumeTrue(kitOwnership.isChanged());
    }

    @Test
    public void addKit() {
        KitPvp data = dataCenterInfo.loginNewRandomUser().getData(KitPvpKey.INSTANCE);
        Kit kit = newKit("MyKit", Set.of());
        KitOwnershipResult kitOwnership = dataCenterInfo.transact((tx) -> {
            return data.addKit(tx, kit);
        });
        assertTrue(kitOwnership.isChanged());
    }

    @Test
    public void addKitAlreadyPresent() {
        KitPvp data = dataCenterInfo.loginNewRandomUser().getData(KitPvpKey.INSTANCE);
        Kit kit = newKit("MyExistingKit", Set.of());
        addKitAssumeSuccess(data, kit);

        // Do it again
        KitOwnershipResult kitOwnership = dataCenterInfo.transact((tx) -> {
            return data.addKit(tx, kit);
        });
        assertFalse(kitOwnership.isChanged(), "Kit should already be added");
    }

    @Test
    public void removeKit() {
        KitPvp data = dataCenterInfo.loginNewRandomUser().getData(KitPvpKey.INSTANCE);
        Kit kit = newKit("MyKit", Set.of());
        addKitAssumeSuccess(data, kit);

        KitOwnershipResult kitOwnership = dataCenterInfo.transact((tx) -> {
            return data.removeKit(tx, kit);
        });
        assertTrue(kitOwnership.isChanged());
    }

    @Test
    public void removeKitNotPresent() {
        KitPvp data = dataCenterInfo.loginNewRandomUser().getData(KitPvpKey.INSTANCE);
        Kit kit = newKit("MyNonExistingKit", Set.of());
        KitOwnershipResult kitOwnership = dataCenterInfo.transact((tx) -> {
            return data.removeKit(tx, kit);
        });
        assertFalse(kitOwnership.isChanged());
    }
    
    @Test
    public void serializeItems(@Mock KitItem<?> item1, @Mock KitItem<?> item2) {
        byte[] item1Data = DataGenerator.randomBytes(1, 15);
        byte[] item2Data = DataGenerator.randomBytes(1, 15);
        when(itemSerializer.serialize(item1)).thenReturn(item1Data);
        when(itemSerializer.serialize(item2)).thenReturn(item2Data);
        when(itemSerializer.deserialize(item1Data)).thenAnswer(invocation -> item1);
        when(itemSerializer.deserialize(item2Data)).thenAnswer(invocation -> item2);
        Set<ItemInSlot> contents = Set.of(new ItemInSlot(3, item1), new ItemInSlot(5, item2));

        Kit originalKit = newKit("MyItemKit", contents);
        KitPvp userData = dataCenterInfo.loginNewRandomUser().getData(KitPvpKey.INSTANCE);
        addKitAssumeSuccess(userData, originalKit);

        Set<Kit> kits = dataCenterInfo.transact(userData::getKits);
        assertEquals(1, kits.size(), () -> "" + kits);
        Kit reloadedKit = kits.iterator().next();
        assertEquals(contents, reloadedKit.getContents());
    }
}

package gg.solarmc.loader.kitpvp.test;

import gg.solarmc.loader.impl.SolarDataConfig;
import gg.solarmc.loader.impl.test.extension.DataCenterInfo;
import gg.solarmc.loader.impl.test.extension.DataGenerator;
import gg.solarmc.loader.impl.test.extension.DatabaseExtension;
import gg.solarmc.loader.kitpvp.ItemInSlot;
import gg.solarmc.loader.kitpvp.ItemSerializer;
import gg.solarmc.loader.kitpvp.Kit;
import gg.solarmc.loader.kitpvp.KitItem;
import gg.solarmc.loader.kitpvp.KitOwnership;
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

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

@ExtendWith(DatabaseExtension.class)
@ExtendWith(MockitoExtension.class)
public class KitPvpIT {

    private final ItemSerializer itemSerializer;
    private DataCenterInfo dataCenterInfo;
    private KitPvpManager manager;

    public KitPvpIT(@Mock ItemSerializer itemSerializer) {
        this.itemSerializer = itemSerializer;
    }

    @BeforeEach
    public void setDataCenter(@TempDir Path folder, SolarDataConfig.DatabaseCredentials credentials) {
        Omnibus omnibus = new DefaultOmnibus();
        omnibus.getRegistry().register(ItemSerializer.class, (byte) 0, itemSerializer, "Serializer");
        dataCenterInfo = DataCenterInfo.builder(folder, credentials).omnibus(omnibus).build();
        manager = dataCenterInfo.dataCenter().getDataManager(KitPvpKey.INSTANCE);
    }

    private Kit newKit(String name, Set<ItemInSlot> items) {
       return dataCenterInfo.transact((tx) -> manager.createKit(tx, name, items).orElseThrow());
    }

    private void addKitAssumeSuccess(KitPvp data, Kit kit) {
        KitOwnership kitOwnership = dataCenterInfo.transact((tx) -> {
            return data.addKit(tx, kit);
        });
        assumeTrue(kitOwnership.isChanged());
    }

    @Test
    public void createKitAlreadyExists() {
        String name = "MyExistingKit";
        newKit(name, Set.of());
        assertEquals(Optional.empty(), dataCenterInfo.transact((tx) -> manager.createKit(tx, name, Set.of())));
    }

    @Test
    public void addKit() {
        KitPvp data = dataCenterInfo.loginNewRandomUser().getData(KitPvpKey.INSTANCE);
        Kit kit = newKit("MyKit", Set.of());
        KitOwnership kitOwnership = dataCenterInfo.transact((tx) -> {
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
        KitOwnership kitOwnership = dataCenterInfo.transact((tx) -> {
            return data.addKit(tx, kit);
        });
        assertFalse(kitOwnership.isChanged(), "Kit should already be added");
    }

    @Test
    public void removeKit() {
        KitPvp data = dataCenterInfo.loginNewRandomUser().getData(KitPvpKey.INSTANCE);
        Kit kit = newKit("MyKit", Set.of());
        addKitAssumeSuccess(data, kit);

        KitOwnership kitOwnership = dataCenterInfo.transact((tx) -> {
            return data.removeKit(tx, kit);
        });
        assertTrue(kitOwnership.isChanged());
    }

    @Test
    public void removeKitNotPresent() {
        KitPvp data = dataCenterInfo.loginNewRandomUser().getData(KitPvpKey.INSTANCE);
        Kit kit = newKit("MyNonExistingKit", Set.of());
        KitOwnership kitOwnership = dataCenterInfo.transact((tx) -> {
            return data.removeKit(tx, kit);
        });
        assertFalse(kitOwnership.isChanged());
    }

    private void mockItemSerialization(KitItem item, byte[] data) throws IOException {
        doAnswer((invocation) -> {
            var output = invocation.getArgument(1, OutputStream.class);
            output.write(data);
            output.flush();
            return null;
        }).when(itemSerializer).serialize(eq(item), any());
        when(itemSerializer.deserialize(argThat(new InputStreamMatcher(data))))
                .thenAnswer((i) -> item);
    }

    @Test
    public void serializeItems(@Mock KitItem item1, @Mock KitItem item2) throws IOException {
        byte[] item1Data = DataGenerator.randomBytes(1, 15);
        byte[] item2Data = DataGenerator.randomBytes(1, 15);
        mockItemSerialization(item1, item1Data);
        mockItemSerialization(item2, item2Data);
        Set<ItemInSlot> contents = Set.of(new ItemInSlot(3, item1), new ItemInSlot(5, item2));

        Kit originalKit = newKit("MyItemKit", contents);
        KitPvp userData = dataCenterInfo.loginNewRandomUser().getData(KitPvpKey.INSTANCE);
        addKitAssumeSuccess(userData, originalKit);

        Set<Kit> kits = dataCenterInfo.transact(userData::getKits);
        assertEquals(1, kits.size(), () -> "" + kits);
        Kit reloadedKit = kits.iterator().next();
        assertEquals(contents, reloadedKit.getContents());
    }

    private void assertKitExistence(
            @SuppressWarnings("OptionalUsedAsFieldOrParameterType") Optional<Kit> expected, Kit kit) {
        assertEquals(expected, dataCenterInfo.transact((tx) -> manager.getKitById(tx, kit.getId())));
        assertEquals(expected, dataCenterInfo.transact((tx) -> manager.getKitByName(tx, kit.getName())));
    }

    private void assertKitExists(Kit kit) {
        assertKitExistence(Optional.of(kit), kit);
    }

    private void assertKitNotExists(Kit kit) {
        assertKitExistence(Optional.empty(), kit);
    }

    @Test
    public void getKitByIdAndName() {
        Kit kit = newKit("MyKitByIdAndName", Set.of());

        assertKitExists(kit);
        manager.clearCaches();
        assertKitExists(kit);
    }

    @Test
    public void deleteKit() {
        Kit kit = newKit("MyKitById", Set.of());

        boolean deleted = dataCenterInfo.transact((tx) -> manager.deleteKit(tx, kit));
        assertTrue(deleted);
        assertKitNotExists(kit);
        manager.clearCaches();
        assertKitNotExists(kit);
    }

    @Test
    public void deleteKitById() {
        Kit kit = newKit("MyKitById", Set.of());

        boolean deleted = dataCenterInfo.transact((tx) -> manager.deleteKitById(tx, kit.getId()));
        assertTrue(deleted);
        assertKitNotExists(kit);
        manager.clearCaches();
        assertKitNotExists(kit);
    }

    @Test
    public void deleteKitByName() {
        Kit kit = newKit("MyKitByName", Set.of());

        boolean deleted = dataCenterInfo.transact((tx) -> manager.deleteKitByName(tx, kit.getName()));
        assertTrue(deleted);
        assertKitNotExists(kit);
        manager.clearCaches();
        assertKitNotExists(kit);
    }
}

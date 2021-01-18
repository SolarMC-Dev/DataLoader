package gg.solarmc.loader.data;

import gg.solarmc.loader.DataCenter;

/**
 * Marker interface representing a container that caches mutable data. <br>
 * <br>
 * Usually, a data object contains some cached/displayable read-only data, values of which
 * should not be relied upon for correctness. This data is available via getters. <br>
 * <br>
 * A data object also exposes methods to manipulate the data itself via a transactional API.
 * {@link DataCenter} should be used to run such transactions.
 */
public interface DataObject {

}

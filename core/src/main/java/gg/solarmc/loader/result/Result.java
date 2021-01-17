package gg.solarmc.loader.result;

import gg.solarmc.loader.data.DataObject;

/**
 * Represents something that is returned from a transactional interaction with a {@link DataObject}
 *
 */
public interface Result<T> {
    T newResult();
}

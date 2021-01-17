package gg.solarmc.loader.result;

import gg.solarmc.loader.result.Result;

/**
 * Represents a Result that has the ability to fail
 *
 */
public interface FailableResult<T> extends Result<T> {
    boolean isSuccess();
}

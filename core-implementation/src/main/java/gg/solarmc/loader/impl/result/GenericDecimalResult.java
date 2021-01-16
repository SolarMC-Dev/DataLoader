package gg.solarmc.loader.impl.result;

import gg.solarmc.loader.result.Result;

import java.math.BigDecimal;

public class GenericDecimalResult implements Result<BigDecimal> {

    private final BigDecimal newResult;

    public GenericDecimalResult(BigDecimal newResult) {
        this.newResult = newResult;
    }

    @Override
    public BigDecimal newResult() {
        return newResult;
    }
}

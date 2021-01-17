package gg.solarmc.loader;

public interface DataObject {

    /**
     * Get the player this dataObject is contained within
     *
     * @return the {@link SolarPlayer} this is contained within
     */

    SolarPlayer getBoundPlayer();

}

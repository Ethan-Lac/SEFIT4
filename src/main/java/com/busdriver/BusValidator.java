package com.busdriver;

/**
 * Validation helper for Bus rules B1-B5.
 */
public class BusValidator {

    /**
     * B1: Bus ID must be exactly 8 characters and all characters must be digits.
     *
     * @param busID bus ID to validate
     * @return true when busID follows the required format
     */
    public static boolean isValidBusID(String busID) {
        return busID != null && busID.matches("\\d{8}");
    }

    /**
     * Validates the allowed fuel type values for the assignment.
     *
     * @param fuelType fuel type to validate
     * @return true for Diesel, Hybrid, or Electricity
     */
    public static boolean isValidFuelType(String fuelType) {
        return fuelType != null &&
               (fuelType.equals("Diesel") ||
                fuelType.equals("Hybrid") ||
                fuelType.equals("Electricity"));
    }

    /**
     * Validates the basic Bus fields.
     *
     * @param bus bus to validate
     * @return true when the bus has valid ID, capacity, fuel level, and fuel type
     */
    public static boolean isValidBus(Bus bus) {
        return bus != null
            && isValidBusID(bus.getBusID())
            && bus.getCapacity() > 0
            && bus.getFuelLevel() >= 0
            && bus.getFuelLevel() <= 100
            && isValidFuelType(bus.getFuelType());
    }

    /**
     * B2: Capacity cannot increase during an update, but it may stay the same or decrease.
     *
     * @param existingBus existing stored bus
     * @param updatedBus  proposed updated bus
     * @return true when the update does not increase capacity
     */
    public static boolean isCapacityUpdateAllowed(Bus existingBus, Bus updatedBus) {
        if (existingBus == null || updatedBus == null) return false;
        return updatedBus.getCapacity() <= existingBus.getCapacity();
    }

    /**
     * B3-B5: Checks whether a driver may operate the selected bus.
     *
     * Rules:
     * - B3: Drivers older than 50 cannot drive buses with capacity 50 or more.
     * - B4: Electric buses require at least 5 years of experience.
     * - B5: Electric and hybrid buses require Heavy or PublicTransport licence.
     *
     * @param driver driver being assigned
     * @param bus    bus being operated
     * @return true when all operation restrictions are satisfied
     */
    public static boolean canDriverOperateBus(Driver driver, Bus bus) {
        if (driver == null || bus == null || !isValidBus(bus) || !driver.isValid()) {
            return false;
        }

        if (driver.getAge() > 50 && bus.getCapacity() >= 50) {
            return false;
        }

        if (bus.getFuelType().equals("Electricity") && driver.getExperienceYears() < 5) {
            return false;
        }

        if (bus.getFuelType().equals("Electricity") || bus.getFuelType().equals("Hybrid")) {
            return driver.getLicenseType().equals("Heavy") ||
                   driver.getLicenseType().equals("PublicTransport");
        }

        return true;
    }
}

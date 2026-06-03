package com.busdriver;

/**
 * Represents a bus in the Intelligent Bus Driver Guidance System.
 * The Bus module validates rules B1-B5 through BusValidator and BusRepository.
 */
public class Bus {

    private String busID;
    private int capacity;
    private double fuelLevel;
    private String fuelType; // Diesel, Hybrid, Electricity

    /**
     * Default constructor required for Gson deserialization.
     */
    public Bus() {}

    /**
     * Full constructor for creating a bus record.
     *
     * @param busID     Unique 8-digit bus ID
     * @param capacity  Passenger capacity
     * @param fuelLevel Current fuel or battery level
     * @param fuelType  Diesel, Hybrid, or Electricity
     */
    public Bus(String busID, int capacity, double fuelLevel, String fuelType) {
        this.busID = busID;
        this.capacity = capacity;
        this.fuelLevel = fuelLevel;
        this.fuelType = fuelType;
    }

    // Getters

    public String getBusID() { return busID; }
    public int getCapacity() { return capacity; }
    public double getFuelLevel() { return fuelLevel; }
    public String getFuelType() { return fuelType; }

    // Setters

    public void setBusID(String busID) { this.busID = busID; }
    public void setCapacity(int capacity) { this.capacity = capacity; }
    public void setFuelLevel(double fuelLevel) { this.fuelLevel = fuelLevel; }
    public void setFuelType(String fuelType) { this.fuelType = fuelType; }

    /**
     * Validates this Bus object's basic fields.
     *
     * @return true when all basic bus fields are valid
     */
    public boolean isValid() {
        return BusValidator.isValidBus(this);
    }

    @Override
    public String toString() {
        return "Bus{" +
               "busID='" + busID + '\'' +
               ", capacity=" + capacity +
               ", fuelLevel=" + fuelLevel +
               ", fuelType='" + fuelType + '\'' +
               '}';
    }
}

package com.busdriver;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Repository class for managing Driver records.
 * All data is persisted to/from a JSON file.
 * Enforces business rules D1-D5.
 */
public class DriverRepository {

    private final String filePath;          // Path to the JSON storage file
    private final Gson gson;                // Gson instance for JSON serialization

    /**
     * Default constructor — uses "data/drivers.json" as storage file.
     */
    public DriverRepository() {
        this("data/drivers.json");
    }

    /**
     * Constructor with custom file path (useful for integration tests).
     *
     * @param filePath Path to the JSON file used for storage
     */
    public DriverRepository(String filePath) {
        this.filePath = filePath;
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        ensureFileExists();
    }

    //  Private Helpers 

    /**
     * Creates the JSON file and parent directories if they do not exist.
     */
    private void ensureFileExists() {
        File file = new File(filePath);
        file.getParentFile().mkdirs();
        if (!file.exists()) {
            try (Writer writer = new FileWriter(file)) {
                writer.write("[]"); // Start with an empty JSON array
            } catch (IOException e) {
                throw new RuntimeException("Cannot create driver storage file: " + filePath, e);
            }
        }
    }

    /**
     * Reads all drivers from the JSON file.
     *
     * @return List of all stored drivers
     */
    private List<Driver> readAll() {
        try (Reader reader = new FileReader(filePath)) {
            Type listType = new TypeToken<List<Driver>>() {}.getType();
            List<Driver> drivers = gson.fromJson(reader, listType);
            return drivers != null ? drivers : new ArrayList<>();
        } catch (IOException e) {
            throw new RuntimeException("Cannot read driver storage file: " + filePath, e);
        }
    }

    /**
     * Writes all drivers to the JSON file.
     *
     * @param drivers The list of drivers to persist
     */
    private void writeAll(List<Driver> drivers) {
        try (Writer writer = new FileWriter(filePath)) {
            gson.toJson(drivers, writer);
        } catch (IOException e) {
            throw new RuntimeException("Cannot write to driver storage file: " + filePath, e);
        }
    }

    //  Public Operations 

    /**
     * Adds a new driver to the repository.
     * Validates all fields and checks for duplicate driverID.
     *
     * @param driver The driver to add
     * @throws IllegalArgumentException if any validation rule is violated
     */
    public void add(Driver driver) {
        // D1: Validate driverID format
        if (!Driver.isValidDriverID(driver.getDriverID())) {
            throw new IllegalArgumentException(
                "Invalid driverID format. Must be 10 chars: digits(2-9) for first two, " +
                "at least 2 special chars in positions 3-8, uppercase letters for last two.");
        }

        // D2: Validate address format
        if (!Driver.isValidAddress(driver.getAddress())) {
            throw new IllegalArgumentException(
                "Invalid address format. Expected: StreetNo|StreetName|City|State|Country");
        }

        // D3: Validate birthdate format
        if (!Driver.isValidBirthdate(driver.getBirthdate())) {
            throw new IllegalArgumentException(
                "Invalid birthdate format. Expected: DD-MM-YYYY");
        }

        // Validate licenseType
        if (!Driver.isValidLicenseType(driver.getLicenseType())) {
            throw new IllegalArgumentException(
                "Invalid licenseType. Must be: Light, Medium, Heavy, or PublicTransport");
        }

        // Validate name
        if (driver.getName() == null || driver.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Driver name cannot be empty.");
        }

        // Validate experience
        if (driver.getExperienceYears() < 0) {
            throw new IllegalArgumentException("Experience years cannot be negative.");
        }

        List<Driver> drivers = readAll();

        // D1: Check for duplicate driverID
        for (Driver existing : drivers) {
            if (existing.getDriverID().equals(driver.getDriverID())) {
                throw new IllegalArgumentException(
                    "Duplicate driverID: " + driver.getDriverID() + " already exists.");
            }
        }

        drivers.add(driver);
        writeAll(drivers);
    }

    /**
     * Updates an existing driver's details.
     * Enforces D4 (license restriction) and D5 (immutable fields).
     *
     * @param updatedDriver Driver object containing the updated fields
     * @throws IllegalArgumentException if any rule is violated or driver not found
     */
    public void update(Driver updatedDriver) {
        List<Driver> drivers = readAll();

        // Find the existing driver by driverID
        Driver existing = null;
        int index = -1;
        for (int i = 0; i < drivers.size(); i++) {
            if (drivers.get(i).getDriverID().equals(updatedDriver.getDriverID())) {
                existing = drivers.get(i);
                index = i;
                break;
            }
        }

        if (existing == null) {
            throw new IllegalArgumentException(
                "Driver not found: " + updatedDriver.getDriverID());
        }

        // D5: driverID and name cannot be changed (they must match existing values)
        if (!existing.getDriverID().equals(updatedDriver.getDriverID())) {
            throw new IllegalArgumentException("driverID cannot be modified (D5).");
        }
        if (!existing.getName().equals(updatedDriver.getName())) {
            throw new IllegalArgumentException("Driver name cannot be modified (D5).");
        }

        // D4: If experience > 10 years, licenseType cannot be changed
        if (existing.getExperienceYears() > 10 &&
            !existing.getLicenseType().equals(updatedDriver.getLicenseType())) {
            throw new IllegalArgumentException(
                "Cannot change licenseType for driver with more than 10 years experience (D4).");
        }

        // Validate updated fields
        if (!Driver.isValidAddress(updatedDriver.getAddress())) {
            throw new IllegalArgumentException(
                "Invalid address format. Expected: StreetNo|StreetName|City|State|Country");
        }
        if (!Driver.isValidBirthdate(updatedDriver.getBirthdate())) {
            throw new IllegalArgumentException(
                "Invalid birthdate format. Expected: DD-MM-YYYY");
        }
        if (!Driver.isValidLicenseType(updatedDriver.getLicenseType())) {
            throw new IllegalArgumentException(
                "Invalid licenseType. Must be: Light, Medium, Heavy, or PublicTransport");
        }

        drivers.set(index, updatedDriver);
        writeAll(drivers);
    }

    /**
     * Retrieves a driver by their driverID.
     *
     * @param driverID The ID of the driver to find
     * @return The matching Driver object, or null if not found
     */
    public Driver retrieve(String driverID) {
        for (Driver d : readAll()) {
            if (d.getDriverID().equals(driverID)) {
                return d;
            }
        }
        return null;
    }

    /**
     * Retrieves all drivers from storage.
     *
     * @return List of all drivers
     */
    public List<Driver> retrieveAll() {
        return readAll();
    }

    /**
     * Returns the total number of drivers stored.
     *
     * @return Count of drivers
     */
    public int count() {
        return readAll().size();
    }
}

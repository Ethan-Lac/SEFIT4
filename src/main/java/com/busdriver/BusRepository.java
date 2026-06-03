package com.busdriver;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Repository class for managing Bus records.
 * All data is persisted to/from a human-readable JSON file.
 */
public class BusRepository {

    private final String filePath;
    private final Gson gson;

    /**
     * Default constructor uses "data/buses.json" as storage file.
     */
    public BusRepository() {
        this("data/buses.json");
    }

    /**
     * Constructor with custom file path for tests or separate storage.
     *
     * @param filePath path to the JSON file used for storage
     */
    public BusRepository(String filePath) {
        this.filePath = filePath;
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        ensureFileExists();
    }

    /**
     * Creates the JSON file and parent directories if they do not exist.
     */
    private void ensureFileExists() {
        File file = new File(filePath);
        File parent = file.getParentFile();
        if (parent != null) {
            parent.mkdirs();
        }
        if (!file.exists()) {
            try (Writer writer = new FileWriter(file)) {
                writer.write("[]");
            } catch (IOException e) {
                throw new RuntimeException("Cannot create bus storage file: " + filePath, e);
            }
        }
    }

    /**
     * Reads all buses from JSON storage.
     *
     * @return list of stored buses
     */
    private List<Bus> readAll() {
        try (Reader reader = new FileReader(filePath)) {
            Type listType = new TypeToken<List<Bus>>() {}.getType();
            List<Bus> buses = gson.fromJson(reader, listType);
            return buses != null ? buses : new ArrayList<>();
        } catch (IOException e) {
            throw new RuntimeException("Cannot read bus storage file: " + filePath, e);
        }
    }

    /**
     * Writes all buses to JSON storage.
     *
     * @param buses list of buses to persist
     */
    private void writeAll(List<Bus> buses) {
        try (Writer writer = new FileWriter(filePath)) {
            gson.toJson(buses, writer);
        } catch (IOException e) {
            throw new RuntimeException("Cannot write to bus storage file: " + filePath, e);
        }
    }

    /**
     * Add a new bus to the repository.
     * Enforces B1 format rules and duplicate busID prevention.
     *
     * @param bus bus to add
     * @throws IllegalArgumentException when validation fails or busID already exists
     */
    public void add(Bus bus) {
        validateBusOrThrow(bus);

        List<Bus> buses = readAll();
        for (Bus existing : buses) {
            if (existing.getBusID().equals(bus.getBusID())) {
                throw new IllegalArgumentException("Duplicate busID: " + bus.getBusID());
            }
        }

        buses.add(bus);
        writeAll(buses);
    }

    /**
     * Update an existing bus.
     * B2 is enforced: capacity cannot increase during update.
     *
     * @param updatedBus bus with updated fields
     * @throws IllegalArgumentException when validation fails, bus is missing, or capacity increases
     */
    public void update(Bus updatedBus) {
        validateBusOrThrow(updatedBus);

        List<Bus> buses = readAll();
        for (int i = 0; i < buses.size(); i++) {
            Bus existing = buses.get(i);
            if (existing.getBusID().equals(updatedBus.getBusID())) {
                if (!BusValidator.isCapacityUpdateAllowed(existing, updatedBus)) {
                    throw new IllegalArgumentException("Bus capacity cannot increase during update.");
                }
                buses.set(i, updatedBus);
                writeAll(buses);
                return;
            }
        }

        throw new IllegalArgumentException("Bus not found: " + updatedBus.getBusID());
    }

    /**
     * Retrieve a bus by busID.
     *
     * @param busID bus ID to search for
     * @return matching Bus object, or null if not found
     */
    public Bus retrieve(String busID) {
        for (Bus bus : readAll()) {
            if (bus.getBusID().equals(busID)) {
                return bus;
            }
        }
        return null;
    }

    /**
     * Retrieve all buses from storage.
     *
     * @return list of all buses
     */
    public List<Bus> retrieveAll() {
        return readAll();
    }

    /**
     * Count stored bus records.
     *
     * @return number of buses in storage
     */
    public int count() {
        return readAll().size();
    }

    /**
     * Shared bus validation used by add and update.
     *
     * @param bus bus to validate
     */
    private void validateBusOrThrow(Bus bus) {
        if (!BusValidator.isValidBus(bus)) {
            throw new IllegalArgumentException(
                "Invalid bus. busID must be 8 digits, capacity must be positive, " +
                "fuelLevel must be 0-100, and fuelType must be Diesel, Hybrid, or Electricity.");
        }
    }
}

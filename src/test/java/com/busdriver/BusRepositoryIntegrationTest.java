package com.busdriver;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for BusRepository.
 * These tests use REAL JSON files and REAL class implementations.
 */
class BusRepositoryIntegrationTest {

    private static final String TEST_FILE = "data/test_buses.json";
    private BusRepository repo;

    /**
     * Before each test: create a fresh repository backed by a real JSON file.
     */
    @BeforeEach
    void setUp() {
        new File(TEST_FILE).delete();
        repo = new BusRepository(TEST_FILE);
    }

    /**
     * After each test: clean up the test file.
     */
    @AfterEach
    void tearDown() {
        new File(TEST_FILE).delete();
    }

    /**
     * Helper method: creates a valid Bus object for integration tests.
     */
    private Bus validBus(String id) {
        return new Bus(id, 45, 80.0, "Diesel");
    }

    /**
     * IT-B1: Valid buses should be written to JSON and retrieved correctly.
     */
    @Test
    @DisplayName("IT-B1 - Valid bus is stored correctly")
    void itB1_ValidBusStoredCorrectly() throws Exception {
        Bus bus = validBus("12345678");

        repo.add(bus);

        assertEquals(1, repo.count());
        Bus retrieved = repo.retrieve("12345678");
        assertNotNull(retrieved);
        assertEquals("12345678", retrieved.getBusID());
        assertEquals(45, retrieved.getCapacity());
        assertEquals(80.0, retrieved.getFuelLevel());
        assertEquals("Diesel", retrieved.getFuelType());

        String fileContent = Files.readString(Path.of(TEST_FILE));
        assertTrue(fileContent.contains("\"busID\": \"12345678\""));
    }

    /**
     * IT-B2: Invalid buses should be rejected and not written to storage.
     */
    @Test
    @DisplayName("IT-B2 - Invalid bus is rejected")
    void itB2_InvalidBusRejected() {
        Bus badBus = new Bus("ABC12345", 45, 80.0, "Diesel");

        assertThrows(IllegalArgumentException.class, () -> repo.add(badBus));
        assertEquals(0, repo.count());
        assertNull(repo.retrieve("ABC12345"));
    }

    /**
     * IT-B3: Valid updates should persist to the JSON file.
     */
    @Test
    @DisplayName("IT-B3 - Bus update is persisted correctly")
    void itB3_UpdatePersistedCorrectly() {
        repo.add(new Bus("12345678", 50, 80.0, "Diesel"));

        Bus updated = new Bus("12345678", 40, 65.5, "Hybrid");
        repo.update(updated);

        Bus retrieved = repo.retrieve("12345678");
        assertNotNull(retrieved);
        assertEquals(40, retrieved.getCapacity());
        assertEquals(65.5, retrieved.getFuelLevel());
        assertEquals("Hybrid", retrieved.getFuelType());
        assertEquals(1, repo.count());
    }

    /**
     * IT-B4: Counts should increase on successful adds and remain unchanged on rejected adds.
     */
    @Test
    @DisplayName("IT-B4 - Bus count is updated correctly")
    void itB4_CountUpdatedCorrectly() {
        assertEquals(0, repo.count());

        repo.add(validBus("12345678"));
        assertEquals(1, repo.count());

        repo.add(new Bus("87654321", 30, 70.0, "Electricity"));
        assertEquals(2, repo.count());

        assertThrows(
            IllegalArgumentException.class,
            () -> repo.add(new Bus("BAD", 20, 50.0, "Diesel"))
        );
        assertEquals(2, repo.count());
    }

    /**
     * IT-B5: Capacity increases during update should be rejected and original data preserved.
     */
    @Test
    @DisplayName("IT-B5 - Capacity increase update is rejected")
    void itB5_CapacityIncreaseRejectedAndOriginalPreserved() {
        repo.add(new Bus("12345678", 40, 80.0, "Diesel"));

        Bus updated = new Bus("12345678", 50, 90.0, "Diesel");

        assertThrows(IllegalArgumentException.class, () -> repo.update(updated));
        Bus retrieved = repo.retrieve("12345678");
        assertEquals(40, retrieved.getCapacity());
        assertEquals(80.0, retrieved.getFuelLevel());
        assertEquals(1, repo.count());
    }
}

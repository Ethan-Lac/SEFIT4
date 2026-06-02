package com.busdriver;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import java.io.File;

/**
 * Integration tests for DriverRepository.
 * These tests use REAL JSON files and REAL class implementations.
 * Each test verifies the full flow: create → persist → retrieve/count.
 */
class DriverIntegrationTest {

    // Use a separate test file to avoid touching production data
    private static final String TEST_FILE = "data/test_drivers.json";
    private DriverRepository repo;

    /**
     * Before each test: create a fresh repository (clears old test data).
     */
    @BeforeEach
    void setUp() {
        // Delete the test file to start fresh
        new File(TEST_FILE).delete();
        repo = new DriverRepository(TEST_FILE);
    }

    /**
     * After each test: clean up the test file.
     */
    @AfterEach
    void tearDown() {
        new File(TEST_FILE).delete();
    }

    //  Helper 

    /** Creates a valid driver object for integration tests. */
    private Driver validDriver(String id) {
        return new Driver(
            id,
            "Nguyen Van A",
            5,
            "Heavy",
            "12|Main St|Hanoi|HN|Vietnam",
            "15-06-1990"
        );
    }

    // 
    // IT-D1: Valid drivers are stored correctly
    // 

    /**
     * IT-D1: Adding a valid driver should persist to JSON file
     *        and be retrievable with correct data.
     */
    @Test
    @DisplayName("IT-D1 - Valid driver is saved to JSON and retrievable")
    void itD1_ValidDriverStoredCorrectly() {
        Driver driver = validDriver("23@#abABCD");

        // Add to repository (writes to real JSON file)
        repo.add(driver);

        // Count must be 1
        assertEquals(1, repo.count(),
            "Count should be 1 after adding one valid driver");

        // Retrieve by ID and verify all fields
        Driver retrieved = repo.retrieve("23@#abABCD");
        assertNotNull(retrieved, "Retrieved driver should not be null");
        assertEquals("23@#abABCD",                 retrieved.getDriverID());
        assertEquals("Nguyen Van A",                retrieved.getName());
        assertEquals(5,                             retrieved.getExperienceYears());
        assertEquals("Heavy",                       retrieved.getLicenseType());
        assertEquals("12|Main St|Hanoi|HN|Vietnam", retrieved.getAddress());
        assertEquals("15-06-1990",                  retrieved.getBirthdate());
    }

    // 
    // IT-D2: Invalid drivers are rejected
    // 

    /**
     * IT-D2: Adding a driver with an invalid driverID should throw an exception
     *        and the count must remain 0 (nothing written to file).
     */
    @Test
    @DisplayName("IT-D2 - Invalid driverID is rejected, count stays 0")
    void itD2_InvalidDriverRejected() {
        Driver badDriver = new Driver(
            "INVALID_ID",           // D1 violation: not 10 chars, wrong format
            "Nguyen Van B",
            3,
            "Light",
            "5|Oak Ave|HCMC|HCM|Vietnam",
            "20-05-1995"
        );

        // Should throw IllegalArgumentException
        assertThrows(IllegalArgumentException.class, () -> repo.add(badDriver),
            "Adding a driver with invalid ID should throw IllegalArgumentException");

        // Count must remain 0 — invalid driver was NOT saved
        assertEquals(0, repo.count(),
            "Count should remain 0 after rejecting an invalid driver");
    }

    /**
     * IT-D2b: Adding a driver with invalid address format is also rejected.
     */
    @Test
    @DisplayName("IT-D2b - Driver with invalid address format is rejected")
    void itD2b_InvalidAddressRejected() {
        Driver badDriver = new Driver(
            "23@#abABCD",
            "Tran Van C",
            2,
            "Medium",
            "No Pipe Address",       // D2 violation
            "10-10-1992"
        );

        assertThrows(IllegalArgumentException.class, () -> repo.add(badDriver),
            "Driver with invalid address should be rejected");
        assertEquals(0, repo.count());
    }

    // 
    // IT-D3: Updates are persisted correctly
    // 

    /**
     * IT-D3: After a valid update, the new data should be in the JSON file
     *        and the count should remain the same (no extra record added).
     */
    @Test
    @DisplayName("IT-D3 - Valid update is persisted to JSON file")
    void itD3_UpdatePersistedCorrectly() {
        // First add a valid driver
        Driver driver = validDriver("23@#abABCD");
        repo.add(driver);
        assertEquals(1, repo.count());

        // Now update the address (mutable field)
        Driver updated = validDriver("23@#abABCD");
        updated.setAddress("99|New Road|HCMC|HCM|Vietnam");
        repo.update(updated);

        // Count should still be 1 (update, not add)
        assertEquals(1, repo.count(),
            "Count should stay 1 after an update");

        // Retrieve and check new address was saved to file
        Driver retrieved = repo.retrieve("23@#abABCD");
        assertEquals("99|New Road|HCMC|HCM|Vietnam", retrieved.getAddress(),
            "Updated address should be persisted in the JSON file");
    }

    /**
     * IT-D3b: Attempting to change licenseType for a driver with > 10 years
     *         experience (D4) should be rejected and file should be unchanged.
     */
    @Test
    @DisplayName("IT-D3b - D4 violation on update is rejected, file unchanged")
    void itD3b_D4ViolationOnUpdateRejected() {
        // Add a driver with 11 years experience
        Driver driver = new Driver(
            "23@#abABCD", "Nguyen Van A", 11,
            "Heavy", "12|Main St|Hanoi|HN|Vietnam", "15-06-1990"
        );
        repo.add(driver);

        // Try to change licenseType — should be rejected (D4)
        Driver updated = new Driver(
            "23@#abABCD", "Nguyen Van A", 11,
            "Light",                             // D4 violation
            "12|Main St|Hanoi|HN|Vietnam", "15-06-1990"
        );

        assertThrows(IllegalArgumentException.class, () -> repo.update(updated),
            "Changing licenseType for driver with > 10 years experience should fail");

        // Verify the original license is still in the file
        Driver retrieved = repo.retrieve("23@#abABCD");
        assertEquals("Heavy", retrieved.getLicenseType(),
            "LicenseType should remain Heavy (unchanged) in the file");
    }

    // 
    // IT-D4: Record counts are updated correctly
    // 

    /**
     * IT-D4: Count must increment for each successful add,
     *        and must NOT change when an invalid driver is rejected.
     */
    @Test
    @DisplayName("IT-D4 - Count increments correctly and stays unchanged on rejection")
    void itD4_CountUpdatedCorrectly() {
        // Start: count = 0
        assertEquals(0, repo.count(), "Initial count should be 0");

        // Add driver 1 → count = 1
        repo.add(validDriver("23@#abABCD"));
        assertEquals(1, repo.count(), "Count should be 1 after first add");

        // Add driver 2 → count = 2
        repo.add(validDriver("45@#cdEFGH"));
        assertEquals(2, repo.count(), "Count should be 2 after second add");

        // Try invalid driver → count should STAY at 2
        Driver bad = new Driver("BAD", "X", 1, "Light", "1|a|b|c|d", "01-01-2000");
        assertThrows(IllegalArgumentException.class, () -> repo.add(bad));
        assertEquals(2, repo.count(), "Count should remain 2 after rejected add");
    }
}

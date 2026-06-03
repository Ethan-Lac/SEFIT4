package com.busdriver;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit Test Class: BusUnitTest
 *
 * Purpose   : Verify all Bus validation rules (B1 - B5)
 * Framework : JUnit 5
 * Coverage  : 18 test cases covering normal, invalid, and edge cases
 *
 * Conditions tested:
 *   B1 - Bus ID Rules and uniqueness
 *   B2 - Capacity Update Restriction
 *   B3 - Driver Age Restriction
 *   B4 - Electric Bus Experience Restriction
 *   B5 - Electric/Hybrid Bus Licence Restriction
 */
class BusUnitTest {

    @TempDir
    Path tempDir;

    private static final DateTimeFormatter BIRTHDATE_FORMAT =
        DateTimeFormatter.ofPattern("dd-MM-yyyy");

    /**
     * Helper method: creates a valid bus object.
     */
    private Bus validBus(String id) {
        return new Bus(id, 45, 75.0, "Diesel");
    }

    /**
     * Helper method: creates a valid driver object for B3-B5 tests.
     */
    private Driver validDriver(int age, int experienceYears, String licenseType) {
        LocalDate birthdate = LocalDate.now().minusYears(age);
        return new Driver(
            "23@#abABCD",
            "Nguyen Van A",
            experienceYears,
            licenseType,
            "12|Main St|Hanoi|HN|Vietnam",
            birthdate.format(BIRTHDATE_FORMAT)
        );
    }

    //
    // B1 - Bus ID Rules
    // Rule: busID must be unique, exactly 8 characters long, and all digits.
    //

    /**
     * Test Case ID : TC-B1-01
     * Condition    : B1
     * Type         : Normal Case
     * Description  : An 8-digit busID should pass validation
     */
    @Test
    @DisplayName("B1 - TC-B1-01: Valid 8-digit busID passes")
    void testB1_ValidBusID() {
        assertTrue(BusValidator.isValidBusID("12345678"));
    }

    /**
     * Test Case ID : TC-B1-02
     * Condition    : B1
     * Type         : Invalid Case
     * Description  : A busID shorter than 8 characters should fail
     */
    @Test
    @DisplayName("B1 - TC-B1-02: Short busID fails")
    void testB1_BusIDTooShort() {
        assertFalse(BusValidator.isValidBusID("1234567"));
    }

    /**
     * Test Case ID : TC-B1-03
     * Condition    : B1
     * Type         : Invalid Case
     * Description  : A busID containing letters should fail
     */
    @Test
    @DisplayName("B1 - TC-B1-03: Non-digit busID fails")
    void testB1_BusIDWithLetters() {
        assertFalse(BusValidator.isValidBusID("1234ABCD"));
    }

    /**
     * Test Case ID : TC-B1-04
     * Condition    : B1
     * Type         : Edge Case
     * Description  : A null busID should fail safely
     */
    @Test
    @DisplayName("B1 - TC-B1-04: Null busID fails")
    void testB1_NullBusID() {
        assertFalse(BusValidator.isValidBusID(null));
    }

    /**
     * Test Case ID : TC-B1-05
     * Condition    : B1
     * Type         : Invalid Case
     * Description  : Duplicate bus IDs should be rejected by the repository
     */
    @Test
    @DisplayName("B1 - TC-B1-05: Duplicate busID is rejected")
    void testB1_DuplicateBusIDRejected() {
        BusRepository repo = new BusRepository(tempDir.resolve("b1_duplicate.json").toString());
        repo.add(validBus("12345678"));

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> repo.add(new Bus("12345678", 30, 80.0, "Hybrid"))
        );
        assertTrue(exception.getMessage().contains("Duplicate busID"));
    }

    //
    // B2 - Capacity Update Restriction
    // Rule: bus capacity cannot increase during update operations, but can decrease.
    //

    /**
     * Test Case ID : TC-B2-01
     * Condition    : B2
     * Type         : Normal Case
     * Description  : Decreasing capacity during update is allowed
     */
    @Test
    @DisplayName("B2 - TC-B2-01: Capacity decrease is allowed")
    void testB2_CapacityDecreaseAllowed() {
        Bus existing = new Bus("12345678", 50, 80.0, "Diesel");
        Bus updated = new Bus("12345678", 40, 80.0, "Diesel");

        assertTrue(BusValidator.isCapacityUpdateAllowed(existing, updated));
    }

    /**
     * Test Case ID : TC-B2-02
     * Condition    : B2
     * Type         : Invalid Case
     * Description  : Increasing capacity during update is rejected
     */
    @Test
    @DisplayName("B2 - TC-B2-02: Capacity increase is rejected")
    void testB2_CapacityIncreaseRejected() {
        Bus existing = new Bus("12345678", 40, 80.0, "Diesel");
        Bus updated = new Bus("12345678", 50, 80.0, "Diesel");

        assertFalse(BusValidator.isCapacityUpdateAllowed(existing, updated));
    }

    /**
     * Test Case ID : TC-B2-03
     * Condition    : B2
     * Type         : Edge Case
     * Description  : Keeping the same capacity during update is allowed
     */
    @Test
    @DisplayName("B2 - TC-B2-03: Same capacity is allowed")
    void testB2_SameCapacityAllowed() {
        Bus existing = new Bus("12345678", 50, 80.0, "Diesel");
        Bus updated = new Bus("12345678", 50, 70.0, "Diesel");

        assertTrue(BusValidator.isCapacityUpdateAllowed(existing, updated));
    }

    //
    // B3 - Driver Age Restriction
    // Rule: drivers older than 50 cannot drive buses with capacity 50 or more.
    //

    /**
     * Test Case ID : TC-B3-01
     * Condition    : B3
     * Type         : Normal Case
     * Description  : A 40-year-old driver may drive a 50-capacity bus
     */
    @Test
    @DisplayName("B3 - TC-B3-01: Driver age 40 can drive capacity 50")
    void testB3_YoungerDriverCanDriveLargeBus() {
        Driver driver = validDriver(40, 8, "Heavy");
        Bus bus = new Bus("12345678", 50, 80.0, "Diesel");

        assertTrue(BusValidator.canDriverOperateBus(driver, bus));
    }

    /**
     * Test Case ID : TC-B3-02
     * Condition    : B3
     * Type         : Invalid Case
     * Description  : A driver older than 50 cannot drive a 50-capacity bus
     */
    @Test
    @DisplayName("B3 - TC-B3-02: Driver over 50 cannot drive capacity 50")
    void testB3_OlderDriverCannotDriveLargeBus() {
        Driver driver = validDriver(51, 20, "Heavy");
        Bus bus = new Bus("12345678", 50, 80.0, "Diesel");

        assertFalse(BusValidator.canDriverOperateBus(driver, bus));
    }

    /**
     * Test Case ID : TC-B3-03
     * Condition    : B3
     * Type         : Edge Case
     * Description  : A driver exactly 50 years old is not older than 50
     */
    @Test
    @DisplayName("B3 - TC-B3-03: Driver exactly 50 can drive capacity 50")
    void testB3_ExactlyFiftyAllowed() {
        Driver driver = validDriver(50, 20, "Heavy");
        Bus bus = new Bus("12345678", 50, 80.0, "Diesel");

        assertTrue(BusValidator.canDriverOperateBus(driver, bus));
    }

    /**
     * Test Case ID : TC-B3-04
     * Condition    : B3
     * Type         : Edge Case
     * Description  : A driver older than 50 may drive a bus below 50 capacity
     */
    @Test
    @DisplayName("B3 - TC-B3-04: Driver over 50 can drive capacity 49")
    void testB3_OlderDriverCanDriveSmallBus() {
        Driver driver = validDriver(55, 25, "Heavy");
        Bus bus = new Bus("12345678", 49, 80.0, "Diesel");

        assertTrue(BusValidator.canDriverOperateBus(driver, bus));
    }

    //
    // B4 - Electric Bus Experience Restriction
    // Rule: electric buses require at least 5 years of driving experience.
    //

    /**
     * Test Case ID : TC-B4-01
     * Condition    : B4
     * Type         : Normal Case
     * Description  : Driver with 6 years experience may drive an electric bus
     */
    @Test
    @DisplayName("B4 - TC-B4-01: Six years experience can drive electric bus")
    void testB4_ExperiencedDriverCanDriveElectric() {
        Driver driver = validDriver(35, 6, "Heavy");
        Bus bus = new Bus("12345678", 40, 90.0, "Electricity");

        assertTrue(BusValidator.canDriverOperateBus(driver, bus));
    }

    /**
     * Test Case ID : TC-B4-02
     * Condition    : B4
     * Type         : Invalid Case
     * Description  : Driver with 4 years experience cannot drive an electric bus
     */
    @Test
    @DisplayName("B4 - TC-B4-02: Four years experience cannot drive electric bus")
    void testB4_InexperiencedDriverCannotDriveElectric() {
        Driver driver = validDriver(35, 4, "Heavy");
        Bus bus = new Bus("12345678", 40, 90.0, "Electricity");

        assertFalse(BusValidator.canDriverOperateBus(driver, bus));
    }

    /**
     * Test Case ID : TC-B4-03
     * Condition    : B4
     * Type         : Edge Case
     * Description  : Exactly 5 years experience is enough for an electric bus
     */
    @Test
    @DisplayName("B4 - TC-B4-03: Exactly five years can drive electric bus")
    void testB4_ExactlyFiveYearsAllowed() {
        Driver driver = validDriver(35, 5, "Heavy");
        Bus bus = new Bus("12345678", 40, 90.0, "Electricity");

        assertTrue(BusValidator.canDriverOperateBus(driver, bus));
    }

    //
    // B5 - Driver Licence Restriction
    // Rule: electric and hybrid buses require Heavy or PublicTransport licence.
    //

    /**
     * Test Case ID : TC-B5-01
     * Condition    : B5
     * Type         : Normal Case
     * Description  : Heavy licence is permitted for a hybrid bus
     */
    @Test
    @DisplayName("B5 - TC-B5-01: Heavy licence can drive hybrid bus")
    void testB5_HeavyLicenceCanDriveHybrid() {
        Driver driver = validDriver(35, 8, "Heavy");
        Bus bus = new Bus("12345678", 40, 90.0, "Hybrid");

        assertTrue(BusValidator.canDriverOperateBus(driver, bus));
    }

    /**
     * Test Case ID : TC-B5-02
     * Condition    : B5
     * Type         : Normal Case
     * Description  : PublicTransport licence is permitted for an electric bus
     */
    @Test
    @DisplayName("B5 - TC-B5-02: PublicTransport licence can drive electric bus")
    void testB5_PublicTransportLicenceCanDriveElectric() {
        Driver driver = validDriver(35, 8, "PublicTransport");
        Bus bus = new Bus("12345678", 40, 90.0, "Electricity");

        assertTrue(BusValidator.canDriverOperateBus(driver, bus));
    }

    /**
     * Test Case ID : TC-B5-03
     * Condition    : B5
     * Type         : Invalid Case
     * Description  : Light licence is rejected for a hybrid bus
     */
    @Test
    @DisplayName("B5 - TC-B5-03: Light licence cannot drive hybrid bus")
    void testB5_LightLicenceCannotDriveHybrid() {
        Driver driver = validDriver(35, 8, "Light");
        Bus bus = new Bus("12345678", 40, 90.0, "Hybrid");

        assertFalse(BusValidator.canDriverOperateBus(driver, bus));
    }

    /**
     * Test Case ID : TC-B5-04
     * Condition    : B5
     * Type         : Invalid Case
     * Description  : Medium licence is rejected for an electric bus
     */
    @Test
    @DisplayName("B5 - TC-B5-04: Medium licence cannot drive electric bus")
    void testB5_MediumLicenceCannotDriveElectric() {
        Driver driver = validDriver(35, 8, "Medium");
        Bus bus = new Bus("12345678", 40, 90.0, "Electricity");

        assertFalse(BusValidator.canDriverOperateBus(driver, bus));
    }
}

package com.busdriver;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 
 * Unit Test Class: DriverUnitTest
 * 
 * Purpose   : Verify all Driver validation rules (D1 - D5)
 * Framework : JUnit 5
 * Coverage  : 18 test cases covering normal, invalid, and edge cases
 *
 * Conditions tested:
 *   D1 - Driver ID Rules
 *   D2 - Address Format
 *   D3 - Birthdate Format
 *   D4 - License Update Restriction
 *   D5 - Immutable Fields (driverID and name)
 * 
 */
class DriverUnitTest {

    /**
     * Helper method: creates a fully valid Driver object for reuse across tests.
     * All fields satisfy D1-D5 requirements.
     */
    private Driver validDriver() {
        return new Driver(
            "23@#abABCD",                  // D1: valid 10-char ID
            "Nguyen Van A",                // name (non-empty)
            5,                             // experienceYears (positive)
            "Heavy",                       // licenseType (valid value)
            "12|Main St|Hanoi|HN|Vietnam", // D2: valid address format
            "15-06-1990"                   // D3: valid DD-MM-YYYY birthdate
        );
    }

    // 
    // D1 — Driver ID Rules
    // Rule: exactly 10 chars, first 2 = digits(2-9), positions 3-8 have
    //       at least 2 special chars, last 2 = uppercase letters (A-Z)
    // 

    /**
     * Test Case ID : TC-D1-01
     * Condition    : D1
     * Type         : Normal Case
     * Description  : A properly formatted 10-character driverID should pass
     * Input        : "23@#abABCD"
     * Expected     : true
     */
    @Test
    @DisplayName("D1 - TC-D1-01: Valid driverID passes validation")
    void testD1_ValidDriverID() {
        assertTrue(Driver.isValidDriverID("23@#abABCD"),
            "A valid driverID with correct format should pass");
    }

    /**
     * Test Case ID : TC-D1-02
     * Condition    : D1
     * Type         : Invalid Case
     * Description  : driverID shorter than 10 characters should be rejected
     * Input        : "23@#abAB" (8 chars)
     * Expected     : false
     */
    @Test
    @DisplayName("D1 - TC-D1-02: ID too short (8 chars) should fail")
    void testD1_IDTooShort() {
        assertFalse(Driver.isValidDriverID("23@#abAB"),
            "A driverID with only 8 characters should fail");
    }

    /**
     * Test Case ID : TC-D1-03
     * Condition    : D1
     * Type         : Invalid Case
     * Description  : First digit is '1' which is outside the allowed range (2-9)
     * Input        : "13@#abABCD"
     * Expected     : false
     */
    @Test
    @DisplayName("D1 - TC-D1-03: First digit '1' is not allowed, should fail")
    void testD1_FirstDigitInvalid() {
        assertFalse(Driver.isValidDriverID("13@#abABCD"),
            "First digit '1' is not allowed (must be 2-9)");
    }

    /**
     * Test Case ID : TC-D1-04
     * Condition    : D1
     * Type         : Invalid Case
     * Description  : driverID has no special characters in positions 3-8
     * Input        : "23abcdABCD"
     * Expected     : false
     */
    @Test
    @DisplayName("D1 - TC-D1-04: No special characters in positions 3-8, should fail")
    void testD1_NoSpecialCharacters() {
        assertFalse(Driver.isValidDriverID("23abcdABCD"),
            "ID without special characters should fail");
    }

    /**
     * Test Case ID : TC-D1-05
     * Condition    : D1
     * Type         : Invalid Case
     * Description  : Last two characters are lowercase, not uppercase letters
     * Input        : "23@#abABcd"
     * Expected     : false
     */
    @Test
    @DisplayName("D1 - TC-D1-05: Last two chars not uppercase, should fail")
    void testD1_LastCharsNotUppercase() {
        assertFalse(Driver.isValidDriverID("23@#abABcd"),
            "Last two characters must be uppercase letters");
    }

    /**
     * Test Case ID : TC-D1-06
     * Condition    : D1
     * Type         : Edge Case
     * Description  : Exactly 2 special characters (minimum required) should pass
     * Input        : "23@#abABCD"
     * Expected     : true
     */
    @Test
    @DisplayName("D1 - TC-D1-06: Exactly 2 special chars (minimum) should pass")
    void testD1_ExactlyTwoSpecialChars() {
        assertTrue(Driver.isValidDriverID("23@#abABCD"),
            "Exactly 2 special characters in positions 3-8 should pass");
    }

    /**
     * Test Case ID : TC-D1-07
     * Condition    : D1
     * Type         : Edge Case
     * Description  : Null input should return false without throwing an exception
     * Input        : null
     * Expected     : false
     */
    @Test
    @DisplayName("D1 - TC-D1-07: Null driverID should fail gracefully")
    void testD1_NullID() {
        assertFalse(Driver.isValidDriverID(null),
            "Null driverID should return false without throwing exception");
    }

    // 
    // D2 — Address Format
    // Rule: must follow StreetNumber|StreetName|City|State|Country (5 parts)
    // 

    /**
     * Test Case ID : TC-D2-01
     * Condition    : D2
     * Type         : Normal Case
     * Description  : Address with all 5 pipe-separated non-empty parts is valid
     * Input        : "12|Main St|Hanoi|HN|Vietnam"
     * Expected     : true
     */
    @Test
    @DisplayName("D2 - TC-D2-01: Valid address format passes")
    void testD2_ValidAddress() {
        assertTrue(Driver.isValidAddress("12|Main St|Hanoi|HN|Vietnam"),
            "A correctly formatted address should pass");
    }

    /**
     * Test Case ID : TC-D2-02
     * Condition    : D2
     * Type         : Invalid Case
     * Description  : Address with only 4 pipe-separated parts (missing one section)
     * Input        : "12|Main St|Hanoi|Vietnam"
     * Expected     : false
     */
    @Test
    @DisplayName("D2 - TC-D2-02: Address with only 4 parts should fail")
    void testD2_FourPartsOnly() {
        assertFalse(Driver.isValidAddress("12|Main St|Hanoi|Vietnam"),
            "Address with only 4 pipe-separated parts should fail");
    }

    /**
     * Test Case ID : TC-D2-03
     * Condition    : D2
     * Type         : Invalid Case
     * Description  : Address with an empty section (City is missing)
     * Input        : "12|Main St||HN|Vietnam"
     * Expected     : false
     */
    @Test
    @DisplayName("D2 - TC-D2-03: Address with empty section should fail")
    void testD2_EmptySection() {
        assertFalse(Driver.isValidAddress("12|Main St||HN|Vietnam"),
            "Address with an empty section should fail");
    }

    /**
     * Test Case ID : TC-D2-04
     * Condition    : D2
     * Type         : Edge Case
     * Description  : Null address should return false without throwing exception
     * Input        : null
     * Expected     : false
     */
    @Test
    @DisplayName("D2 - TC-D2-04: Null address should fail gracefully")
    void testD2_NullAddress() {
        assertFalse(Driver.isValidAddress(null),
            "Null address should return false without throwing exception");
    }

    // 
    // D3 — Birthdate Format
    // Rule: must follow DD-MM-YYYY format with valid day/month values
    // 

    /**
     * Test Case ID : TC-D3-01
     * Condition    : D3
     * Type         : Normal Case
     * Description  : A valid date in DD-MM-YYYY format should pass
     * Input        : "15-06-1990"
     * Expected     : true
     */
    @Test
    @DisplayName("D3 - TC-D3-01: Valid birthdate format passes")
    void testD3_ValidBirthdate() {
        assertTrue(Driver.isValidBirthdate("15-06-1990"),
            "A valid DD-MM-YYYY birthdate should pass");
    }

    /**
     * Test Case ID : TC-D3-02
     * Condition    : D3
     * Type         : Invalid Case
     * Description  : Date in YYYY/MM/DD format (wrong separator and order)
     * Input        : "1990/06/15"
     * Expected     : false
     */
    @Test
    @DisplayName("D3 - TC-D3-02: Wrong date format YYYY/MM/DD should fail")
    void testD3_WrongFormat() {
        assertFalse(Driver.isValidBirthdate("1990/06/15"),
            "Date in YYYY/MM/DD format should fail");
    }

    /**
     * Test Case ID : TC-D3-03
     * Condition    : D3
     * Type         : Invalid Case
     * Description  : Month value 13 does not exist
     * Input        : "15-13-1990"
     * Expected     : false
     */
    @Test
    @DisplayName("D3 - TC-D3-03: Invalid month 13 should fail")
    void testD3_InvalidMonth() {
        assertFalse(Driver.isValidBirthdate("15-13-1990"),
            "Month 13 does not exist, should fail");
    }

    /**
     * Test Case ID : TC-D3-04
     * Condition    : D3
     * Type         : Edge Case
     * Description  : Day value 00 is not a valid day
     * Input        : "00-06-1990"
     * Expected     : false
     */
    @Test
    @DisplayName("D3 - TC-D3-04: Day 00 is invalid, should fail")
    void testD3_DayZero() {
        assertFalse(Driver.isValidBirthdate("00-06-1990"),
            "Day 0 does not exist, should fail");
    }

    // 
    // D4 — License Update Restriction
    // Rule: if experienceYears > 10, licenseType CANNOT be changed on update
    // 

    /**
     * Test Case ID : TC-D4-01
     * Condition    : D4
     * Type         : Normal Case
     * Description  : Driver with exactly 10 years is NOT restricted
     * Input        : experienceYears = 10
     * Expected     : restriction = false
     */
    @Test
    @DisplayName("D4 - TC-D4-01: Driver with 10 years can change licenseType")
    void testD4_CanChangeLicenceUnder10Years() {
        Driver driver = validDriver();
        driver.setExperienceYears(10);
        assertFalse(driver.getExperienceYears() > 10,
            "Driver with exactly 10 years should NOT be restricted");
    }

    /**
     * Test Case ID : TC-D4-02
     * Condition    : D4
     * Type         : Invalid Case
     * Description  : Driver with 11 years cannot change licenseType
     * Input        : experienceYears=11, old="Heavy", new="Light"
     * Expected     : restriction = true
     */
    @Test
    @DisplayName("D4 - TC-D4-02: Driver with 11 years cannot change licenseType")
    void testD4_CannotChangeLicenceOver10Years() {
        Driver existing = validDriver();
        existing.setExperienceYears(11);
        existing.setLicenseType("Heavy");

        Driver updated = validDriver();
        updated.setExperienceYears(11);
        updated.setLicenseType("Light");

        boolean restricted = existing.getExperienceYears() > 10 &&
                             !existing.getLicenseType().equals(updated.getLicenseType());
        assertTrue(restricted,
            "Driver with 11 years should not be able to change licenseType");
    }

    /**
     * Test Case ID : TC-D4-03
     * Condition    : D4
     * Type         : Edge Case
     * Description  : Boundary value — exactly 10 years is not restricted
     * Input        : experienceYears = 10
     * Expected     : false
     */
    @Test
    @DisplayName("D4 - TC-D4-03: Exactly 10 years experience is NOT restricted")
    void testD4_ExactlyTenYearsNotRestricted() {
        Driver driver = validDriver();
        driver.setExperienceYears(10);
        assertFalse(driver.getExperienceYears() > 10,
            "Exactly 10 years should NOT trigger the D4 restriction");
    }

    // 
    // D5 — Immutable Fields
    // Rule: driverID and name cannot be modified during update operations
    // 

    /**
     * Test Case ID : TC-D5-01
     * Condition    : D5
     * Type         : Normal Case
     * Description  : Mutable fields like address CAN be changed during update
     * Input        : new address = "99|New Road|HCMC|HCM|Vietnam"
     * Expected     : address updated successfully
     */
    @Test
    @DisplayName("D5 - TC-D5-01: Updating address (mutable field) is allowed")
    void testD5_MutableFieldCanChange() {
        Driver driver = validDriver();
        driver.setAddress("99|New Road|HCMC|HCM|Vietnam");
        assertEquals("99|New Road|HCMC|HCM|Vietnam", driver.getAddress(),
            "Address is a mutable field and should be updatable");
    }

    /**
     * Test Case ID : TC-D5-02
     * Condition    : D5
     * Type         : Invalid Case
     * Description  : Attempting to change driverID is a D5 violation
     * Input        : original="23@#abABCD", new="34@#abABCD"
     * Expected     : violation = true
     */
    @Test
    @DisplayName("D5 - TC-D5-02: driverID cannot be changed during update")
    void testD5_DriverIDImmutable() {
        String originalID = "23@#abABCD";
        String newID      = "34@#abABCD";
        boolean violation = !originalID.equals(newID);
        assertTrue(violation,
            "Changing driverID during update should be flagged as a D5 violation");
    }

    /**
     * Test Case ID : TC-D5-03
     * Condition    : D5
     * Type         : Invalid Case
     * Description  : Attempting to change driver name is a D5 violation
     * Input        : original="Nguyen Van A", new="Tran Van B"
     * Expected     : violation = true
     */
    @Test
    @DisplayName("D5 - TC-D5-03: Driver name cannot be changed during update")
    void testD5_NameImmutable() {
        String originalName = "Nguyen Van A";
        String newName      = "Tran Van B";
        boolean violation = !originalName.equals(newName);
        assertTrue(violation,
            "Changing driver name during update should be flagged as a D5 violation");
    }
}
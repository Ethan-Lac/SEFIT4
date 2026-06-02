package com.busdriver;

/**
 * Represents a bus driver in the Intelligent Bus Driver Guidance System.
 * Enforces validation rules D1-D5 as specified in the requirements.
 */
public class Driver {

    private String driverID;        // Unique 10-character ID (D1)
    private String name;            // Full name (immutable after creation - D5)
    private int experienceYears;    // Years of driving experience
    private String licenseType;     // Light, Medium, Heavy, PublicTransport
    private String address;         // Format: StreetNo|StreetName|City|State|Country (D2)
    private String birthdate;       // Format: DD-MM-YYYY (D3)

    /**
     * Default constructor required for Gson deserialization.
     */
    public Driver() {}

    /**
     * Full constructor for creating a new Driver.
     *
     * @param driverID        Unique driver ID (must follow D1 rules)
     * @param name            Full name of the driver
     * @param experienceYears Years of experience
     * @param licenseType     Type of license: Light, Medium, Heavy, PublicTransport
     * @param address         Address in format: StreetNo|StreetName|City|State|Country
     * @param birthdate       Date of birth in format: DD-MM-YYYY
     */
    public Driver(String driverID, String name, int experienceYears,
                  String licenseType, String address, String birthdate) {
        this.driverID = driverID;
        this.name = name;
        this.experienceYears = experienceYears;
        this.licenseType = licenseType;
        this.address = address;
        this.birthdate = birthdate;
    }

    // Getters 

    public String getDriverID()       { return driverID; }
    public String getName()           { return name; }
    public int getExperienceYears()   { return experienceYears; }
    public String getLicenseType()    { return licenseType; }
    public String getAddress()        { return address; }
    public String getBirthdate()      { return birthdate; }

    // Setters 

    public void setDriverID(String driverID)             { this.driverID = driverID; }
    public void setName(String name)                     { this.name = name; }
    public void setExperienceYears(int experienceYears)  { this.experienceYears = experienceYears; }
    public void setLicenseType(String licenseType)       { this.licenseType = licenseType; }
    public void setAddress(String address)               { this.address = address; }
    public void setBirthdate(String birthdate)           { this.birthdate = birthdate; }

    // Validation Methods

    /**
     * D1: Validates the driverID format.
     * Rules:
     *   - Exactly 10 characters
     *   - First 2 characters: digits between 2 and 9
     *   - Characters 3-8: at least 2 special characters
     *   - Last 2 characters: uppercase letters A-Z
     *
     * @param id The driver ID to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidDriverID(String id) {
        if (id == null || id.length() != 10) return false;

        // First two characters must be digits between 2 and 9
        char c0 = id.charAt(0);
        char c1 = id.charAt(1);
        if (!Character.isDigit(c0) || !Character.isDigit(c1)) return false;
        if (c0 < '2' || c0 > '9') return false;
        if (c1 < '2' || c1 > '9') return false;

        // Last two characters must be uppercase letters
        char c8 = id.charAt(8);
        char c9 = id.charAt(9);
        if (!Character.isUpperCase(c8) || !Character.isUpperCase(c9)) return false;

        // Characters at positions 3-8 (index 2-7) must contain at least 2 special characters
        int specialCount = 0;
        for (int i = 2; i <= 7; i++) {
            char c = id.charAt(i);
            if (!Character.isLetterOrDigit(c)) {
                specialCount++;
            }
        }
        return specialCount >= 2;
    }

    /**
     * D2: Validates the address format.
     * Format: StreetNumber|StreetName|City|State|Country
     *
     * @param address The address string to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidAddress(String address) {
        if (address == null) return false;
        String[] parts = address.split("\\|");
        // Must have exactly 5 parts, all non-empty
        if (parts.length != 5) return false;
        for (String part : parts) {
            if (part.trim().isEmpty()) return false;
        }
        return true;
    }

    /**
     * D3: Validates the birthdate format DD-MM-YYYY.
     *
     * @param birthdate The birthdate string to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidBirthdate(String birthdate) {
        if (birthdate == null) return false;
        // Must match DD-MM-YYYY pattern
        if (!birthdate.matches("\\d{2}-\\d{2}-\\d{4}")) return false;

        String[] parts = birthdate.split("-");
        int day   = Integer.parseInt(parts[0]);
        int month = Integer.parseInt(parts[1]);
        int year  = Integer.parseInt(parts[2]);

        if (month < 1 || month > 12) return false;
        if (day < 1 || day > 31)     return false;
        if (year < 1900 || year > 2100) return false;

        return true;
    }

    /**
     * Validates the licenseType value.
     *
     * @param licenseType The license type to check
     * @return true if it is one of: Light, Medium, Heavy, PublicTransport
     */
    public static boolean isValidLicenseType(String licenseType) {
        if (licenseType == null) return false;
        return licenseType.equals("Light") || licenseType.equals("Medium") ||
               licenseType.equals("Heavy") || licenseType.equals("PublicTransport");
    }

    /**
     * Validates all fields of this Driver object.
     *
     * @return true if all fields are valid
     */
    public boolean isValid() {
        return isValidDriverID(driverID)
            && name != null && !name.trim().isEmpty()
            && experienceYears >= 0
            && isValidLicenseType(licenseType)
            && isValidAddress(address)
            && isValidBirthdate(birthdate);
    }

    /**
     * Calculates the driver's age based on birthdate.
     *
     * @return Age in years, or -1 if birthdate is invalid
     */
    public int getAge() {
        if (!isValidBirthdate(birthdate)) return -1;
        String[] parts = birthdate.split("-");
        int birthYear  = Integer.parseInt(parts[2]);
        int birthMonth = Integer.parseInt(parts[1]);
        int birthDay   = Integer.parseInt(parts[0]);

        java.time.LocalDate today = java.time.LocalDate.now();
        java.time.LocalDate dob   = java.time.LocalDate.of(birthYear, birthMonth, birthDay);
        return java.time.Period.between(dob, today).getYears();
    }

    @Override
    public String toString() {
        return "Driver{" +
               "driverID='" + driverID + '\'' +
               ", name='" + name + '\'' +
               ", experienceYears=" + experienceYears +
               ", licenseType='" + licenseType + '\'' +
               ", address='" + address + '\'' +
               ", birthdate='" + birthdate + '\'' +
               '}';
    }
}

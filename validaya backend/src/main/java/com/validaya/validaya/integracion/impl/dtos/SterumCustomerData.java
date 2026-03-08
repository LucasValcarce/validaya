package com.validaya.validaya.integracion.impl.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

/**
 * Customer data for Stereum charge creation
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SterumCustomerData {

    /**
     * Customer first name
     */
    private String name;

    /**
     * Customer last name
     */
    private String lastname;

    /**
     * Document number (ID, passport, etc.)
     */
    @JsonProperty("document_number")
    private String documentNumber;

    /**
     * Customer email
     */
    private String email;

    /**
     * Customer phone
     */
    private String phone;

    /**
     * Street address
     */
    private String address;

    /**
     * City
     */
    private String city;

    /**
     * Country code
     */
    private String country;

    /**
     * State/Province
     */
    private String state;

    /**
     * Postal code
     */
    @JsonProperty("zip_code")
    private String zipCode;
}

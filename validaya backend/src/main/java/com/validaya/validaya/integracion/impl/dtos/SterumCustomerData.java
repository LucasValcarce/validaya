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
    private String name;
    private String lastname;
    @JsonProperty("document_number")
    private String documentNumber;
    private String email;
    private String phone;
    private String address;
    private String city = "Santa Cruz de la Sierra";;
    private String country = "BO";
    private String state = "Santa Cruz";
    @JsonProperty("zip_code")
    private String zipCode = "0000";
}

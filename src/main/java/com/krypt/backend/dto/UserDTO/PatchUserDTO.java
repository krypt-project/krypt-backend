package com.krypt.backend.dto.UserDTO;

import jakarta.validation.constraints.Size;

public class PatchUserDTO {
    @Size(max = 50, message = "Last name must be less than 50 characters")
    private String lastName;

    @Size(max = 50, message = "First name must be less than 50 characters")
    private String firstName;

    public @Size(max = 50, message = "Last name must be less than 50 characters") String getLastName() {
        return lastName;
    }

    public void setLastName(@Size(max = 50, message = "Last name must be less than 50 characters") String lastName) {
        this.lastName = lastName;
    }

    public @Size(max = 50, message = "First name must be less than 50 characters") String getFirstName() {
        return firstName;
    }

    public void setFirstName(@Size(max = 50, message = "First name must be less than 50 characters") String firstName) {
        this.firstName = firstName;
    }
}

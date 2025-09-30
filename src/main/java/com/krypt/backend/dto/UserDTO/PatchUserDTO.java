package com.krypt.backend.dto.UserDTO;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

public class PatchUserDTO {
    @Getter
    @Setter
    @Size(max = 50, message = "Last name must be less than 50 characters")
    private String lastName;

    @Getter
    @Setter
    @Size(max = 50, message = "First name must be less than 50 characters")
    private String firstName;

}

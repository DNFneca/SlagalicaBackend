package org.civlabs.civlabsschedulebackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
public class UserDTO {
    private Long id;
    private String email;
    private String username;
    private Boolean active;
    private Boolean ready;
}

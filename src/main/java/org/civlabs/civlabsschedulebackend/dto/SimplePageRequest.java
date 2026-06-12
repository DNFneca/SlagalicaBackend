package org.civlabs.civlabsschedulebackend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SimplePageRequest {
    private int page = 0;
    private int size = 10;
}
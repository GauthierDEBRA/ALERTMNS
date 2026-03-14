package com.alertmns.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfilePreferencesRequest {

    private Boolean notifyReunions;
    private Boolean notifyMessages;
    private Boolean notifyAbsences;
}

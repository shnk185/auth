package org.ngs.auth.dto;

import lombok.Data;

@Data
public abstract class BaseRequest {
    private String uuid;
    private String apiKey;
    private String bearer;
}

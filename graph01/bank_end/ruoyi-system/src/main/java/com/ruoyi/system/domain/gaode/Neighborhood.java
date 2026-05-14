package com.ruoyi.system.domain.gaode;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 社区信息
 */
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Neighborhood {
    @JsonDeserialize(using = EmptyArrayAsNullStringDeserializer.class)
    private String name;

    @JsonDeserialize(using = EmptyArrayAsNullStringDeserializer.class)
    private String type;
}

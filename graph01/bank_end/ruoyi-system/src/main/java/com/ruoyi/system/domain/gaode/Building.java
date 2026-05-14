package com.ruoyi.system.domain.gaode;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 楼信息
 */
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Building {
    @JsonDeserialize(using = EmptyArrayAsNullStringDeserializer.class)
    private String name;

    @JsonDeserialize(using = EmptyArrayAsNullStringDeserializer.class)
    private String type;
}

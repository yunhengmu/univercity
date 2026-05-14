package com.ruoyi.system.domain.gaode;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 门牌信息
 */
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class StreetNumber {
    @JsonDeserialize(using = EmptyArrayAsNullStringDeserializer.class)
    private String street;

    @JsonDeserialize(using = EmptyArrayAsNullStringDeserializer.class)
    private String number;

    @JsonDeserialize(using = EmptyArrayAsNullStringDeserializer.class)
    private String location;

    @JsonDeserialize(using = EmptyArrayAsNullStringDeserializer.class)
    private String direction;

    @JsonDeserialize(using = EmptyArrayAsNullStringDeserializer.class)
    private String distance;
}

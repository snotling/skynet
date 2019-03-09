package org.snoth.skynet.bean;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class Column {

    private String column;
    private String type;
    private Integer maxLength;
    private String defaultValue;
    private boolean nullable;
    private String comment;

}

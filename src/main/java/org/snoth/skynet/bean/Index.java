package org.snoth.skynet.bean;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class Index {

    private String name;
    private String tablespace;
    private String definition;

}

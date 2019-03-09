package org.snoth.skynet.bean;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
public class Constraint {

    private String name;
    private String columnName;
    private String type;
    private String foreignTableName;
    private String foreignColumnName;
    private String condition;

    public Constraint(String name, String type) {
        this.name = name;
        this.type = type;
    }

    public boolean isCheck() {
        return "CHECK".equals(type);
    }

    public boolean isForeignKey() {
        return "FOREIGN KEY".equals(type);
    }

}

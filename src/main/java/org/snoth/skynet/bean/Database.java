package org.snoth.skynet.bean;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Database {

    private String serverName;
    private String dbName;
    private Integer port;
    private String user;
    private String password;
    private String schema;

}

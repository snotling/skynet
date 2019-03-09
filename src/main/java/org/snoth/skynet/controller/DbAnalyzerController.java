package org.snoth.skynet.controller;


import lombok.extern.slf4j.Slf4j;
import org.snoth.skynet.bean.CompareDb;
import org.snoth.skynet.bean.CompareDbResult;
import org.snoth.skynet.bean.Database;
import org.snoth.skynet.bean.Table;
import org.snoth.skynet.service.DbService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.origin.SystemEnvironmentOrigin;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
public class DbAnalyzerController {

    private final DbService postgreService;

    @Autowired
    public DbAnalyzerController(@Qualifier("postgre") DbService postgreService) {
        this.postgreService = postgreService;
    }

    /*
    http://localhost:12000/api/v1/db/info?serverName=localhost&dbName=presentation&port=5432&user=postgres&password=Azerty01&schema=gma
     */
    @GetMapping(value="/api/v1/db/info")
    public List<Table> getDatabaseInfo(
            @RequestParam(value = "serverName") String serverName,
            @RequestParam(value = "dbName") String dbName,
            @RequestParam(value = "port") Integer port,
            @RequestParam(value = "user") String user,
            @RequestParam(value = "password") String password,
            @RequestParam(value = "schema") String schema
    ) {
        final long t = System.currentTimeMillis();
        try {
            return postgreService.loadDatabase(new Database(serverName, dbName, port, user, password, schema));
        } finally {
            log.info("Get database information in {}ms", (System.currentTimeMillis() - t));
        }
    }

    @PostMapping(value="/api/v1/db/compare")
    @ResponseBody
    public CompareDbResult compareDatabase(@RequestBody CompareDb compareDb) {
        final long t = System.currentTimeMillis();
        try {
            return postgreService.compare(compareDb.getOne(), compareDb.getTwo());
        } finally {
            log.info("Compare database in {}ms", (System.currentTimeMillis() - t));
        }
    }

    /*
http://localhost:12000/api/v1/db/test?serverName=localhost&dbName=presentation&port=5432&user=postgres&password=Azerty01&schema=gma
 */
    @GetMapping(value="/api/v1/db/test")
    public CompareDbResult compareTest(
            @RequestParam(value = "serverName") String serverName,
            @RequestParam(value = "dbName") String dbName,
            @RequestParam(value = "port") Integer port,
            @RequestParam(value = "user") String user,
            @RequestParam(value = "password") String password,
            @RequestParam(value = "schema") String schema
    ) {
            return this.compareDatabase(new CompareDb(new Database(serverName, dbName, port, user, password, schema),
                    new Database(serverName, dbName, port, user, password, schema)));
    }

}

package org.snoth.skynet.service;

import lombok.extern.slf4j.Slf4j;
import org.postgresql.ds.PGSimpleDataSource;
import org.snoth.skynet.bean.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@Qualifier("postgre")
public class DbServicePostgreImpl implements DbService {

    private final String TABLE_LIST = "select table_name from information_schema.tables where table_schema = ?";
    private final String TABLE_COLUMNS = "select cols.column_name, cols.column_default, cols.is_nullable, cols.data_type, cols.character_maximum_length, " +
            "      (select pg_catalog.col_description(c.oid, cols2.ordinal_position::int) " +
            "      from pg_catalog.pg_class c " +
            "      join information_schema.columns cols2 on cols2.table_name = c.relname " +
            "      where cols2.table_schema = cols.table_schema " +
            "      and cols2.table_name = cols.table_name " +
            "      and cols2.column_name = cols.column_name) as column_comment " +
            "from information_schema.columns cols " +
            "where table_schema = ? " +
            "and table_name = ?";

    private final String TABLE_CONSTRAINTS = "select " +
            "        tc.constraint_name, " +
            "        coalesce(kcu.column_name, ccu.column_name, '') as column_name, " +
            "        coalesce(ccu.table_name, '') as foreign_table_name, " +
            "        coalesce(ccu.column_name, '') as foreign_column_name, " +
            "        tc.constraint_type, " +
            "        con.consrc as condition " +
            "    from information_schema.table_constraints as tc " +
            "    join pg_catalog.pg_constraint con on tc.constraint_name = con.conname " +
            "    left join information_schema.key_column_usage as kcu " +
            "                on (tc.constraint_name = kcu.constraint_name and tc.table_name = kcu.table_name) " +
            "    left join information_schema.constraint_column_usage as ccu " +
            "                on ccu.constraint_name = tc.constraint_name " +
            "    where tc.constraint_schema = ? " +
            "    and tc.table_name = ? ";

    private final String TABLE_INDEX = "select indexname, tablespace, indexdef from pg_indexes " +
            "where schemaname = ? " +
            "and tablename = ? ";

    private final String TABLE_COMMENTS = "select obj_description(?::regclass)";


    @Override
    public List<Table> loadDatabase(Database database) {

        PGSimpleDataSource source = new PGSimpleDataSource();
        source.setServerName(database.getServerName());
        source.setDatabaseName(database.getDbName());
        source.setPortNumber(database.getPort());
        source.setUser(database.getUser());
        source.setPassword(database.getPassword());
        source.setCurrentSchema(database.getSchema());

        final List<Table> tableList = new ArrayList<>();
        JdbcTemplate jdbcTemplate = new JdbcTemplate(source);
        try {
            final List<Map<String, Object>> tableMap = jdbcTemplate.queryForList(TABLE_LIST, database.getSchema());
            tableMap.forEach(m -> {
                        final Table table = new Table();
                        final String tableName = getString(m, "table_name");
                        table.setName(tableName);

                        getTableCommentInformation(database.getSchema(), jdbcTemplate, table, tableName);
                        getColumnInformations(database.getSchema(), jdbcTemplate, table, tableName);
                        getConstraintInformations(database.getSchema(), jdbcTemplate, table, tableName);
                        getIndexInformations(database.getSchema(), jdbcTemplate, table, tableName);

                        tableList.add(table);
                    }
            );

            tableList.stream()
                    .map(Table::toString)
                    .forEach(log::info);

        } finally {
            try {
                jdbcTemplate.getDataSource().getConnection().close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return tableList;
    }

    @Override
    public CompareDbResult compare(Database one, Database two) {
        final List<Table> tablesOne = loadDatabase(one);
        final List<Table> tablesTwo = loadDatabase(two);
        return new CompareDbResult();
    }

    private void getIndexInformations(String schema, JdbcTemplate jdbcTemplate, Table table, String tableName) {
        final List<Map<String, Object>> indexMap = jdbcTemplate.queryForList(TABLE_INDEX, schema, tableName);
        indexMap.forEach(i -> {
            Index index = new Index(getString(i, "indexname"), getString(i, "tablespace"), getString(i, "indexdef"));
            table.getIndexes().add(index);
        });
    }

    private void getTableCommentInformation(String schema, JdbcTemplate jdbcTemplate, Table table, String tableName) {
        final List<Map<String, Object>> commentTableMap = jdbcTemplate.queryForList(TABLE_COMMENTS, schema.concat(".").concat(tableName));
        if (!commentTableMap.isEmpty()) {
            table.setComment(getString(commentTableMap.get(0), "obj_description"));
        }
    }

    private void getConstraintInformations(String schema, JdbcTemplate jdbcTemplate, Table table, String tableName) {
        final List<Map<String, Object>> constraintsMap = jdbcTemplate.queryForList(TABLE_CONSTRAINTS, schema, tableName);
        constraintsMap.forEach(c -> {
            Constraint constraint = new Constraint(getString(c, "constraint_name"), getString(c, "constraint_type"));
            if (!constraint.isCheck()) {
                constraint.setColumnName(getString(c, "column_name"));
            } else {
                constraint.setCondition(getString(c, "condition"));
            }
            if (constraint.isForeignKey()) {
                constraint.setForeignTableName(getString(c, "foreign_column_name"));
                constraint.setForeignColumnName(getString(c, "foreign_table_name"));
            }
            table.getConstraints().add(constraint);
        });
    }

    private void getColumnInformations(String schema, JdbcTemplate jdbcTemplate, Table table, String tableName) {
        final List<Map<String, Object>> columnsMap = jdbcTemplate.queryForList(TABLE_COLUMNS, schema, tableName);
        columnsMap.forEach(c -> {
            Column column = new Column(getString(c, "column_name"), getString(c, "data_type"), getInt(c, "character_maximum_length"),
                    getString(c, "column_default"), getBool(c, "is_nullable"), getString(c, "column_comment"));
            table.getColumns().add(column);
        });
    }

    private String getString(Map<String, Object> c, String column_name) {
        return (String) c.get(column_name);
    }

    private Integer getInt(Map<String, Object> c, String column_name) {
        return (Integer) c.get(column_name);
    }

    private String[] trueBoolValues = {"TRUE", "true", "t", "T", "y", "Y", "yes", "YES", "on", "ON", "1"};

    private boolean getBool(Map<String, Object> c, String column_name) {
        final String bool = (String) c.get(column_name);
        return bool != null && Arrays.asList(trueBoolValues).contains(bool);
    }
}

package org.snoth.skynet.service;

import org.snoth.skynet.bean.CompareDbResult;
import org.snoth.skynet.bean.Database;
import org.snoth.skynet.bean.Table;

import java.util.List;

public interface DbService {

    List<Table> loadDatabase(Database database);

    CompareDbResult compare(Database one, Database two);
}

/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.activiti.engine.impl.db;

import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.SortedSet;
import java.util.TreeSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**

 */
public class DbSchemaExport {

  private static final Logger logger = LoggerFactory.getLogger(DbSchemaExport.class);

public static void main(String[] args) throws Exception {
    if (args == null || args.length != 1) {
      logger.error("Syntax: java -cp ... org.activiti.engine.impl.db.DbSchemaExport <path-to-properties-file> <path-to-export-file>");
      return;
    }
    File propertiesFile = new File(args[0]);
    if (!propertiesFile.exists()) {
      logger.error(new StringBuilder().append("File '").append(args[0]).append("' doesn't exist \n").append("Syntax: java -cp ... org.activiti.engine.impl.db.DbSchemaExport <path-to-properties-file> <path-to-export-file>\n").toString());
      return;
    }
    Properties properties = new Properties();
    properties.load(new FileInputStream(propertiesFile));

    String jdbcDriver = properties.getProperty("jdbc.driver");
    String jdbcUrl = properties.getProperty("jdbc.url");
    String jdbcUsername = properties.getProperty("jdbc.username");
    String jdbcPassword = properties.getProperty("jdbc.password");

    Class.forName(jdbcDriver);
    Connection connection = DriverManager.getConnection(jdbcUrl, jdbcUsername, jdbcPassword);
    try {
      DatabaseMetaData meta = connection.getMetaData();

      SortedSet<String> tableNames = new TreeSet<>();
      ResultSet tables = meta.getTables(null, null, null, null);
      while (tables.next()) {
        String tableName = tables.getString(3);
        tableNames.add(tableName);
      }

      logger.info("TABLES");
      for (String tableName : tableNames) {
        Map<String, String> columnDescriptions = new HashMap<>();
        ResultSet columns = meta.getColumns(null, null, tableName, null);
        while (columns.next()) {
          String columnName = columns.getString(4);
          String columnTypeAndSize = new StringBuilder().append(columns.getString(6)).append(" ").append(columns.getInt(7)).toString();
          columnDescriptions.put(columnName, columnTypeAndSize);
        }

        logger.info(tableName);
        new TreeSet<String>(columnDescriptions.keySet()).forEach(columnName -> logger.info(new StringBuilder().append("  ").append(columnName).append(" ").append(columnDescriptions.get(columnName)).toString()));

        logger.info("INDEXES");
        SortedSet<String> indexNames = new TreeSet<>();
        ResultSet indexes = meta.getIndexInfo(null, null, tableName, false, true);
        while (indexes.next()) {
          String indexName = indexes.getString(6);
          indexNames.add(indexName);
        }
        indexNames.forEach(logger::info);
        System.out.println();
      }

    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      connection.close();
    }
  }
}

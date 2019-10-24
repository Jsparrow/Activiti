package org.activiti.engine.test.profiler;

import java.util.HashMap;
import java.util.Map;

/**

 */
public class CommandExecutionResult {

    protected String commandFqn;
    protected long totalTimeInMs;
    protected long databaseTimeInMs;

    protected Map<String, Long> dbSelects = new HashMap<>();
    protected Map<String, Long> dbInserts = new HashMap<>();
    protected Map<String, Long> dbUpdates = new HashMap<>();
    protected Map<String, Long> dbDeletes = new HashMap<>();

    public String getCommandFqn() {
        return commandFqn;
    }

    public void setCommandFqn(String commandFqn) {
        this.commandFqn = commandFqn;
    }

    public long getTotalTimeInMs() {
        return totalTimeInMs;
    }

    public void setTotalTimeInMs(long totalTimeInMs) {
        this.totalTimeInMs = totalTimeInMs;
    }

    public long getDatabaseTimeInMs() {
        return databaseTimeInMs;
    }

    public void setDatabaseTimeInMs(long databaseTimeInMs) {
        this.databaseTimeInMs = databaseTimeInMs;
    }

    public void addDatabaseTime(long time) {
        this.databaseTimeInMs += time;
    }

    public Map<String, Long> getDbSelects() {
        return dbSelects;
    }

    public void addDbSelect(String select) {
        dbSelects.putIfAbsent(select, 0L);
        Long oldValue = dbSelects.get(select);
        dbSelects.put(select, oldValue + 1);
    }

    public void setDbSelects(Map<String, Long> dbSelects) {
        this.dbSelects = dbSelects;
    }

    public Map<String, Long> getDbInserts() {
        return dbInserts;
    }

    public void addDbInsert(String insert) {
        dbInserts.putIfAbsent(insert, 0L);
        Long oldValue = dbInserts.get(insert);
        dbInserts.put(insert, oldValue + 1);
    }

    public void setDbInserts(Map<String, Long> dbInserts) {
        this.dbInserts = dbInserts;
    }

    public Map<String, Long> getDbUpdates() {
        return dbUpdates;
    }

    public void addDbUpdate(String update) {
        dbUpdates.putIfAbsent(update, 0L);
        Long oldValue = dbUpdates.get(update);
        dbUpdates.put(update, oldValue + 1);
    }

    public void setDbUpdates(Map<String, Long> dbUpdates) {
        this.dbUpdates = dbUpdates;
    }

    public Map<String, Long> getDbDeletes() {
        return dbDeletes;
    }

    public void addDbDelete(String delete) {
        dbDeletes.putIfAbsent(delete, 0L);
        Long oldValue = dbDeletes.get(delete);
        dbDeletes.put(delete, oldValue + 1);
    }

    public void setDbDeletes(Map<String, Long> dbDeletes) {
        this.dbDeletes = dbDeletes;
    }
}

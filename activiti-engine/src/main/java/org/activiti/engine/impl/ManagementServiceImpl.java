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
package org.activiti.engine.impl;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.ManagementService;
import org.activiti.engine.event.EventLogEntry;
import org.activiti.engine.impl.cmd.CustomSqlExecution;
import org.activiti.engine.impl.cmd.DeleteDeadLetterJobCmd;
import org.activiti.engine.impl.cmd.DeleteEventLogEntry;
import org.activiti.engine.impl.cmd.DeleteJobCmd;
import org.activiti.engine.impl.cmd.DeleteTimerJobCmd;
import org.activiti.engine.impl.cmd.ExecuteCustomSqlCmd;
import org.activiti.engine.impl.cmd.ExecuteJobCmd;
import org.activiti.engine.impl.cmd.GetEventLogEntriesCmd;
import org.activiti.engine.impl.cmd.GetJobExceptionStacktraceCmd;
import org.activiti.engine.impl.cmd.GetPropertiesCmd;
import org.activiti.engine.impl.cmd.GetTableCountCmd;
import org.activiti.engine.impl.cmd.GetTableMetaDataCmd;
import org.activiti.engine.impl.cmd.GetTableNameCmd;
import org.activiti.engine.impl.cmd.JobType;
import org.activiti.engine.impl.cmd.MoveDeadLetterJobToExecutableJobCmd;
import org.activiti.engine.impl.cmd.MoveJobToDeadLetterJobCmd;
import org.activiti.engine.impl.cmd.MoveTimerToExecutableJobCmd;
import org.activiti.engine.impl.cmd.SetJobRetriesCmd;
import org.activiti.engine.impl.cmd.SetTimerJobRetriesCmd;
import org.activiti.engine.impl.db.DbSqlSession;
import org.activiti.engine.impl.db.DbSqlSessionFactory;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandConfig;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.management.TableMetaData;
import org.activiti.engine.management.TablePageQuery;
import org.activiti.engine.runtime.DeadLetterJobQuery;
import org.activiti.engine.runtime.Job;
import org.activiti.engine.runtime.JobQuery;
import org.activiti.engine.runtime.SuspendedJobQuery;
import org.activiti.engine.runtime.TimerJobQuery;

/**




 */
public class ManagementServiceImpl extends ServiceImpl implements ManagementService {

  @Override
public Map<String, Long> getTableCount() {
    return commandExecutor.execute(new GetTableCountCmd());
  }

  @Override
public String getTableName(Class<?> activitiEntityClass) {
    return commandExecutor.execute(new GetTableNameCmd(activitiEntityClass));
  }

  @Override
public TableMetaData getTableMetaData(String tableName) {
    return commandExecutor.execute(new GetTableMetaDataCmd(tableName));
  }

  @Override
public void executeJob(String jobId) {
    if (jobId == null) {
      throw new ActivitiIllegalArgumentException("JobId is null");
    }
    
    try {
      commandExecutor.execute(new ExecuteJobCmd(jobId));
      
    } catch (RuntimeException e) {
      if (e instanceof ActivitiException) {
        throw e;
      } else {
        throw new ActivitiException(new StringBuilder().append("Job ").append(jobId).append(" failed").toString(), e);
      }
    }
  }
  
  @Override
public Job moveTimerToExecutableJob(String jobId) {
    return commandExecutor.execute(new MoveTimerToExecutableJobCmd(jobId));
  }
  
  @Override
public Job moveJobToDeadLetterJob(String jobId) {
    return commandExecutor.execute(new MoveJobToDeadLetterJobCmd(jobId));
  }
  
  @Override
  public Job moveDeadLetterJobToExecutableJob(String jobId, int retries) {
    return commandExecutor.execute(new MoveDeadLetterJobToExecutableJobCmd(jobId, retries));
  }

  @Override
public void deleteJob(String jobId) {
    commandExecutor.execute(new DeleteJobCmd(jobId));
  }
  
  @Override
public void deleteTimerJob(String jobId) {
    commandExecutor.execute(new DeleteTimerJobCmd(jobId));
  }
  
  @Override
public void deleteDeadLetterJob(String jobId) {
    commandExecutor.execute(new DeleteDeadLetterJobCmd(jobId));
  }

  @Override
public void setJobRetries(String jobId, int retries) {
    commandExecutor.execute(new SetJobRetriesCmd(jobId, retries));
  }
  
  @Override
public void setTimerJobRetries(String jobId, int retries) {
    commandExecutor.execute(new SetTimerJobRetriesCmd(jobId, retries));
  }

  @Override
public TablePageQuery createTablePageQuery() {
    return new TablePageQueryImpl(commandExecutor);
  }

  @Override
public JobQuery createJobQuery() {
    return new JobQueryImpl(commandExecutor);
  }
  
  @Override
public TimerJobQuery createTimerJobQuery() {
    return new TimerJobQueryImpl(commandExecutor);
  }
  
  @Override
public SuspendedJobQuery createSuspendedJobQuery() {
    return new SuspendedJobQueryImpl(commandExecutor);
  }
  
  @Override
public DeadLetterJobQuery createDeadLetterJobQuery() {
    return new DeadLetterJobQueryImpl(commandExecutor);
  }

  @Override
public String getJobExceptionStacktrace(String jobId) {
    return commandExecutor.execute(new GetJobExceptionStacktraceCmd(jobId, JobType.ASYNC));
  }
  
  @Override
public String getTimerJobExceptionStacktrace(String jobId) {
    return commandExecutor.execute(new GetJobExceptionStacktraceCmd(jobId, JobType.TIMER));
  }
  
  @Override
public String getSuspendedJobExceptionStacktrace(String jobId) {
    return commandExecutor.execute(new GetJobExceptionStacktraceCmd(jobId, JobType.SUSPENDED));
  }
  
  @Override
public String getDeadLetterJobExceptionStacktrace(String jobId) {
    return commandExecutor.execute(new GetJobExceptionStacktraceCmd(jobId, JobType.DEADLETTER));
  }

  @Override
public Map<String, String> getProperties() {
    return commandExecutor.execute(new GetPropertiesCmd());
  }

  @Override
public String databaseSchemaUpgrade(final Connection connection, final String catalog, final String schema) {
    CommandConfig config = commandExecutor.getDefaultConfig().transactionNotSupported();
    return commandExecutor.execute(config, (CommandContext commandContext) -> {
        DbSqlSessionFactory dbSqlSessionFactory = (DbSqlSessionFactory) commandContext.getSessionFactories().get(DbSqlSession.class);
        DbSqlSession dbSqlSession = new DbSqlSession(dbSqlSessionFactory, commandContext.getEntityCache(), connection, catalog, schema);
        commandContext.getSessions().put(DbSqlSession.class, dbSqlSession);
        return dbSqlSession.dbSchemaUpdate();
      });
  }

  @Override
public <T> T executeCommand(Command<T> command) {
    if (command == null) {
      throw new ActivitiIllegalArgumentException("The command is null");
    }
    return commandExecutor.execute(command);
  }

  @Override
public <T> T executeCommand(CommandConfig config, Command<T> command) {
    if (config == null) {
      throw new ActivitiIllegalArgumentException("The config is null");
    }
    if (command == null) {
      throw new ActivitiIllegalArgumentException("The command is null");
    }
    return commandExecutor.execute(config, command);
  }

  @Override
  public <MapperType, ResultType> ResultType executeCustomSql(CustomSqlExecution<MapperType, ResultType> customSqlExecution) {
    Class<MapperType> mapperClass = customSqlExecution.getMapperClass();
    return commandExecutor.execute(new ExecuteCustomSqlCmd<MapperType, ResultType>(mapperClass, customSqlExecution));
  }

  @Override
  public List<EventLogEntry> getEventLogEntries(Long startLogNr, Long pageSize) {
    return commandExecutor.execute(new GetEventLogEntriesCmd(startLogNr, pageSize));
  }

  @Override
  public List<EventLogEntry> getEventLogEntriesByProcessInstanceId(String processInstanceId) {
    return commandExecutor.execute(new GetEventLogEntriesCmd(processInstanceId));
  }

  @Override
  public void deleteEventLogEntry(long logNr) {
    commandExecutor.execute(new DeleteEventLogEntry(logNr));
  }

}

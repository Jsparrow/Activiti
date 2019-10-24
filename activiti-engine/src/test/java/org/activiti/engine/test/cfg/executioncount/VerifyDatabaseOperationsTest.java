package org.activiti.engine.test.cfg.executioncount;
import java.util.List;
import java.util.Map;

import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.impl.cfg.CommandExecutorImpl;
import org.activiti.engine.impl.db.DbSqlSessionFactory;
import org.activiti.engine.impl.history.DefaultHistoryManager;
import org.activiti.engine.impl.history.HistoryLevel;
import org.activiti.engine.impl.interceptor.CommandInterceptor;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.profiler.ActivitiProfiler;
import org.activiti.engine.test.profiler.CommandStats;
import org.activiti.engine.test.profiler.ConsoleLogger;
import org.activiti.engine.test.profiler.ProfileSession;
import org.activiti.engine.test.profiler.ProfilingDbSqlSessionFactory;
import org.activiti.engine.test.profiler.TotalExecutionTimeCommandInterceptor;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**

 */
public class VerifyDatabaseOperationsTest extends PluggableActivitiTestCase {
  
  private static final Logger logger = LoggerFactory.getLogger(VerifyDatabaseOperationsTest.class);
protected boolean oldExecutionTreeFetchValue;
  protected boolean oldExecutionRelationshipCountValue;
  protected boolean oldenableProcessDefinitionInfoCacheValue;
  protected CommandInterceptor oldFirstCommandInterceptor;
  protected DbSqlSessionFactory oldDbSqlSessionFactory;
  protected HistoryLevel oldHistoryLevel;
  
  @Override
  protected void setUp() throws Exception {
    super.setUp();

    // Enable flags
    this.oldExecutionTreeFetchValue = processEngineConfiguration.getPerformanceSettings().isEnableEagerExecutionTreeFetching();
    this.oldExecutionRelationshipCountValue = processEngineConfiguration.getPerformanceSettings().isEnableExecutionRelationshipCounts();
    this.oldenableProcessDefinitionInfoCacheValue = processEngineConfiguration.isEnableProcessDefinitionInfoCache();
    oldHistoryLevel = ((DefaultHistoryManager) processEngineConfiguration.getHistoryManager()).getHistoryLevel();

    processEngineConfiguration.getPerformanceSettings().setEnableEagerExecutionTreeFetching(true);
    processEngineConfiguration.getPerformanceSettings().setEnableExecutionRelationshipCounts(true);
    processEngineConfiguration.setEnableProcessDefinitionInfoCache(false);
    ((DefaultHistoryManager) processEngineConfiguration.getHistoryManager()).setHistoryLevel(HistoryLevel.AUDIT);

    // The time interceptor should be first
    CommandExecutorImpl commandExecutor = ((CommandExecutorImpl) processEngineConfiguration.getCommandExecutor());
    this.oldFirstCommandInterceptor = commandExecutor.getFirst();
    
    TotalExecutionTimeCommandInterceptor timeCommandInterceptor = new TotalExecutionTimeCommandInterceptor();
    timeCommandInterceptor.setNext(oldFirstCommandInterceptor);
    commandExecutor.setFirst(timeCommandInterceptor);
    
    // Add dbsqlSession factory that captures CRUD operations
    this.oldDbSqlSessionFactory = processEngineConfiguration.getDbSqlSessionFactory();
    DbSqlSessionFactory newDbSqlSessionFactory = new ProfilingDbSqlSessionFactory();
    newDbSqlSessionFactory.setDatabaseType(oldDbSqlSessionFactory.getDatabaseType());
    newDbSqlSessionFactory.setDatabaseTablePrefix(oldDbSqlSessionFactory.getDatabaseTablePrefix());
    newDbSqlSessionFactory.setTablePrefixIsSchema(oldDbSqlSessionFactory.isTablePrefixIsSchema());
    newDbSqlSessionFactory.setDatabaseCatalog(oldDbSqlSessionFactory.getDatabaseCatalog());
    newDbSqlSessionFactory.setDatabaseSchema(oldDbSqlSessionFactory.getDatabaseSchema());
    newDbSqlSessionFactory.setSqlSessionFactory(oldDbSqlSessionFactory.getSqlSessionFactory());
    newDbSqlSessionFactory.setIdGenerator(oldDbSqlSessionFactory.getIdGenerator());
    newDbSqlSessionFactory.setDbHistoryUsed(oldDbSqlSessionFactory.isDbHistoryUsed());
    processEngineConfiguration.addSessionFactory(newDbSqlSessionFactory);
  }
  
  @Override
  protected void tearDown() throws Exception {
    
    processEngineConfiguration.getPerformanceSettings().setEnableEagerExecutionTreeFetching(oldExecutionTreeFetchValue);
    processEngineConfiguration.getPerformanceSettings().setEnableExecutionRelationshipCounts(oldExecutionRelationshipCountValue);
    processEngineConfiguration.setEnableProcessDefinitionInfoCache(oldenableProcessDefinitionInfoCacheValue);
    ((DefaultHistoryManager) processEngineConfiguration.getHistoryManager()).setHistoryLevel(oldHistoryLevel);
    
    ((CommandExecutorImpl) processEngineConfiguration.getCommandExecutor()).setFirst(oldFirstCommandInterceptor);
    
    processEngineConfiguration.addSessionFactory(oldDbSqlSessionFactory);
    
    // Validate (cause this tended to be screwed up)
    List<HistoricActivityInstance> historicActivityInstances = historyService.createHistoricActivityInstanceQuery().list();
    historicActivityInstances.forEach(historicActivityInstance -> {
      Assert.assertTrue(historicActivityInstance.getStartTime() != null);
      Assert.assertTrue(historicActivityInstance.getEndTime() != null);
    });
    
    ActivitiProfiler.getInstance().reset();
    
    repositoryService.createDeploymentQuery().list().forEach(deployment -> repositoryService.deleteDeployment(deployment.getId(), true));
    super.tearDown();
  }
  
  public void testStartToEnd() {
    deployStartProcessInstanceAndProfile("process01.bpmn20.xml", "process01");
    
    assertDatabaseSelects("StartProcessInstanceCmd", 
        "selectLatestProcessDefinitionByKey", 1L);
    
    assertDatabaseInserts("StartProcessInstanceCmd", 
        "HistoricActivityInstanceEntityImpl-bulk-with-2", 1L,
        "HistoricProcessInstanceEntityImpl", 1L);
    
    assertNoUpdatesAndDeletes("StartProcessInstanceCmd");
    
    Assert.assertEquals(0, runtimeService.createProcessInstanceQuery().count());
    Assert.assertEquals(1, historyService.createHistoricProcessInstanceQuery().finished().count());
  }
  
  public void testVariablesAndPassthrough() {
    deployStartProcessInstanceAndProfile("process-variables-servicetask01.bpmn20.xml", "process-variables-servicetask01");
    
    assertDatabaseSelects("StartProcessInstanceCmd", 
        "selectLatestProcessDefinitionByKey", 1L);
    assertDatabaseInserts("StartProcessInstanceCmd", 
        "HistoricVariableInstanceEntityImpl-bulk-with-4", 1L,
        "HistoricProcessInstanceEntityImpl", 1L,
        "HistoricActivityInstanceEntityImpl-bulk-with-9", 1L);
    assertNoUpdatesAndDeletes("StartProcessInstanceCmd");
    
    Assert.assertEquals(0, runtimeService.createProcessInstanceQuery().count());
    Assert.assertEquals(1, historyService.createHistoricProcessInstanceQuery().finished().count());
  }

  public void testManyVariablesViaServiceTaskAndPassthroughs() {
    deployStartProcessInstanceAndProfile("process-variables-servicetask02.bpmn20.xml", "process-variables-servicetask02");
    
    assertDatabaseSelects("StartProcessInstanceCmd", 
        "selectLatestProcessDefinitionByKey", 1L);
    assertDatabaseInserts("StartProcessInstanceCmd", 
        "HistoricVariableInstanceEntityImpl-bulk-with-50", 1L,
        "HistoricProcessInstanceEntityImpl", 1L,
        "HistoricActivityInstanceEntityImpl-bulk-with-9", 1L);
    assertNoUpdatesAndDeletes("StartProcessInstanceCmd");
    
    Assert.assertEquals(0, runtimeService.createProcessInstanceQuery().count());
    Assert.assertEquals(1, historyService.createHistoricProcessInstanceQuery().finished().count());
  }
  
  public void testOnlyPassThroughs() {
    deployStartProcessInstanceAndProfile("process02.bpmn20.xml", "process02");
    
    assertDatabaseSelects("StartProcessInstanceCmd", 
        "selectLatestProcessDefinitionByKey", 1L);
    assertDatabaseInserts("StartProcessInstanceCmd", 
        "HistoricActivityInstanceEntityImpl-bulk-with-9", 1L,
        "HistoricProcessInstanceEntityImpl", 1L);
    assertNoUpdatesAndDeletes("StartProcessInstanceCmd");
    
    Assert.assertEquals(0, runtimeService.createProcessInstanceQuery().count());
    Assert.assertEquals(1, historyService.createHistoricProcessInstanceQuery().finished().count());
  }

  public void testParallelForkAndJoin() {
    deployStartProcessInstanceAndProfile("process03.bpmn20.xml", "process03");
    
    assertDatabaseSelects("StartProcessInstanceCmd", 
        "selectLatestProcessDefinitionByKey", 1L);
    assertDatabaseInserts("StartProcessInstanceCmd", 
        "HistoricActivityInstanceEntityImpl-bulk-with-7", 1L,
        "HistoricProcessInstanceEntityImpl", 1L);
    assertNoUpdatesAndDeletes("StartProcessInstanceCmd");
    
    Assert.assertEquals(0, runtimeService.createProcessInstanceQuery().count());
    Assert.assertEquals(1, historyService.createHistoricProcessInstanceQuery().finished().count());
  }
  
  public void testNestedParallelForkAndJoin() {
    deployStartProcessInstanceAndProfile("process04.bpmn20.xml", "process04");
    
    assertDatabaseSelects("StartProcessInstanceCmd", 
        "selectLatestProcessDefinitionByKey", 1L);
    assertDatabaseInserts("StartProcessInstanceCmd", 
        "HistoricActivityInstanceEntityImpl-bulk-with-21", 1L,
        "HistoricProcessInstanceEntityImpl", 1L);
    assertNoUpdatesAndDeletes("StartProcessInstanceCmd");
    
    Assert.assertEquals(0, runtimeService.createProcessInstanceQuery().count());
    Assert.assertEquals(1, historyService.createHistoricProcessInstanceQuery().finished().count());
  }
  
  public void testExlusiveGateway() {
    deployStartProcessInstanceAndProfile("process05.bpmn20.xml", "process05");
    
    assertDatabaseSelects("StartProcessInstanceCmd", 
        "selectLatestProcessDefinitionByKey", 1L);
    assertDatabaseInserts("StartProcessInstanceCmd", 
        "HistoricActivityInstanceEntityImpl-bulk-with-5", 1L,
        "HistoricProcessInstanceEntityImpl", 1L,
        "HistoricVariableInstanceEntityImpl", 1L);
    assertNoUpdatesAndDeletes("StartProcessInstanceCmd");
    
    Assert.assertEquals(0, runtimeService.createProcessInstanceQuery().count());
    Assert.assertEquals(1, historyService.createHistoricProcessInstanceQuery().finished().count());
  }
  
  public void testOneTaskProcess() {
    deployStartProcessInstanceAndProfile("process-usertask-01.bpmn20.xml", "process-usertask-01", false);
    Task task = taskService.createTaskQuery().singleResult();
    taskService.complete(task.getId());
    stopProfiling();
    
    assertExecutedCommands("StartProcessInstanceCmd", "org.activiti.engine.impl.TaskQueryImpl", "CompleteTaskCmd");
    
    // Start process instance
    assertDatabaseSelects("StartProcessInstanceCmd", 
        "selectLatestProcessDefinitionByKey", 1L);
    assertDatabaseInserts("StartProcessInstanceCmd", 
        "ExecutionEntityImpl-bulk-with-2", 1L,
        "TaskEntityImpl", 1L,
        "HistoricActivityInstanceEntityImpl-bulk-with-2", 1L,
        "HistoricTaskInstanceEntityImpl", 1L,
        "HistoricProcessInstanceEntityImpl", 1L);
    assertNoUpdatesAndDeletes("StartProcessInstanceCmd");
    
    // Task Query
    assertDatabaseSelects("org.activiti.engine.impl.TaskQueryImpl", 
        "selectTaskByQueryCriteria", 1L);
    assertNoInserts("org.activiti.engine.impl.TaskQueryImpl");
    assertNoUpdates("org.activiti.engine.impl.TaskQueryImpl");
    assertNoDeletes("org.activiti.engine.impl.TaskQueryImpl");
    
    // Task Complete
    
    // TODO: implement counting for tasks similar to executions
    
    assertDatabaseSelects("CompleteTaskCmd", 
        "selectById org.activiti.engine.impl.persistence.entity.HistoricProcessInstanceEntityImpl", 1L,
        "selectById org.activiti.engine.impl.persistence.entity.HistoricTaskInstanceEntityImpl", 1L,
        "selectById org.activiti.engine.impl.persistence.entity.TaskEntityImpl", 1L,
        "selectUnfinishedHistoricActivityInstanceExecutionIdAndActivityId", 2L,
        "selectTasksByParentTaskId", 1L,
        "selectIdentityLinksByTask", 1L,
        "selectVariablesByTaskId", 1L,
        "selectExecutionsWithSameRootProcessInstanceId", 1L,
        "selectTasksByExecutionId", 1L
        );
    assertDatabaseInserts("CompleteTaskCmd", 
        "HistoricActivityInstanceEntityImpl", 1L);
    assertNoUpdates("CompleteTaskCmd");
    assertDatabaseDeletes("CompleteTaskCmd", 
        "TaskEntityImpl", 1L,
        "ExecutionEntityImpl", 2L); // execution and processinstance
    
  }
  
  
  // ---------------------------------
  // HELPERS
  // ---------------------------------
  
  protected void assertExecutedCommands(String...commands) {
    ProfileSession profileSession = ActivitiProfiler.getInstance().getProfileSessions().get(0);
    Map<String, CommandStats> allStats = profileSession.calculateSummaryStatistics();
    
    if (commands.length != allStats.size()) {
      logger.info("Following commands were found: ");
      allStats.keySet().forEach(logger::info);
    }
    Assert.assertEquals(commands.length, allStats.size());
    
    for (String command : commands) {
      Assert.assertNotNull("Could not get stats for " + command, getStatsForCommand(command, allStats));
    }
  }

  protected void assertDatabaseSelects(String commandClass, Object ... expectedSelects) {
    CommandStats stats = getStats(commandClass);
    if (expectedSelects.length / 2 != stats.getDbSelects().size()) {
      Assert.fail("Unexpected number of database selects : " + stats.getDbSelects().size());
    }
    
    for (int i=0; i<expectedSelects.length; i+=2) {
      String dbSelect = (String) expectedSelects[i];
      Long count = (Long) expectedSelects[i+1];
      
      Assert.assertEquals("Wrong select count for " + dbSelect, count, stats.getDbSelects().get(dbSelect));
    }
  }

  protected void assertDatabaseInserts(String commandClass, Object ... expectedInserts) {
    CommandStats stats = getStats(commandClass);
    
    if (expectedInserts.length / 2 != stats.getDbInserts().size()) {
      Assert.fail(new StringBuilder().append("Unexpected number of database inserts : ").append(stats.getDbInserts().size()).append(", but expected ").append(expectedInserts.length / 2).toString());
    }
    
    for (int i=0; i<expectedInserts.length; i+=2) {
      String dbInsert = (String) expectedInserts[i];
      Long count = (Long) expectedInserts[i+1];
      
      Assert.assertEquals(new StringBuilder().append("Insert count for ").append(dbInsert).append("not correct").toString(), count, stats.getDbInserts().get("org.activiti.engine.impl.persistence.entity." + dbInsert));
    }
  }
  
  protected void assertDatabaseDeletes(String commandClass,  Object ... expectedDeletes) {
    CommandStats stats = getStats(commandClass);
    
    if (expectedDeletes.length / 2 != stats.getDbDeletes().size()) {
      Assert.fail("Unexpected number of database deletes : " + stats.getDbDeletes().size());
    }
    
    for (int i=0; i<expectedDeletes.length; i+=2) {
      String dbDelete = (String) expectedDeletes[i];
      Long count = (Long) expectedDeletes[i+1];
      
      Assert.assertEquals(new StringBuilder().append("Delete count count for ").append(dbDelete).append("not correct").toString(), count, stats.getDbDeletes().get("org.activiti.engine.impl.persistence.entity." + dbDelete));
    }
  }
  
  protected void assertNoInserts(String commandClass) {
    CommandStats stats = getStats(commandClass);
    Assert.assertEquals(0, stats.getDbInserts().size());
  }
  
  protected void assertNoUpdatesAndDeletes(String commandClass) {
    assertNoDeletes(commandClass);
    assertNoUpdates(commandClass);
  }
  
  protected void assertNoDeletes(String commandClass) {
    CommandStats stats = getStats(commandClass);
    Assert.assertEquals(0, stats.getDbDeletes().size());
  }
  
  protected void assertNoUpdates(String commandClass) {
    CommandStats stats = getStats(commandClass);
    Assert.assertEquals(0, stats.getDbUpdates().size());
  }
  
  protected CommandStats getStats(String commandClass) {
    ProfileSession profileSession = ActivitiProfiler.getInstance().getProfileSessions().get(0);
    Map<String, CommandStats> allStats = profileSession.calculateSummaryStatistics();
    CommandStats stats = getStatsForCommand(commandClass, allStats);
    return stats;
  }

  protected CommandStats getStatsForCommand(String commandClass, Map<String, CommandStats> allStats) {
    String clazz = commandClass;
    if (!clazz.startsWith("org.activiti")) {
      clazz = "org.activiti.engine.impl.cmd." + clazz;
    }
    CommandStats stats = allStats.get(clazz);
    return stats;
  }

  
  // HELPERS
  
  protected ActivitiProfiler deployStartProcessInstanceAndProfile(String path, String processDefinitionKey) {
    return deployStartProcessInstanceAndProfile(path, processDefinitionKey, true);
  }
  
  protected ActivitiProfiler deployStartProcessInstanceAndProfile(String path, String processDefinitionKey, boolean stopProfilingAfterStart) {
    deploy(path);
    ActivitiProfiler activitiProfiler = startProcessInstanceAndProfile(processDefinitionKey);
    if (stopProfilingAfterStart) {
      stopProfiling();
    }
    return activitiProfiler;
  }
  
  protected void deploy(String path) {
    repositoryService.createDeployment().addClasspathResource("org/activiti/engine/test/cfg/executioncount/" + path).deploy();
  }
  
  protected ActivitiProfiler startProcessInstanceAndProfile(String processDefinitionKey) {
    ActivitiProfiler activitiProfiler = ActivitiProfiler.getInstance();
    activitiProfiler.startProfileSession("Profiling session");
    runtimeService.startProcessInstanceByKey(processDefinitionKey);
    return activitiProfiler;
  }
  
  protected void stopProfiling() {
    ActivitiProfiler profiler = ActivitiProfiler.getInstance();
    profiler.stopCurrentProfileSession();
    new ConsoleLogger(profiler).log();
  }

}

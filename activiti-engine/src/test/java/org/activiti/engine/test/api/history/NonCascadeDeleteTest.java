package org.activiti.engine.test.api.history;

import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.impl.history.HistoryLevel;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.task.Task;
import org.junit.Test;

public class NonCascadeDeleteTest extends PluggableActivitiTestCase {

  private static String processDefinitionKey = "oneTaskProcess";
  
  private String deploymentId;
  
  private String processInstanceId;
  
  @Override
protected void setUp() throws Exception {
    super.setUp();
  }
  
  @Override
protected void tearDown() throws Exception {
	  super.tearDown();
  }
  @Test
  public void testHistoricProcessInstanceQuery(){
    deploymentId = repositoryService.createDeployment()
      .addClasspathResource("org/activiti/engine/test/api/runtime/oneTaskProcess.bpmn20.xml")
      .deploy().getId();

    processInstanceId = runtimeService.startProcessInstanceByKey(processDefinitionKey).getId();
    Task task = taskService.createTaskQuery().processInstanceId(processInstanceId).singleResult();
    taskService.complete(task.getId());
    
    if (!processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
		return;
	}
	HistoricProcessInstance processInstance = historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
	assertEquals(processDefinitionKey, processInstance.getProcessDefinitionKey());
	// Delete deployment and historic process instance remains.
	repositoryService.deleteDeployment(deploymentId, false);
	HistoricProcessInstance processInstanceAfterDelete = historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
	assertNull(processInstanceAfterDelete.getProcessDefinitionKey());
	assertNull(processInstanceAfterDelete.getProcessDefinitionName());
	assertNull(processInstanceAfterDelete.getProcessDefinitionVersion());
	// clean
	historyService.deleteHistoricProcessInstance(processInstanceId);
  }
}

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

package org.activiti.engine.impl.persistence.entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.bpmn.model.ActivitiListener;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.delegate.event.impl.ActivitiEventBuilder;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.CountingExecutionEntity;
import org.activiti.engine.impl.util.ProcessDefinitionUtil;

/**





 */

public class ExecutionEntityImpl extends VariableScopeImpl implements ExecutionEntity, CountingExecutionEntity {

  private static final long serialVersionUID = 1L;

  // current position /////////////////////////////////////////////////////////

  protected FlowElement currentFlowElement; 
  protected ActivitiListener currentActivitiListener; // Only set when executing an execution listener

  /**
   * the process instance. this is the root of the execution tree. the processInstance of a process instance is a self reference.
   */
  protected ExecutionEntityImpl processInstance;

  /** the parent execution */
  protected ExecutionEntityImpl parent;

  /** nested executions representing scopes or concurrent paths */
  protected List<ExecutionEntityImpl> executions;

  /** super execution, not-null if this execution is part of a subprocess */
  protected ExecutionEntityImpl superExecution;

  /** reference to a subprocessinstance, not-null if currently subprocess is started from this execution */
  protected ExecutionEntityImpl subProcessInstance;

  /** The tenant identifier (if any) */
  protected String tenantId = ProcessEngineConfiguration.NO_TENANT_ID;
  protected String name;
  protected String description;
  protected String localizedName;
  protected String localizedDescription;

  protected Date lockTime;

  // state/type of execution //////////////////////////////////////////////////
  
  protected boolean isActive = true;
  protected boolean isScope = true;
  protected boolean isConcurrent;
  protected boolean isEnded;
  protected boolean isEventScope;
  protected boolean isMultiInstanceRoot;
  protected boolean isCountEnabled;

  // events ///////////////////////////////////////////////////////////////////
  
  // TODO: still needed in v6?

  protected String eventName;

  // associated entities /////////////////////////////////////////////////////

  // (we cache associated entities here to minimize db queries)
  protected List<EventSubscriptionEntity> eventSubscriptions;
  protected List<JobEntity> jobs;
  protected List<TimerJobEntity> timerJobs;
  protected List<TaskEntity> tasks;
  protected List<IdentityLinkEntity> identityLinks;

  // cascade deletion ////////////////////////////////////////////////////////

  protected String deleteReason;
  
  protected int suspensionState = SuspensionState.ACTIVE.getStateCode();
  
  protected String startUserId;
  protected Date startTime;
  
  // CountingExecutionEntity 
  protected int eventSubscriptionCount;
  protected int taskCount;
  protected int jobCount;
  protected int timerJobCount;
  protected int suspendedJobCount;
  protected int deadLetterJobCount;
  protected int variableCount;
  protected int identityLinkCount;

  /**
   * persisted reference to the processDefinition.
   * 
   * @see #processDefinition
   * @see #setProcessDefinition(ProcessDefinitionImpl)
   * @see #getProcessDefinition()
   */
  protected String processDefinitionId;

  /**
   * persisted reference to the process definition key.
   */
  protected String processDefinitionKey;

  /**
   * persisted reference to the process definition name.
   */
  protected String processDefinitionName;

  /**
   * persisted reference to the process definition version.
   */
  protected Integer processDefinitionVersion;

  /**
   * persisted reference to the deployment id.
   */
  protected String deploymentId;

  /**
   * persisted reference to the current position in the diagram within the {@link #processDefinition}.
   * 
   * @see #activity
   * @see #setActivity(ActivityImpl)
   * @see #getActivity()
   */
  protected String activityId;

  /**
   * The name of the current activity position
   */
  protected String activityName;

  /**
   * persisted reference to the process instance.
   * 
   * @see #getProcessInstance()
   */
  protected String processInstanceId;

  /**
   * persisted reference to the business key.
   */
  protected String businessKey;

  /**
   * persisted reference to the parent of this execution.
   * 
   * @see #getParent()
   * @see #setParentId(String)
   */
  protected String parentId;

  /**
   * persisted reference to the super execution of this execution
   * 
   * @See {@link #getSuperExecution()}
   * @see #setSuperExecution(ExecutionEntityImpl)
   */
  protected String superExecutionId;
  
  protected String rootProcessInstanceId;
  protected ExecutionEntityImpl rootProcessInstance;

  protected boolean forcedUpdate;

  protected List<VariableInstanceEntity> queryVariables;
  
  protected boolean isDeleted; // TODO: should be in entity superclass probably

  protected String parentProcessInstanceId;

  public ExecutionEntityImpl() {
    
  }
  
  /**
   * Static factory method: to be used when a new execution is created for the very first time/
   * Calling this will make sure no extra db fetches are needed later on, as all collections
   * will be populated with empty collections. If they would be null, it would trigger
   * a database fetch for those relationship entities.
   */
  public static ExecutionEntityImpl createWithEmptyRelationshipCollections() {
    ExecutionEntityImpl execution = new ExecutionEntityImpl();
    execution.executions = new ArrayList<>(1);
    execution.tasks = new ArrayList<>(1);
    execution.variableInstances = new HashMap<>(1);
    execution.jobs = new ArrayList<>(1);
    execution.timerJobs = new ArrayList<>(1);
    execution.eventSubscriptions = new ArrayList<>(1);
    execution.identityLinks = new ArrayList<>(1);
    return execution;
  }
  
  
  //persistent state /////////////////////////////////////////////////////////

 @Override
public Object getPersistentState() {
   Map<String, Object> persistentState = new HashMap<>();
   persistentState.put("processDefinitionId", this.processDefinitionId);
   persistentState.put("businessKey", this.businessKey);
   persistentState.put("activityId", this.activityId);
   persistentState.put("isActive", this.isActive);
   persistentState.put("isConcurrent", this.isConcurrent);
   persistentState.put("isScope", this.isScope);
   persistentState.put("isEventScope", this.isEventScope);
   persistentState.put("parentId", parentId);
   persistentState.put("name", name);
   persistentState.put("lockTime", lockTime);
   persistentState.put("superExecution", this.superExecutionId);
   persistentState.put("rootProcessInstanceId", this.rootProcessInstanceId);
   if (forcedUpdate) {
     persistentState.put("forcedUpdate", Boolean.TRUE);
   }
   persistentState.put("suspensionState", this.suspensionState);
   persistentState.put("startTime", this.startTime);
   persistentState.put("startUserId", this.startUserId);
   persistentState.put("eventSubscriptionCount", eventSubscriptionCount);
   persistentState.put("taskCount", taskCount);
   persistentState.put("jobCount", jobCount);
   persistentState.put("timerJobCount", timerJobCount);
   persistentState.put("suspendedJobCount", suspendedJobCount);
   persistentState.put("deadLetterJobCount", deadLetterJobCount);
   persistentState.put("variableCount", variableCount);
   persistentState.put("identityLinkCount", identityLinkCount);
   return persistentState;
 }
  
  // The current flow element, will be filled during operation execution

  @Override
public FlowElement getCurrentFlowElement() {
    if (currentFlowElement == null) {
      String processDefinitionId = getProcessDefinitionId();
      if (processDefinitionId != null) {
        org.activiti.bpmn.model.Process process = ProcessDefinitionUtil.getProcess(processDefinitionId);
        currentFlowElement = process.getFlowElement(getCurrentActivityId(), true);
      }
    }
    return currentFlowElement;
  }

  @Override
public void setCurrentFlowElement(FlowElement currentFlowElement) {
    this.currentFlowElement = currentFlowElement;
    if (currentFlowElement != null) {
      this.activityId = currentFlowElement.getId();
    } else {
      this.activityId = null;
    }
  }
  
  @Override
public ActivitiListener getCurrentActivitiListener() {
    return currentActivitiListener;
  }

  @Override
public void setCurrentActivitiListener(ActivitiListener currentActivitiListener) {
    this.currentActivitiListener = currentActivitiListener;
  }

  // executions ///////////////////////////////////////////////////////////////

  /** ensures initialization and returns the non-null executions list */
  @Override
public List<ExecutionEntityImpl> getExecutions() {
    ensureExecutionsInitialized();
    return executions;
  }
  
  @Override
  public void addChildExecution(ExecutionEntity executionEntity) {
    ensureExecutionsInitialized();
    executions.add((ExecutionEntityImpl) executionEntity);
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  protected void ensureExecutionsInitialized() {
    if (executions == null) {
      this.executions = (List) Context.getCommandContext().getExecutionEntityManager().findChildExecutionsByParentExecutionId(id);
    }
  }

  // business key ////////////////////////////////////////////////////////////

  @Override
public String getBusinessKey() {
    return businessKey;
  }

  @Override
public void setBusinessKey(String businessKey) {
    this.businessKey = businessKey;
  }

  @Override
public String getProcessInstanceBusinessKey() {
    return getProcessInstance().getBusinessKey();
  }

  // process definition ///////////////////////////////////////////////////////

  @Override
public void setProcessDefinitionId(String processDefinitionId) {
    this.processDefinitionId = processDefinitionId;
  }

  @Override
public String getProcessDefinitionId() {
    return processDefinitionId;
  }

  @Override
public String getProcessDefinitionKey() {
    return processDefinitionKey;
  }

  @Override
public void setProcessDefinitionKey(String processDefinitionKey) {
    this.processDefinitionKey = processDefinitionKey;
  }

  @Override
public String getProcessDefinitionName() {
    return processDefinitionName;
  }

  @Override
public void setProcessDefinitionName(String processDefinitionName) {
    this.processDefinitionName = processDefinitionName;
  }

  @Override
public Integer getProcessDefinitionVersion() {
    return processDefinitionVersion;
  }

  @Override
public void setProcessDefinitionVersion(Integer processDefinitionVersion) {
    this.processDefinitionVersion = processDefinitionVersion;
  }

  @Override
public String getDeploymentId() {
    return deploymentId;
  }

  @Override
public void setDeploymentId(String deploymentId) {
    this.deploymentId = deploymentId;
  }

  // process instance /////////////////////////////////////////////////////////

  /** ensures initialization and returns the process instance. */
  @Override
public ExecutionEntityImpl getProcessInstance() {
    ensureProcessInstanceInitialized();
    return processInstance;
  }

  protected void ensureProcessInstanceInitialized() {
    if ((processInstance == null) && (processInstanceId != null)) {
      processInstance = (ExecutionEntityImpl) Context.getCommandContext().getExecutionEntityManager().findById(processInstanceId);
    }
  }

  @Override
public void setProcessInstance(ExecutionEntity processInstance) {
    this.processInstance = (ExecutionEntityImpl) processInstance;
    if (processInstance != null) {
      this.processInstanceId = this.processInstance.getId();
    }
  }

  @Override
public boolean isProcessInstanceType() {
    return parentId == null;
  }

  // parent ///////////////////////////////////////////////////////////////////

  /** ensures initialization and returns the parent */
  @Override
public ExecutionEntityImpl getParent() {
    ensureParentInitialized();
    return parent;
  }

  protected void ensureParentInitialized() {
    if (parent == null && parentId != null) {
      parent = (ExecutionEntityImpl) Context.getCommandContext().getExecutionEntityManager().findById(parentId);
    }
  }

  @Override
public void setParent(ExecutionEntity parent) {
    this.parent = (ExecutionEntityImpl) parent;

    if (parent != null) {
      this.parentId = parent.getId();
    } else {
      this.parentId = null;
    }
  }

  // parent process instance id      /////////////////////////////////////////

  @Override
public String getParentProcessInstanceId() {
    return parentProcessInstanceId;
  }

  @Override
public void setParentProcessInstanceId(String parentProcessInstanceId) {
      this.parentProcessInstanceId = parentProcessInstanceId;
    }
  
  // super- and subprocess executions /////////////////////////////////////////

  @Override
public String getSuperExecutionId() {
    return superExecutionId;
  }

  @Override
public ExecutionEntityImpl getSuperExecution() {
    ensureSuperExecutionInitialized();
    return superExecution;
  }

  @Override
public void setSuperExecution(ExecutionEntity superExecution) {
    this.superExecution = (ExecutionEntityImpl) superExecution;
    if (superExecution != null) {
      superExecution.setSubProcessInstance(null);
    }

    if (superExecution != null) {
      this.superExecutionId = ((ExecutionEntityImpl) superExecution).getId();
      this.parentProcessInstanceId = superExecution.getProcessInstanceId(); 
    } else {
      this.superExecutionId = null;
      this.parentProcessInstanceId = null;
    }
  }

  protected void ensureSuperExecutionInitialized() {
    if (superExecution == null && superExecutionId != null) {
      superExecution = (ExecutionEntityImpl) Context.getCommandContext().getExecutionEntityManager().findById(superExecutionId);
    }
  }

  @Override
public ExecutionEntityImpl getSubProcessInstance() {
    ensureSubProcessInstanceInitialized();
    return subProcessInstance;
  }

  @Override
public void setSubProcessInstance(ExecutionEntity subProcessInstance) {
    this.subProcessInstance = (ExecutionEntityImpl) subProcessInstance;
  }

  protected void ensureSubProcessInstanceInitialized() {
    if (subProcessInstance == null) {
      subProcessInstance = (ExecutionEntityImpl) Context.getCommandContext().getExecutionEntityManager().findSubProcessInstanceBySuperExecutionId(id);
    }
  }
  
  @Override
public ExecutionEntity getRootProcessInstance() {
    ensureRootProcessInstanceInitialized();
    return rootProcessInstance;
  }
  
  protected void ensureRootProcessInstanceInitialized() {
    if (rootProcessInstanceId == null) {
      rootProcessInstance = (ExecutionEntityImpl) Context.getCommandContext().getExecutionEntityManager().findById(rootProcessInstanceId);
    }
  }
  
  @Override
public void setRootProcessInstance(ExecutionEntity rootProcessInstance) {
    this.rootProcessInstance = (ExecutionEntityImpl) rootProcessInstance;

    if (rootProcessInstance != null) {
      this.rootProcessInstanceId = rootProcessInstance.getId();
    } else {
      this.rootProcessInstanceId = null;
    }
  }
  
  @Override
public String getRootProcessInstanceId() {
    return rootProcessInstanceId;
  }
  
  @Override
public void setRootProcessInstanceId(String rootProcessInstanceId) {
    this.rootProcessInstanceId = rootProcessInstanceId;
  }

  // scopes ///////////////////////////////////////////////////////////////////

  @Override
public boolean isScope() {
    return isScope;
  }

  @Override
public void setScope(boolean isScope) {
    this.isScope = isScope;
  }

  @Override
public void forceUpdate() {
    this.forcedUpdate = true;
  }
  
 // VariableScopeImpl methods //////////////////////////////////////////////////////////////////

  
  // TODO: this should ideally move to another place  
  @Override
  protected void initializeVariableInstanceBackPointer(VariableInstanceEntity variableInstance) {
    if (processInstanceId != null) {
      variableInstance.setProcessInstanceId(processInstanceId);
    } else {
      variableInstance.setProcessInstanceId(id);
    }
    variableInstance.setExecutionId(id);
  }

  @Override
  protected Collection<VariableInstanceEntity> loadVariableInstances() {
    return Context.getCommandContext().getVariableInstanceEntityManager().findVariableInstancesByExecutionId(id);
  }

  @Override
  protected VariableScopeImpl getParentVariableScope() {
    return getParent();
  }

  /**
   * used to calculate the sourceActivityExecution for method {@link #updateActivityInstanceIdInHistoricVariableUpdate(HistoricDetailVariableInstanceUpdateEntity, ExecutionEntityImpl)}
   */
  @Override
protected ExecutionEntityImpl getSourceActivityExecution() {
    return this;
  }

  @Override
  protected VariableInstanceEntity createVariableInstance(String variableName, Object value, ExecutionEntity sourceActivityExecution) {
    VariableInstanceEntity result = super.createVariableInstance(variableName, value, sourceActivityExecution);

    // Dispatch event, if needed
    if (Context.getProcessEngineConfiguration() != null && Context.getProcessEngineConfiguration().getEventDispatcher().isEnabled()) {
      Context
          .getProcessEngineConfiguration()
          .getEventDispatcher()
          .dispatchEvent(
              ActivitiEventBuilder.createVariableEvent(ActivitiEventType.VARIABLE_CREATED, variableName, value, result.getType(), result.getTaskId(), result.getExecutionId(), getProcessInstanceId(),
                  getProcessDefinitionId()));
    }
    return result;
  }
  
  @Override
  protected void updateVariableInstance(VariableInstanceEntity variableInstance, Object value, ExecutionEntity sourceActivityExecution) {
    super.updateVariableInstance(variableInstance, value, sourceActivityExecution);

    // Dispatch event, if needed
    if (Context.getProcessEngineConfiguration() != null && Context.getProcessEngineConfiguration().getEventDispatcher().isEnabled()) {
      Context
          .getProcessEngineConfiguration()
          .getEventDispatcher()
          .dispatchEvent(
              ActivitiEventBuilder.createVariableEvent(ActivitiEventType.VARIABLE_UPDATED, variableInstance.getName(), value, variableInstance.getType(), variableInstance.getTaskId(),
                  variableInstance.getExecutionId(), getProcessInstanceId(), getProcessDefinitionId()));
    }
  }

  @Override
  protected VariableInstanceEntity getSpecificVariable(String variableName) {

    CommandContext commandContext = Context.getCommandContext();
    if (commandContext == null) {
      throw new ActivitiException("lazy loading outside command context");
    }
    VariableInstanceEntity variableInstance = commandContext.getVariableInstanceEntityManager().findVariableInstanceByExecutionAndName(id, variableName);

    return variableInstance;
  }

  @Override
  protected List<VariableInstanceEntity> getSpecificVariables(Collection<String> variableNames) {
    CommandContext commandContext = Context.getCommandContext();
    if (commandContext == null) {
      throw new ActivitiException("lazy loading outside command context");
    }
    return commandContext.getVariableInstanceEntityManager().findVariableInstancesByExecutionAndNames(id, variableNames);
  }

  // event subscription support //////////////////////////////////////////////

  @Override
public List<EventSubscriptionEntity> getEventSubscriptions() {
    ensureEventSubscriptionsInitialized();
    return eventSubscriptions;
  }

  protected void ensureEventSubscriptionsInitialized() {
    if (eventSubscriptions == null) {
      eventSubscriptions = Context.getCommandContext().getEventSubscriptionEntityManager().findEventSubscriptionsByExecution(id);
    }
  }

  // referenced job entities //////////////////////////////////////////////////

  @Override
public List<JobEntity> getJobs() {
    ensureJobsInitialized();
    return jobs;
  }
  
  protected void ensureJobsInitialized() {
    if (jobs == null) {
      jobs = Context.getCommandContext().getJobEntityManager().findJobsByExecutionId(id);
    }
  }
  
  @Override
public List<TimerJobEntity> getTimerJobs() {
    ensureTimerJobsInitialized();
    return timerJobs;
  }
  
  protected void ensureTimerJobsInitialized() {
    if (timerJobs == null) {
      timerJobs = Context.getCommandContext().getTimerJobEntityManager().findJobsByExecutionId(id);
    }
  }

  // referenced task entities ///////////////////////////////////////////////////

  protected void ensureTasksInitialized() {
    if (tasks == null) {
      tasks = Context.getCommandContext().getTaskEntityManager().findTasksByExecutionId(id);
    }
  }

  @Override
public List<TaskEntity> getTasks() {
    ensureTasksInitialized();
    return tasks;
  }

  // identity links ///////////////////////////////////////////////////////////

  @Override
public List<IdentityLinkEntity> getIdentityLinks() {
    ensureIdentityLinksInitialized();
    return identityLinks;
  }

  protected void ensureIdentityLinksInitialized() {
    if (identityLinks == null) {
      identityLinks = Context.getCommandContext().getIdentityLinkEntityManager().findIdentityLinksByProcessInstanceId(id);
    }
  }

  // getters and setters //////////////////////////////////////////////////////

  @Override
public String getProcessInstanceId() {
    return processInstanceId;
  }

  @Override
public void setProcessInstanceId(String processInstanceId) {
    this.processInstanceId = processInstanceId;
  }

  @Override
public String getParentId() {
    return parentId;
  }

  @Override
public void setParentId(String parentId) {
    this.parentId = parentId;
  }

  @Override
public String getActivityId() {
    return activityId;
  }

  @Override
public boolean isConcurrent() {
    return isConcurrent;
  }

  @Override
public void setConcurrent(boolean isConcurrent) {
    this.isConcurrent = isConcurrent;
  }

  @Override
public boolean isActive() {
    return isActive;
  }

  @Override
public void setActive(boolean isActive) {
    this.isActive = isActive;
  }
  
  @Override
public void inactivate() {
    this.isActive = false;
  }

  @Override
public boolean isEnded() {
    return isEnded;
  }
  
  @Override
public void setEnded(boolean isEnded) {
    this.isEnded = isEnded;
  }

  @Override
public String getEventName() {
    return eventName;
  }

  @Override
public void setEventName(String eventName) {
    this.eventName = eventName;
  }

  @Override
public String getDeleteReason() {
    return deleteReason;
  }

  @Override
public void setDeleteReason(String deleteReason) {
    this.deleteReason = deleteReason;
  }

  @Override
public int getSuspensionState() {
    return suspensionState;
  }

  @Override
public void setSuspensionState(int suspensionState) {
    this.suspensionState = suspensionState;
  }

  @Override
public boolean isSuspended() {
    return suspensionState == SuspensionState.SUSPENDED.getStateCode();
  }

  @Override
public boolean isEventScope() {
    return isEventScope;
  }

  @Override
public void setEventScope(boolean isEventScope) {
    this.isEventScope = isEventScope;
  }
  
  @Override
  public boolean isMultiInstanceRoot() {
    return isMultiInstanceRoot;
  }
  
  @Override
  public void setMultiInstanceRoot(boolean isMultiInstanceRoot) {
    this.isMultiInstanceRoot = isMultiInstanceRoot;
  }
  
  @Override
  public boolean isCountEnabled() {
    return isCountEnabled;
  }

  @Override
  public void setCountEnabled(boolean isCountEnabled) {
    this.isCountEnabled = isCountEnabled;
  }

  @Override
public String getCurrentActivityId() {
    return activityId;
  }

  @Override
  public String getName() {
    if (localizedName != null && localizedName.length() > 0) {
      return localizedName;
    } else {
      return name;
    }
  }

  @Override
public void setName(String name) {
    this.name = name;
  }

  @Override
public String getDescription() {
    if (localizedDescription != null && localizedDescription.length() > 0) {
      return localizedDescription;
    } else {
      return description;
    }
  }
  
  @Override
public void setDescription(String description) {
    this.description = description;
  }
  
  @Override
public String getLocalizedName() {
    return localizedName;
  }
  
  @Override
public void setLocalizedName(String localizedName) {
    this.localizedName = localizedName;
  }
  
  @Override
public String getLocalizedDescription() {
    return localizedDescription;
  }
  
  @Override
public void setLocalizedDescription(String localizedDescription) {
    this.localizedDescription = localizedDescription;
  }

  @Override
public String getTenantId() {
    return tenantId;
  }

  @Override
public void setTenantId(String tenantId) {
    this.tenantId = tenantId;
  }

  @Override
public Date getLockTime() {
    return lockTime;
  }

  @Override
public void setLockTime(Date lockTime) {
    this.lockTime = lockTime;
  }

  @Override
public Map<String, Object> getProcessVariables() {
    Map<String, Object> variables = new HashMap<>();
    if (queryVariables != null) {
      queryVariables.stream().filter(variableInstance -> variableInstance.getId() != null && variableInstance.getTaskId() == null).forEach(variableInstance -> variables.put(variableInstance.getName(), variableInstance.getValue()));
    }
    return variables;
  }

  public List<VariableInstanceEntity> getQueryVariables() {
    if (queryVariables == null && Context.getCommandContext() != null) {
      queryVariables = new VariableInitializingList();
    }
    return queryVariables;
  }

  public void setQueryVariables(List<VariableInstanceEntity> queryVariables) {
    this.queryVariables = queryVariables;
  }

  @Override
public boolean isDeleted() {
    return isDeleted;
  }

  @Override
public void setDeleted(boolean isDeleted) {
    this.isDeleted = isDeleted;
  }
  
  public String getActivityName() {
    return activityName;
  }

  @Override
public String getStartUserId() {
    return startUserId;
  }

  @Override
public void setStartUserId(String startUserId) {
    this.startUserId = startUserId;
  }

  @Override
public Date getStartTime() {
    return startTime;
  }

  @Override
public void setStartTime(Date startTime) {
    this.startTime = startTime;
  }
  
  @Override
public int getEventSubscriptionCount() {
    return eventSubscriptionCount;
  }

  @Override
public void setEventSubscriptionCount(int eventSubscriptionCount) {
    this.eventSubscriptionCount = eventSubscriptionCount;
  }

  @Override
public int getTaskCount() {
    return taskCount;
  }

  @Override
public void setTaskCount(int taskCount) {
    this.taskCount = taskCount;
  }

  @Override
public int getJobCount() {
    return jobCount;
  }

  @Override
public void setJobCount(int jobCount) {
    this.jobCount = jobCount;
  }
  
  @Override
public int getTimerJobCount() {
    return timerJobCount;
  }

  @Override
public void setTimerJobCount(int timerJobCount) {
    this.timerJobCount = timerJobCount;
  }

  @Override
public int getSuspendedJobCount() {
    return suspendedJobCount;
  }

  @Override
public void setSuspendedJobCount(int suspendedJobCount) {
    this.suspendedJobCount = suspendedJobCount;
  }

  @Override
public int getDeadLetterJobCount() {
    return deadLetterJobCount;
  }

  @Override
public void setDeadLetterJobCount(int deadLetterJobCount) {
    this.deadLetterJobCount = deadLetterJobCount;
  }

  @Override
public int getVariableCount() {
    return variableCount;
  }

  @Override
public void setVariableCount(int variableCount) {
    this.variableCount = variableCount;
  }
  
  @Override
public int getIdentityLinkCount() {
    return identityLinkCount;
  }
  
  @Override
public void setIdentityLinkCount(int identityLinkCount) {
    this.identityLinkCount = identityLinkCount;
  }  


  //toString /////////////////////////////////////////////////////////////////

  @Override
public String toString() {
    if (isProcessInstanceType()) {
      return new StringBuilder().append("ProcessInstance[").append(getId()).append("]").toString();
    } else {
      StringBuilder strb = new StringBuilder();
      if (isScope) {
        strb.append(new StringBuilder().append("Scoped execution[ id '").append(getId()).append("' ]").toString());
      } else if(isMultiInstanceRoot) {
        strb.append(new StringBuilder().append("Multi instance root execution[ id '").append(getId()).append("' ]").toString());
      } else {
        strb.append(new StringBuilder().append("Execution[ id '").append(getId()).append("' ]").toString());
      }
      if (activityId != null) {
        strb.append(" - activity '" + activityId);
      }
      if (parentId != null) {
        strb.append(new StringBuilder().append(" - parent '").append(parentId).append("'").toString());
      }
      return strb.toString();
    }
  }

}

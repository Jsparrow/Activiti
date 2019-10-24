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

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.db.BulkDeleteable;

/**

 */
public class IdentityLinkEntityImpl extends AbstractEntityNoRevision implements IdentityLinkEntity, Serializable, BulkDeleteable {

  private static final long serialVersionUID = 1L;

  protected String type;
  protected String userId;
  protected String groupId;
  protected String taskId;
  protected String processInstanceId;
  protected String processDefId;
  protected TaskEntity task;
  protected ExecutionEntity processInstance;
  protected ProcessDefinitionEntity processDef;
  
  public IdentityLinkEntityImpl() {
    
  }

  @Override
public Object getPersistentState() {
    Map<String, Object> persistentState = new HashMap<>();
    persistentState.put("id", this.id);
    persistentState.put("type", this.type);

    if (this.userId != null) {
      persistentState.put("userId", this.userId);
    }

    if (this.groupId != null) {
      persistentState.put("groupId", this.groupId);
    }

    if (this.taskId != null) {
      persistentState.put("taskId", this.taskId);
    }

    if (this.processInstanceId != null) {
      persistentState.put("processInstanceId", this.processInstanceId);
    }

    if (this.processDefId != null) {
      persistentState.put("processDefId", this.processDefId);
    }

    return persistentState;
  }

  @Override
public boolean isUser() {
    return userId != null;
  }

  @Override
public boolean isGroup() {
    return groupId != null;
  }

  @Override
public String getType() {
    return type;
  }

  @Override
public void setType(String type) {
    this.type = type;
  }

  @Override
public String getUserId() {
    return userId;
  }

  @Override
public void setUserId(String userId) {
    if (this.groupId != null && userId != null) {
      throw new ActivitiException("Cannot assign a userId to a task assignment that already has a groupId");
    }
    this.userId = userId;
  }

  @Override
public String getGroupId() {
    return groupId;
  }

  @Override
public void setGroupId(String groupId) {
    if (this.userId != null && groupId != null) {
      throw new ActivitiException("Cannot assign a groupId to a task assignment that already has a userId");
    }
    this.groupId = groupId;
  }

  @Override
public String getTaskId() {
    return taskId;
  }

  @Override
public void setTaskId(String taskId) {
    this.taskId = taskId;
  }

  @Override
public String getProcessInstanceId() {
    return processInstanceId;
  }

  @Override
public void setProcessInstanceId(String processInstanceId) {
    this.processInstanceId = processInstanceId;
  }

  @Override
public String getProcessDefId() {
    return processDefId;
  }

  @Override
public void setProcessDefId(String processDefId) {
    this.processDefId = processDefId;
  }

  @Override
public TaskEntity getTask() {
    if ((task == null) && (taskId != null)) {
      this.task = Context.getCommandContext().getTaskEntityManager().findById(taskId);
    }
    return task;
  }

  @Override
public void setTask(TaskEntity task) {
    this.task = task;
    this.taskId = task.getId();
  }

  @Override
public ExecutionEntity getProcessInstance() {
    if ((processInstance == null) && (processInstanceId != null)) {
      this.processInstance = Context.getCommandContext().getExecutionEntityManager().findById(processInstanceId);
    }
    return processInstance;
  }

  @Override
public void setProcessInstance(ExecutionEntity processInstance) {
    this.processInstance = processInstance;
    this.processInstanceId = processInstance.getId();
  }

  @Override
public ProcessDefinitionEntity getProcessDef() {
    if ((processDef == null) && (processDefId != null)) {
      this.processDef = Context.getCommandContext().getProcessDefinitionEntityManager().findById(processDefId);
    }
    return processDef;
  }

  @Override
public void setProcessDef(ProcessDefinitionEntity processDef) {
    this.processDef = processDef;
    this.processDefId = processDef.getId();
  }

  @Override
  public String getProcessDefinitionId() {
    return this.processDefId;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("IdentityLinkEntity[id=").append(id);
    sb.append(", type=").append(type);
    if (userId != null) {
      sb.append(", userId=").append(userId);
    }
    if (groupId != null) {
      sb.append(", groupId=").append(groupId);
    }
    if (taskId != null) {
      sb.append(", taskId=").append(taskId);
    }
    if (processInstanceId != null) {
      sb.append(", processInstanceId=").append(processInstanceId);
    }
    if (processDefId != null) {
      sb.append(", processDefId=").append(processDefId);
    }
    sb.append("]");
    return sb.toString();
  }
}

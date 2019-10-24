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
package org.activiti.engine.impl.cmd;

import java.io.Serializable;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.delegate.event.impl.ActivitiEventBuilder;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.runtime.ProcessInstance;

public class SetProcessInstanceNameCmd implements Command<Void>, Serializable {

  private static final long serialVersionUID = 1L;

  protected String processInstanceId;
  protected String name;

  public SetProcessInstanceNameCmd(String processInstanceId, String name) {
    this.processInstanceId = processInstanceId;
    this.name = name;
  }

  @Override
  public Void execute(CommandContext commandContext) {
    if (processInstanceId == null) {
      throw new ActivitiIllegalArgumentException("processInstanceId is null");
    }

    ExecutionEntity execution = commandContext.getExecutionEntityManager().findById(processInstanceId);

    if (execution == null) {
      throw new ActivitiObjectNotFoundException(new StringBuilder().append("process instance ").append(processInstanceId).append(" doesn't exist").toString(), ProcessInstance.class);
    }

    if (!execution.isProcessInstanceType()) {
      throw new ActivitiObjectNotFoundException(new StringBuilder().append("process instance ").append(processInstanceId).append(" doesn't exist, the given ID references an execution, though").toString(), ProcessInstance.class);
    }

    if (execution.isSuspended()) {
      throw new ActivitiException(new StringBuilder().append("process instance ").append(processInstanceId).append(" is suspended, cannot set name").toString());
    }

    // Actually set the name
    execution.setName(name);
    
    if (commandContext.getEventDispatcher().isEnabled()) {
        commandContext.getEventDispatcher().dispatchEvent(ActivitiEventBuilder.createEntityEvent(ActivitiEventType.ENTITY_UPDATED, execution));
      }

    // Record the change in history
    commandContext.getHistoryManager().recordProcessInstanceNameChange(processInstanceId, name);

    return null;
  }

}

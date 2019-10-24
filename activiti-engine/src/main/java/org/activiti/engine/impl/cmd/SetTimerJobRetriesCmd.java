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

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.delegate.event.impl.ActivitiEventBuilder;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.TimerJobEntity;
import org.activiti.engine.runtime.Job;

/**

 */
public class SetTimerJobRetriesCmd implements Command<Void>, Serializable {

  private static final long serialVersionUID = 1L;

  private final String jobId;
  private final int retries;

  public SetTimerJobRetriesCmd(String jobId, int retries) {
    if (jobId == null || jobId.length() < 1) {
      throw new ActivitiIllegalArgumentException(new StringBuilder().append("The job id is mandatory, but '").append(jobId).append("' has been provided.").toString());
    }
    if (retries < 0) {
      throw new ActivitiIllegalArgumentException(new StringBuilder().append("The number of job retries must be a non-negative Integer, but '").append(retries).append("' has been provided.").toString());
    }
    this.jobId = jobId;
    this.retries = retries;
  }

  @Override
public Void execute(CommandContext commandContext) {
    TimerJobEntity job = commandContext.getTimerJobEntityManager().findById(jobId);
    if (job != null) {
      
      job.setRetries(retries);

      if (commandContext.getEventDispatcher().isEnabled()) {
        commandContext.getEventDispatcher().dispatchEvent(ActivitiEventBuilder.createEntityEvent(ActivitiEventType.ENTITY_UPDATED, job));
      }
    } else {
      throw new ActivitiObjectNotFoundException(new StringBuilder().append("No timer job found with id '").append(jobId).append("'.").toString(), Job.class);
    }
    return null;
  }
}

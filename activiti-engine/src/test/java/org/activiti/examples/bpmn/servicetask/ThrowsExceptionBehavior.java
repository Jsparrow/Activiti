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

package org.activiti.examples.bpmn.servicetask;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.DelegateHelper;
import org.activiti.engine.impl.delegate.ActivityBehavior;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**

 */
public class ThrowsExceptionBehavior implements ActivityBehavior {

  private static final Logger logger = LoggerFactory.getLogger(ThrowsExceptionBehavior.class);

@Override
public void execute(DelegateExecution execution) {
    String var = (String) execution.getVariable("var");
    
    String sequenceFlowToTake = null; 
    
    try {
      executeLogic(var);
      sequenceFlowToTake = "no-exception";
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
	sequenceFlowToTake = "exception";
    }
    
    DelegateHelper.leaveDelegate(execution, sequenceFlowToTake);
  }

  protected void executeLogic(String value) {
    if ("throw-exception".equals(value)) {
      throw new RuntimeException();
    }
  }
  
}

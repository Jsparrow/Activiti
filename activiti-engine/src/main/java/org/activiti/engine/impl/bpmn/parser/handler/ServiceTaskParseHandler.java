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
package org.activiti.engine.impl.bpmn.parser.handler;

import org.activiti.bpmn.model.BaseElement;
import org.activiti.bpmn.model.ImplementationType;
import org.activiti.bpmn.model.ServiceTask;
import org.activiti.engine.impl.bpmn.parser.BpmnParse;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServiceTaskParseHandler extends AbstractActivityBpmnParseHandler<ServiceTask> {

  private static Logger logger = LoggerFactory.getLogger(ServiceTaskParseHandler.class);

  @Override
public Class<? extends BaseElement> getHandledType() {
    return ServiceTask.class;
  }

  @Override
protected void executeParse(BpmnParse bpmnParse, ServiceTask serviceTask) {

    if (StringUtils.isNotEmpty(serviceTask.getType())) {
      createActivityBehaviorForServiceTaskType(bpmnParse, serviceTask);
    } else if (ImplementationType.IMPLEMENTATION_TYPE_CLASS.equalsIgnoreCase(serviceTask.getImplementationType())) {
      createClassDelegateServiceTask(bpmnParse, serviceTask);
    } else if (ImplementationType.IMPLEMENTATION_TYPE_DELEGATEEXPRESSION.equalsIgnoreCase(serviceTask.getImplementationType())) {
      createServiceTaskDelegateExpressionActivityBehavior(bpmnParse, serviceTask);
    } else if (ImplementationType.IMPLEMENTATION_TYPE_EXPRESSION.equalsIgnoreCase(serviceTask.getImplementationType())) {
      createServiceTaskExpressionActivityBehavior(bpmnParse, serviceTask);
    } else if (ImplementationType.IMPLEMENTATION_TYPE_WEBSERVICE.equalsIgnoreCase(serviceTask.getImplementationType()) && StringUtils.isNotEmpty(serviceTask.getOperationRef())) {
      createWebServiceActivityBehavior(bpmnParse, serviceTask);
    } else {
      createDefaultServiceTaskActivityBehavior(bpmnParse, serviceTask);
    }

  }
  
  protected void createActivityBehaviorForServiceTaskType(BpmnParse bpmnParse, ServiceTask serviceTask) {
    if ("mail".equalsIgnoreCase(serviceTask.getType())) {
      createMailActivityBehavior(bpmnParse, serviceTask);
    } else if ("mule".equalsIgnoreCase(serviceTask.getType())) {
      createMuleActivityBehavior(bpmnParse, serviceTask);
    } else if ("camel".equalsIgnoreCase(serviceTask.getType())) {
      createCamelActivityBehavior(bpmnParse, serviceTask);
    } else if ("shell".equalsIgnoreCase(serviceTask.getType())) {
      createShellActivityBehavior(bpmnParse, serviceTask);
    } else {
      createActivityBehaviorForCustomServiceTaskType(bpmnParse, serviceTask);
    }
  }

  protected void createMailActivityBehavior(BpmnParse bpmnParse, ServiceTask serviceTask) {
    serviceTask.setBehavior(bpmnParse.getActivityBehaviorFactory().createMailActivityBehavior(serviceTask));
  }

  protected void createMuleActivityBehavior(BpmnParse bpmnParse, ServiceTask serviceTask) {
    serviceTask.setBehavior(bpmnParse.getActivityBehaviorFactory().createMuleActivityBehavior(serviceTask));
  }

  protected void createCamelActivityBehavior(BpmnParse bpmnParse, ServiceTask serviceTask) {
    serviceTask.setBehavior(bpmnParse.getActivityBehaviorFactory().createCamelActivityBehavior(serviceTask));
  }

  protected void createShellActivityBehavior(BpmnParse bpmnParse, ServiceTask serviceTask) {
    serviceTask.setBehavior(bpmnParse.getActivityBehaviorFactory().createShellActivityBehavior(serviceTask));
  }
  
  protected void createActivityBehaviorForCustomServiceTaskType(BpmnParse bpmnParse, ServiceTask serviceTask) {
    logger.warn(new StringBuilder().append("Invalid service task type: '").append(serviceTask.getType()).append("' ").append(" for service task ").append(serviceTask.getId()).toString());
  }

  protected void createClassDelegateServiceTask(BpmnParse bpmnParse, ServiceTask serviceTask) {
    serviceTask.setBehavior(bpmnParse.getActivityBehaviorFactory().createClassDelegateServiceTask(serviceTask));
  }

  protected void createServiceTaskDelegateExpressionActivityBehavior(BpmnParse bpmnParse, ServiceTask serviceTask) {
    serviceTask.setBehavior(bpmnParse.getActivityBehaviorFactory().createServiceTaskDelegateExpressionActivityBehavior(serviceTask));
  }

  protected void createServiceTaskExpressionActivityBehavior(BpmnParse bpmnParse, ServiceTask serviceTask) {
    serviceTask.setBehavior(bpmnParse.getActivityBehaviorFactory().createServiceTaskExpressionActivityBehavior(serviceTask));
  }

  protected void createWebServiceActivityBehavior(BpmnParse bpmnParse, ServiceTask serviceTask) {
    serviceTask.setBehavior(bpmnParse.getActivityBehaviorFactory().createWebServiceActivityBehavior(serviceTask));
  }

  protected void createDefaultServiceTaskActivityBehavior(BpmnParse bpmnParse, ServiceTask serviceTask) {
    serviceTask.setBehavior(bpmnParse.getActivityBehaviorFactory().createDefaultServiceTaskBehavior(serviceTask));
  }
}

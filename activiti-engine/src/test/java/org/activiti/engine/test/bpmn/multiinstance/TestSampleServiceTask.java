package org.activiti.engine.test.bpmn.multiinstance;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.impl.bpmn.behavior.AbstractBpmnActivityBehavior;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**

 */
public class TestSampleServiceTask extends AbstractBpmnActivityBehavior {
  
  private static final Logger logger = LoggerFactory.getLogger(TestSampleServiceTask.class);
private static final long serialVersionUID = 1L;

  @Override
  public void execute(DelegateExecution execution) {
    logger.info(new StringBuilder().append("###: execution: ").append(execution.getId()).append("; ").append(execution.getVariable("value")).append("; ").append(getMultiInstanceActivityBehavior())
			.toString());
    leave(execution);
  }
}

package org.activiti.engine.test.api.repository;

import java.util.List;
import java.util.Map;

import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.ExtensionElement;
import org.activiti.bpmn.model.Lane;
import org.activiti.bpmn.model.Process;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.test.Deployment;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by P3700487 on 2/19/2015.
 */
public class LaneExtensionTest extends PluggableActivitiTestCase {

  private static final Logger logger = LoggerFactory.getLogger(LaneExtensionTest.class);

@Test
  @Deployment
  public void testLaneExtensionElement() {
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionKey("swimlane-extension").singleResult();
    BpmnModel bpmnModel = repositoryService.getBpmnModel(processDefinition.getId());
    byte[] xml = new BpmnXMLConverter().convertToXML(bpmnModel);
    logger.info(new String(xml));
    Process bpmnProcess = bpmnModel.getMainProcess();
    bpmnProcess.getLanes().stream().map(Lane::getExtensionElements).forEach(extensions -> Assert.assertTrue(extensions.size() > 0));
  }

}

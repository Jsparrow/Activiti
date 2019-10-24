package org.activiti.editor.language;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.ParallelGateway;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.bpmn.model.SubProcess;
import org.junit.Test;

public class FlowNodeInSubProcessConverterTest extends AbstractConverterTest {

  @Test
  public void doubleConversionValidation() throws Exception {
    BpmnModel bpmnModel = readJsonFile();
    validateModel(bpmnModel);
    bpmnModel = convertToJsonAndBack(bpmnModel);
    validateModel(bpmnModel);
  }

  private void validateModel(BpmnModel model) {
    FlowElement flowElement = model.getMainProcess().getFlowElement("subprocess1", true);
    assertNotNull(flowElement);
    assertTrue(flowElement instanceof SubProcess);
    SubProcess subProcess = (SubProcess) flowElement;
    ParallelGateway gateway = (ParallelGateway) subProcess.getFlowElement("sid-A0E0B174-36DF-4C4F-A952-311CC3C031FC");
    assertNotNull(gateway);
    List<SequenceFlow> sequenceFlows = gateway.getOutgoingFlows();
    assertTrue(sequenceFlows.size() == 2);
    assertTrue("sid-9C669980-C274-4A48-BF7F-B9C5CA577DD2".equals(sequenceFlows.get(0).getId()) || "sid-A299B987-396F-46CA-8D63-85991FBFCE6E".equals(sequenceFlows.get(0).getId()));
    assertTrue("sid-9C669980-C274-4A48-BF7F-B9C5CA577DD2".equals(sequenceFlows.get(1).getId()) || "sid-A299B987-396F-46CA-8D63-85991FBFCE6E".equals(sequenceFlows.get(1).getId()));
    assertTrue("sid-A0E0B174-36DF-4C4F-A952-311CC3C031FC".equals(sequenceFlows.get(0).getSourceRef()));
    assertTrue("sid-A0E0B174-36DF-4C4F-A952-311CC3C031FC".equals(sequenceFlows.get(1).getSourceRef()));
  }

  @Override
protected String getResource() {
    return "test.flownodeinsubprocessmodel.json";
  }

}

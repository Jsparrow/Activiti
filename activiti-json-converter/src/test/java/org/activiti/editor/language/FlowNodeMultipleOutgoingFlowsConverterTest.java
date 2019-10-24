package org.activiti.editor.language;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.ParallelGateway;
import org.activiti.bpmn.model.SequenceFlow;
import org.junit.Test;

public class FlowNodeMultipleOutgoingFlowsConverterTest extends AbstractConverterTest {

  @Test
  public void doubleConversionValidation() throws Exception {
    BpmnModel bpmnModel = readJsonFile();
    validateModel(bpmnModel);
    bpmnModel = convertToJsonAndBack(bpmnModel);
    // System.out.println("xml " + new String(new
    // BpmnXMLConverter().convertToXML(bpmnModel), "utf-8"));
    validateModel(bpmnModel);
  }

  private void validateModel(BpmnModel model) {
    FlowElement flowElement = model.getMainProcess().getFlowElement("parallel1", true);
    assertNotNull(flowElement);
    assertTrue(flowElement instanceof ParallelGateway);
    ParallelGateway gateway = (ParallelGateway) flowElement;
    List<SequenceFlow> sequenceFlows = gateway.getOutgoingFlows();
    assertTrue(sequenceFlows.size() == 3);
    assertTrue("sid-B9EE4ECE-BF72-4C25-B768-8295906E5CF8".equals(sequenceFlows.get(0).getId()) || "sid-D2491B73-0382-4EC2-AAAC-C8FD129E4CBE".equals(sequenceFlows.get(0).getId())
        || "sid-7036D56C-E8EF-493B-ADEC-57EED4C6CE1F".equals(sequenceFlows.get(0).getId()));
    assertTrue("sid-B9EE4ECE-BF72-4C25-B768-8295906E5CF8".equals(sequenceFlows.get(1).getId()) || "sid-D2491B73-0382-4EC2-AAAC-C8FD129E4CBE".equals(sequenceFlows.get(1).getId())
        || "sid-7036D56C-E8EF-493B-ADEC-57EED4C6CE1F".equals(sequenceFlows.get(1).getId()));
    assertTrue("sid-B9EE4ECE-BF72-4C25-B768-8295906E5CF8".equals(sequenceFlows.get(2).getId()) || "sid-D2491B73-0382-4EC2-AAAC-C8FD129E4CBE".equals(sequenceFlows.get(2).getId())
        || "sid-7036D56C-E8EF-493B-ADEC-57EED4C6CE1F".equals(sequenceFlows.get(2).getId()));
    assertTrue("parallel1".equals(sequenceFlows.get(0).getSourceRef()));
    assertTrue("parallel1".equals(sequenceFlows.get(1).getSourceRef()));
    assertTrue("parallel1".equals(sequenceFlows.get(2).getSourceRef()));
    flowElement = model.getMainProcess().getFlowElement("parallel2", true);
    assertNotNull(flowElement);
    assertTrue(flowElement instanceof ParallelGateway);
    gateway = (ParallelGateway) flowElement;
    sequenceFlows = gateway.getIncomingFlows();
    assertTrue(sequenceFlows.size() == 3);
    assertTrue("sid-4C19E041-42FA-485D-9D09-D47CCD9DB270".equals(sequenceFlows.get(0).getId()) || "sid-05A991A6-0296-4867-ACBA-EF9EEC68FB8A".equals(sequenceFlows.get(0).getId())
        || "sid-C546AC84-379D-4094-9DC3-548593F2EA0D".equals(sequenceFlows.get(0).getId()));
    assertTrue("sid-4C19E041-42FA-485D-9D09-D47CCD9DB270".equals(sequenceFlows.get(1).getId()) || "sid-05A991A6-0296-4867-ACBA-EF9EEC68FB8A".equals(sequenceFlows.get(1).getId())
        || "sid-C546AC84-379D-4094-9DC3-548593F2EA0D".equals(sequenceFlows.get(1).getId()));
    assertTrue("sid-4C19E041-42FA-485D-9D09-D47CCD9DB270".equals(sequenceFlows.get(2).getId()) || "sid-05A991A6-0296-4867-ACBA-EF9EEC68FB8A".equals(sequenceFlows.get(2).getId())
        || "sid-C546AC84-379D-4094-9DC3-548593F2EA0D".equals(sequenceFlows.get(2).getId()));
    assertTrue("parallel2".equals(sequenceFlows.get(0).getTargetRef()));
    assertTrue("parallel2".equals(sequenceFlows.get(1).getTargetRef()));
    assertTrue("parallel2".equals(sequenceFlows.get(2).getTargetRef()));
  }

  @Override
protected String getResource() {
    return "test.flownodemultipleoutgoingflowsmodel.json";
  }

}

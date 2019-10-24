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
package org.activiti.validation.validator.impl;

import java.util.List;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.FlowNode;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.validation.ValidationError;
import org.activiti.validation.validator.Problems;
import org.activiti.validation.validator.ValidatorImpl;

/**

 */
public class DiagramInterchangeInfoValidator extends ValidatorImpl {

  @Override
  public void validate(BpmnModel bpmnModel, List<ValidationError> errors) {
    if (!bpmnModel.getLocationMap().isEmpty()) {

      // Location map
	bpmnModel.getLocationMap().keySet().forEach(bpmnReference -> {
        if (bpmnModel.getFlowElement(bpmnReference) == null) {
          boolean condition = bpmnModel.getArtifact(bpmnReference) == null && bpmnModel.getPool(bpmnReference) == null && bpmnModel.getLane(bpmnReference) == null;
			// check if it's a Pool or Lane, then DI is ok
		// ACT-1625: don't warn when artifacts are referenced from
          // DI
          if (condition) {
		  addWarning(errors, Problems.DI_INVALID_REFERENCE, null, bpmnModel.getFlowElement(bpmnReference), "Invalid reference in diagram interchange definition: could not find " + bpmnReference);
		}
        } else if (!(bpmnModel.getFlowElement(bpmnReference) instanceof FlowNode)) {
          addWarning(errors, Problems.DI_DOES_NOT_REFERENCE_FLOWNODE, null, bpmnModel.getFlowElement(bpmnReference), new StringBuilder().append("Invalid reference in diagram interchange definition: ").append(bpmnReference).append(" does not reference a flow node").toString());
        }
      });

    }

    if (!bpmnModel.getFlowLocationMap().isEmpty()) {
      // flowlocation map
	bpmnModel.getFlowLocationMap().keySet().forEach(bpmnReference -> {
        if (bpmnModel.getFlowElement(bpmnReference) == null) {
          // ACT-1625: don't warn when artifacts are referenced from
          // DI
          if (bpmnModel.getArtifact(bpmnReference) == null) {
            addWarning(errors, Problems.DI_INVALID_REFERENCE, null, bpmnModel.getFlowElement(bpmnReference), "Invalid reference in diagram interchange definition: could not find " + bpmnReference);
          }
        } else if (!(bpmnModel.getFlowElement(bpmnReference) instanceof SequenceFlow)) {
          addWarning(errors, Problems.DI_DOES_NOT_REFERENCE_SEQ_FLOW, null, bpmnModel.getFlowElement(bpmnReference), new StringBuilder().append("Invalid reference in diagram interchange definition: ").append(bpmnReference).append(" does not reference a sequence flow").toString());
        }
      });
    }
  }
}

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
package org.activiti.bpmn.converter.parser;

import javax.xml.stream.XMLStreamReader;

import org.activiti.bpmn.constants.BpmnXMLConstants;
import org.activiti.bpmn.converter.util.BpmnXMLUtil;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.ItemDefinition;
import org.apache.commons.lang3.StringUtils;

/**

 */
public class ItemDefinitionParser implements BpmnXMLConstants {

  public void parse(XMLStreamReader xtr, BpmnModel model) throws Exception {
    if (!StringUtils.isNotEmpty(xtr.getAttributeValue(null, ATTRIBUTE_ID))) {
		return;
	}
	String itemDefinitionId = new StringBuilder().append(model.getTargetNamespace()).append(":").append(xtr.getAttributeValue(null, ATTRIBUTE_ID)).toString();
	String structureRef = xtr.getAttributeValue(null, ATTRIBUTE_STRUCTURE_REF);
	if (StringUtils.isNotEmpty(structureRef)) {
        ItemDefinition item = new ItemDefinition();
        item.setId(itemDefinitionId);
        BpmnXMLUtil.addXMLLocation(item, xtr);

        int indexOfP = structureRef.indexOf(':');
        if (indexOfP != -1) {
          String prefix = structureRef.substring(0, indexOfP);
          String resolvedNamespace = model.getNamespace(prefix);
          structureRef = new StringBuilder().append(resolvedNamespace).append(":").append(structureRef.substring(indexOfP + 1)).toString();
        } else {
          structureRef = new StringBuilder().append(model.getTargetNamespace()).append(":").append(structureRef).toString();
        }

        item.setStructureRef(structureRef);
        item.setItemKind(xtr.getAttributeValue(null, ATTRIBUTE_ITEM_KIND));
        BpmnXMLUtil.parseChildElements(ELEMENT_ITEM_DEFINITION, item, xtr, model);
        model.addItemDefinition(itemDefinitionId, item);
      }
  }
}

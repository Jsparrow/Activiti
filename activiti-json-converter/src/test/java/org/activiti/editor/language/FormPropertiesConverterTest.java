package org.activiti.editor.language;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.FormProperty;
import org.activiti.bpmn.model.FormValue;
import org.activiti.bpmn.model.StartEvent;
import org.activiti.bpmn.model.UserTask;
import org.junit.Test;

public class FormPropertiesConverterTest extends AbstractConverterTest {

  @Test
  public void convertJsonToModel() throws Exception {
    BpmnModel bpmnModel = readJsonFile();
    validateModel(bpmnModel);
  }
  
  @Test 
  public void doubleConversionValidation() throws Exception {
    BpmnModel bpmnModel = readJsonFile();
    validateModel(bpmnModel);
    bpmnModel = convertToJsonAndBack(bpmnModel);
    validateModel(bpmnModel);
  }
  
  @Override
protected String getResource() {
    return "test.formpropertiesmodel.json";
  }
  
  private void validateModel(BpmnModel model) {
    assertEquals("formPropertiesProcess", model.getMainProcess().getId());
    assertEquals("User registration", model.getMainProcess().getName());
    assertEquals(true, model.getMainProcess().isExecutable());
    
    FlowElement startFlowElement = model.getMainProcess().getFlowElement("startNode", true);
    assertNotNull(startFlowElement);
    assertTrue(startFlowElement instanceof StartEvent);
    StartEvent startEvent = (StartEvent) startFlowElement;

    List<FormProperty> formProperties = startEvent.getFormProperties();

    assertNotNull(formProperties);
    assertEquals("Invalid form properties list: ", 8 ,formProperties.size());

    formProperties.forEach(formProperty -> {
      if ("new_property_1".equals(formProperty.getId())) {
        checkFormProperty(formProperty, "v000", true, false, false);
      } else if ("new_property_2".equals(formProperty.getId())) {
        checkFormProperty(formProperty, "v001", true, false, true);
      } else if ("new_property_3".equals(formProperty.getId())) {
        checkFormProperty(formProperty, "v010", true, true, false);
      } else if ("new_property_4".equals(formProperty.getId())) {
        checkFormProperty(formProperty, "v011", true, true, true);
      } else if ("new_property_5".equals(formProperty.getId())) {
        checkFormProperty(formProperty, "v100", true, false, false);

        List<Map<String, Object>> formValues = new ArrayList<>();
        formProperty.getFormValues().forEach(formValue -> {
          Map<String, Object> formValueMap = new HashMap<>();
          formValueMap.put("id", formValue.getId());
          formValueMap.put("name", formValue.getName());
          formValues.add(formValueMap);
        });
        checkFormPropertyFormValues(formValues);

      } else if ("new_property_6".equals(formProperty.getId())) {
        checkFormProperty(formProperty, "v101", true, false, true);
      } else if ("new_property_7".equals(formProperty.getId())) {
        checkFormProperty(formProperty, "v110", true, true, false);
      } else if ("new_property_8".equals(formProperty.getId())) {
        checkFormProperty(formProperty, "v111", true, true, true);
      } else {
        fail("unexpected form property id " + formProperty.getId());
      }
    });
    
    FlowElement userFlowElement = model.getMainProcess().getFlowElement("userTask", true);
    assertNotNull(userFlowElement);
    assertTrue(userFlowElement instanceof UserTask);
    UserTask userTask = (UserTask) userFlowElement;

    formProperties = userTask.getFormProperties();

    assertNotNull(formProperties);
    assertEquals("Invalid form properties list: ", 8 ,formProperties.size());

    formProperties.forEach(formProperty -> {
      if ("new_property_1".equals(formProperty.getId())) {
        checkFormProperty(formProperty, "v000", false, false, false);
      } else if ("new_property_2".equals(formProperty.getId())) {
        checkFormProperty(formProperty, "v001", false, false, true);
      } else if ("new_property_3".equals(formProperty.getId())) {
        checkFormProperty(formProperty, "v010", false, true, false);
      } else if ("new_property_4".equals(formProperty.getId())) {
        checkFormProperty(formProperty, "v011", false, true, true);
      } else if ("new_property_5".equals(formProperty.getId())) {
        checkFormProperty(formProperty, "v100", true, false, false);

        List<Map<String, Object>> formValues = new ArrayList<>();
        formProperty.getFormValues().forEach(formValue -> {
          Map<String, Object> formValueMap = new HashMap<>();
          formValueMap.put("id", formValue.getId());
          formValueMap.put("name", formValue.getName());
          formValues.add(formValueMap);
        });
        checkFormPropertyFormValues(formValues);

      } else if ("new_property_6".equals(formProperty.getId())) {
        checkFormProperty(formProperty, "v101", true, false, true);
      } else if ("new_property_7".equals(formProperty.getId())) {
        checkFormProperty(formProperty, "v110", true, true, false);
      } else if ("new_property_8".equals(formProperty.getId())) {
        checkFormProperty(formProperty, "v111", true, true, true);
      } else {
        fail("unexpected form property id " + formProperty.getId());
      }
    });
    
  }
  
  private void checkFormProperty(FormProperty formProperty, String name, boolean shouldBeRequired, boolean shouldBeReadable, boolean shouldBeWritable) {
    assertEquals(name, formProperty.getName());
    assertEquals(shouldBeRequired, formProperty.isRequired());
    assertEquals(shouldBeReadable, formProperty.isReadable());
    assertEquals(shouldBeWritable, formProperty.isWriteable());
  }
  
  private void checkFormPropertyFormValues(List<Map<String, Object>> formValues) {
    List<Map<String, Object>> expectedFormValues = new ArrayList<>();
    Map<String, Object> formValue1 = new HashMap<>();
    formValue1.put("id", "value1");
    formValue1.put("name", "Value 1");
    Map<String, Object> formValue2 = new HashMap<>();
    formValue2.put("id", "value2");
    formValue2.put("name", "Value 2");

    Map<String, Object> formValue3 = new HashMap<>();
    formValue3.put("id", "value3");
    formValue3.put("name", "Value 3");

    Map<String, Object> formValue4 = new HashMap<>();
    formValue4.put("id", "value4");
    formValue4.put("name", "Value 4");

    expectedFormValues.add(formValue1);
    expectedFormValues.add(formValue2);
    expectedFormValues.add(formValue3);
    expectedFormValues.add(formValue4);

    assertEquals(expectedFormValues, formValues);
  }
}

package org.activiti.bpmn.model;

public class StringDataObject extends ValuedDataObject {

  @Override
public void setValue(Object value) {
    this.value = value.toString();
  }

  @Override
public StringDataObject clone() {
    StringDataObject clone = new StringDataObject();
    clone.setValues(this);
    return clone;
  }
}

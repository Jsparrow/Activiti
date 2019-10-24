package org.activiti.bpmn.model;

public class BooleanDataObject extends ValuedDataObject {

  @Override
public void setValue(Object value) {
    this.value = Boolean.valueOf(value.toString());
  }

  @Override
public BooleanDataObject clone() {
    BooleanDataObject clone = new BooleanDataObject();
    clone.setValues(this);
    return clone;
  }
}

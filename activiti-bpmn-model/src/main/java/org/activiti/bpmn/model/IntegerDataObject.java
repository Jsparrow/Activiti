package org.activiti.bpmn.model;

public class IntegerDataObject extends ValuedDataObject {

  @Override
public void setValue(Object value) {
    this.value = Integer.valueOf(value.toString());
  }

  @Override
public IntegerDataObject clone() {
    IntegerDataObject clone = new IntegerDataObject();
    clone.setValues(this);
    return clone;
  }
}

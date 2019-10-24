package org.activiti.bpmn.model;

public class DoubleDataObject extends ValuedDataObject {

  @Override
public void setValue(Object value) {
    this.value = Double.valueOf(value.toString());
  }

  @Override
public DoubleDataObject clone() {
    DoubleDataObject clone = new DoubleDataObject();
    clone.setValues(this);
    return clone;
  }
}

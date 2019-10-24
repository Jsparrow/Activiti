package org.activiti.bpmn.model;

public class LongDataObject extends ValuedDataObject {

  @Override
public void setValue(Object value) {
    this.value = Long.valueOf(value.toString());
  }

  @Override
public LongDataObject clone() {
    LongDataObject clone = new LongDataObject();
    clone.setValues(this);
    return clone;
  }
}

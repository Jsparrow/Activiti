package org.activiti.bpmn.model;

import java.util.ArrayList;
import java.util.List;

public class IOSpecification extends BaseElement {

  protected List<DataSpec> dataInputs = new ArrayList<>();
  protected List<DataSpec> dataOutputs = new ArrayList<>();
  protected List<String> dataInputRefs = new ArrayList<>();
  protected List<String> dataOutputRefs = new ArrayList<>();

  public List<DataSpec> getDataInputs() {
    return dataInputs;
  }

  public void setDataInputs(List<DataSpec> dataInputs) {
    this.dataInputs = dataInputs;
  }

  public List<DataSpec> getDataOutputs() {
    return dataOutputs;
  }

  public void setDataOutputs(List<DataSpec> dataOutputs) {
    this.dataOutputs = dataOutputs;
  }

  public List<String> getDataInputRefs() {
    return dataInputRefs;
  }

  public void setDataInputRefs(List<String> dataInputRefs) {
    this.dataInputRefs = dataInputRefs;
  }

  public List<String> getDataOutputRefs() {
    return dataOutputRefs;
  }

  public void setDataOutputRefs(List<String> dataOutputRefs) {
    this.dataOutputRefs = dataOutputRefs;
  }

  @Override
public IOSpecification clone() {
    IOSpecification clone = new IOSpecification();
    clone.setValues(this);
    return clone;
  }

  public void setValues(IOSpecification otherSpec) {
    dataInputs = new ArrayList<>();
    if (otherSpec.getDataInputs() != null && !otherSpec.getDataInputs().isEmpty()) {
      otherSpec.getDataInputs().forEach(dataSpec -> dataInputs.add(dataSpec.clone()));
    }

    dataOutputs = new ArrayList<>();
    if (otherSpec.getDataOutputs() != null && !otherSpec.getDataOutputs().isEmpty()) {
      otherSpec.getDataOutputs().forEach(dataSpec -> dataOutputs.add(dataSpec.clone()));
    }

    dataInputRefs = new ArrayList<>(otherSpec.getDataInputRefs());
    dataOutputRefs = new ArrayList<>(otherSpec.getDataOutputRefs());
  }
}

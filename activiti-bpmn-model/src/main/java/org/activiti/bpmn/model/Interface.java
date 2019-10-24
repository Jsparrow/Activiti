package org.activiti.bpmn.model;

import java.util.ArrayList;
import java.util.List;

public class Interface extends BaseElement {

  protected String name;
  protected String implementationRef;
  protected List<Operation> operations = new ArrayList<>();

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getImplementationRef() {
    return implementationRef;
  }

  public void setImplementationRef(String implementationRef) {
    this.implementationRef = implementationRef;
  }

  public List<Operation> getOperations() {
    return operations;
  }

  public void setOperations(List<Operation> operations) {
    this.operations = operations;
  }

  @Override
public Interface clone() {
    Interface clone = new Interface();
    clone.setValues(this);
    return clone;
  }

  public void setValues(Interface otherElement) {
    super.setValues(otherElement);
    setName(otherElement.getName());
    setImplementationRef(otherElement.getImplementationRef());

    operations = new ArrayList<>();
    if (otherElement.getOperations() != null && !otherElement.getOperations().isEmpty()) {
      otherElement.getOperations().forEach(operation -> operations.add(operation.clone()));
    }
  }
}

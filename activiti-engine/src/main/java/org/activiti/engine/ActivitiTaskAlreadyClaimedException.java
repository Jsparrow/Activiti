package org.activiti.engine;

/**
 * This exception is thrown when you try to claim a task that is already claimed by someone else.
 * 


 */
public class ActivitiTaskAlreadyClaimedException extends ActivitiException {

  private static final long serialVersionUID = 1L;

  /** the id of the task that is already claimed */
  private String taskId;

  /** the assignee of the task that is already claimed */
  private String taskAssignee;

  public ActivitiTaskAlreadyClaimedException(String taskId, String taskAssignee) {
    super(new StringBuilder().append("Task '").append(taskId).append("' is already claimed by someone else.").toString());
    this.taskId = taskId;
    this.taskAssignee = taskAssignee;
  }

  public String getTaskId() {
    return this.taskId;
  }

  public String getTaskAssignee() {
    return this.taskAssignee;
  }

}

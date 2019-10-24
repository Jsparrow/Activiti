package org.activiti.engine.test.api.task;

import java.util.HashSet;
import java.util.Set;

import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;
import org.activiti.engine.task.IdentityLink;

public class DelegateTaskTestTaskListener implements TaskListener {

  public static final String VARNAME_CANDIDATE_USERS = "candidateUsers";
  public static final String VARNAME_CANDIDATE_GROUPS = "candidateGroups";

  @Override
public void notify(DelegateTask delegateTask) {
    Set<IdentityLink> candidates = delegateTask.getCandidates();
    Set<String> candidateUsers = new HashSet<>();
    Set<String> candidateGroups = new HashSet<>();
    candidates.forEach(candidate -> {
      if (candidate.getUserId() != null) {
        candidateUsers.add(candidate.getUserId());
      } else if (candidate.getGroupId() != null) {
        candidateGroups.add(candidate.getGroupId());
      }
    });
    delegateTask.setVariable(VARNAME_CANDIDATE_USERS, candidateUsers);
    delegateTask.setVariable(VARNAME_CANDIDATE_GROUPS, candidateGroups);
  }

}

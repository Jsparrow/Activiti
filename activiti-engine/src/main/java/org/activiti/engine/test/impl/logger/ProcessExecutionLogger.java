/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.activiti.engine.test.impl.logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.test.impl.logger.DebugInfoExecutionTree.DebugInfoExecutionTreeNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**

 */
public class ProcessExecutionLogger {

  private static final Logger logger = LoggerFactory.getLogger(ProcessExecutionLogger.class);

  protected Map<String, List<DebugInfo>> debugInfoMap = new HashMap<>();

  // To avoid going to the db (and thus influencing process execution/tests), we store all encountered executions here,
  // to build up a tree representation with that information afterwards.
  protected Map<String, ExecutionEntity> createdExecutions = new HashMap<>();
  protected Map<String, ExecutionEntity> deletedExecutions = new HashMap<>();

  public ProcessExecutionLogger() {

  }

  public void addDebugInfo(AbstractDebugInfo debugInfo) {
    addDebugInfo(debugInfo, false);
  }

  public synchronized void addDebugInfo(AbstractDebugInfo debugInfo, boolean generateExecutionTreeRepresentation) {

    // Store debug info
    String threadName = Thread.currentThread().getName();
    debugInfoMap.putIfAbsent(threadName, new ArrayList<>());
    debugInfoMap.get(threadName).add(debugInfo);

    // Generate execution tree
    if (generateExecutionTreeRepresentation) {
      debugInfo.setExecutionTrees(generateExecutionTrees());
    }
  }

  protected List<DebugInfoExecutionTree> generateExecutionTrees() {

    // Gather information
    List<ExecutionEntity> processInstances = new ArrayList<>();
    Map<String, List<ExecutionEntity>> parentMapping = new HashMap<>();

    createdExecutions.values().stream().filter(executionEntity -> !deletedExecutions.containsKey(executionEntity.getId())).forEach(executionEntity -> {
        if (executionEntity.getParentId() == null) {
          processInstances.add(executionEntity);
        } else {
          parentMapping.putIfAbsent(executionEntity.getParentId(), new ArrayList<>());
          parentMapping.get(executionEntity.getParentId()).add(executionEntity);
        }
      });

    // Build tree representation
    List<DebugInfoExecutionTree> executionTrees = new ArrayList<>();
    processInstances.forEach(processInstance -> {

      DebugInfoExecutionTree executionTree = new DebugInfoExecutionTree();
      executionTrees.add(executionTree);

      DebugInfoExecutionTreeNode rootNode = new DebugInfoExecutionTreeNode();
      executionTree.setProcessInstance(rootNode);
      rootNode.setId(processInstance.getId());

      internalPopulateExecutionTree(rootNode, parentMapping);
    });

    return executionTrees;
  }

  protected void internalPopulateExecutionTree(DebugInfoExecutionTreeNode parentNode, Map<String, List<ExecutionEntity>> parentMapping) {
    if (parentMapping.containsKey(parentNode.getId())) {
      parentMapping.get(parentNode.getId()).forEach(childExecutionEntity -> {
        DebugInfoExecutionTreeNode childNode = new DebugInfoExecutionTreeNode();
        childNode.setId(childExecutionEntity.getId());
        childNode.setActivityId(childExecutionEntity.getCurrentFlowElement() != null ? childExecutionEntity.getCurrentFlowElement().getId() : null);
        childNode.setActivityName(childExecutionEntity.getCurrentFlowElement() != null ? childExecutionEntity.getCurrentFlowElement().getName() : null);
        childNode.setProcessDefinitionId(childExecutionEntity.getProcessDefinitionId());

        childNode.setParentNode(childNode);
        parentNode.getChildNodes().add(childNode);

        internalPopulateExecutionTree(childNode, parentMapping);
      });
    }
  }

  public void logDebugInfo() {
    logDebugInfo(false);
  }

  public void logDebugInfo(boolean clearAfterLogging) {

    logger.info("--------------------------------");
    logger.info("CommandInvoker debug information");
    logger.info("--------------------------------");
    debugInfoMap.keySet().forEach(threadName -> {

      logger.info("");
      logger.info(new StringBuilder().append("Thread '").append(threadName).append("':").toString());
      logger.info("");

      debugInfoMap.get(threadName).forEach(debugInfo -> debugInfo.printOut(logger));

    });

    logger.info("");

    if (clearAfterLogging) {
      clear();
    }
  }

  public void clear() {
    debugInfoMap.clear();
    createdExecutions.clear();
  }

  public void executionCreated(ExecutionEntity executionEntity) {
    createdExecutions.put(executionEntity.getId(), executionEntity);
  }

  public void executionDeleted(ExecutionEntity executionEntity) {
    deletedExecutions.put(executionEntity.getId(), executionEntity);
  }

}

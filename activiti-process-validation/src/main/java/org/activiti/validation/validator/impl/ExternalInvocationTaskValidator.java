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
package org.activiti.validation.validator.impl;

import java.util.List;

import org.activiti.bpmn.model.FieldExtension;
import org.activiti.bpmn.model.TaskWithFieldExtensions;
import org.activiti.validation.ValidationError;
import org.activiti.validation.validator.Problems;
import org.activiti.validation.validator.ProcessLevelValidator;

public abstract class ExternalInvocationTaskValidator extends ProcessLevelValidator {

  protected void validateFieldDeclarationsForEmail(org.activiti.bpmn.model.Process process, TaskWithFieldExtensions task, List<FieldExtension> fieldExtensions, List<ValidationError> errors) {
    boolean toDefined = false;
    boolean textOrHtmlDefined = false;

    for (FieldExtension fieldExtension : fieldExtensions) {
      if ("to".equals(fieldExtension.getFieldName())) {
        toDefined = true;
      }
      if ("html".equals(fieldExtension.getFieldName())) {
        textOrHtmlDefined = true;
      }
      if ("htmlVar".equals(fieldExtension.getFieldName())) {
        textOrHtmlDefined = true;
      }
      if ("text".equals(fieldExtension.getFieldName())) {
        textOrHtmlDefined = true;
      }
      if ("textVar".equals(fieldExtension.getFieldName())) {
        textOrHtmlDefined = true;
      }
    }

    if (!toDefined) {
      addError(errors, Problems.MAIL_TASK_NO_RECIPIENT, process, task, "No recipient is defined on the mail activity");
    }
    if (!textOrHtmlDefined) {
      addError(errors, Problems.MAIL_TASK_NO_CONTENT, process, task, "Text, html, textVar or htmlVar field should be provided");
    }
  }

  protected void validateFieldDeclarationsForShell(org.activiti.bpmn.model.Process process, TaskWithFieldExtensions task, List<FieldExtension> fieldExtensions, List<ValidationError> errors) {
    boolean shellCommandDefined = false;

    for (FieldExtension fieldExtension : fieldExtensions) {
      String fieldName = fieldExtension.getFieldName();
      String fieldValue = fieldExtension.getStringValue();

      if ("command".equals(fieldName)) {
        shellCommandDefined = true;
      }

      if (("wait".equals(fieldName) || "redirectError".equals(fieldName) || "cleanEnv".equals(fieldName)) && !"true".equals(fieldValue.toLowerCase()) && !"false".equals(fieldValue.toLowerCase())) {
        addError(errors, Problems.SHELL_TASK_INVALID_PARAM, process, task, "Undefined parameter value for shell field");
      }

    }

    if (!shellCommandDefined) {
      addError(errors, Problems.SHELL_TASK_NO_COMMAND, process, task, "No shell command is defined on the shell activity");
    }
  }
  
  protected void validateFieldDeclarationsForDmn(org.activiti.bpmn.model.Process process, TaskWithFieldExtensions task, List<FieldExtension> fieldExtensions, List<ValidationError> errors) {
    boolean keyDefined = false;

    for (FieldExtension fieldExtension : fieldExtensions) {
      String fieldName = fieldExtension.getFieldName();
      String fieldValue = fieldExtension.getStringValue();

      if ("decisionTableReferenceKey".equals(fieldName) && fieldValue != null && fieldValue.length() > 0) {
        keyDefined = true;
      }
    }

    if (!keyDefined) {
      addError(errors, Problems.DMN_TASK_NO_KEY, process, task, "No decision table reference key is defined on the dmn activity");
    }
  }

}

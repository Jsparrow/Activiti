package org.activiti.spring.process.variable;

import java.util.Map;

import org.activiti.engine.ActivitiException;
import org.activiti.spring.process.model.VariableDefinition;
import org.activiti.spring.process.variable.types.VariableType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Used to read variable values as per their expected types from the proc extension json
 */
public class VariableParsingService {

    private static final Logger logger = LoggerFactory.getLogger(VariableParsingService.class);
	private Map<String, VariableType> variableTypeMap;

	public VariableParsingService(Map<String, VariableType> variableTypeMap) {
        this.variableTypeMap = variableTypeMap;
    }

	public Object parse(VariableDefinition variableDefinition){


        if (variableDefinition.getType() == null) {
			return variableDefinition.getValue();
		}
		VariableType type = variableTypeMap.get(variableDefinition.getType());
		return type.parseFromValue(variableDefinition.getValue());
    }

}

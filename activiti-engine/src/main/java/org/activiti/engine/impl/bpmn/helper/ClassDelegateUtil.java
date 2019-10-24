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
package org.activiti.engine.impl.bpmn.helper;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.impl.bpmn.parser.FieldDeclaration;
import org.activiti.engine.impl.util.ReflectUtil;

/**

 */
public class ClassDelegateUtil {

  public static Object instantiateDelegate(Class<?> clazz, List<FieldDeclaration> fieldDeclarations) {
    return instantiateDelegate(clazz.getName(), fieldDeclarations);
  }

  public static Object instantiateDelegate(String className, List<FieldDeclaration> fieldDeclarations) {
    Object object = ReflectUtil.instantiate(className);
    applyFieldDeclaration(fieldDeclarations, object);
    return object;
  }

  public static void applyFieldDeclaration(List<FieldDeclaration> fieldDeclarations, Object target) {
    if (fieldDeclarations != null) {
      fieldDeclarations.forEach(declaration -> applyFieldDeclaration(declaration, target));
    }
  }

  public static void applyFieldDeclaration(FieldDeclaration declaration, Object target) {
    Method setterMethod = ReflectUtil.getSetter(declaration.getName(), target.getClass(), declaration.getValue().getClass());

    if (setterMethod != null) {
      try {
        setterMethod.invoke(target, declaration.getValue());
      } catch (IllegalArgumentException e) {
        throw new ActivitiException(new StringBuilder().append("Error while invoking '").append(declaration.getName()).append("' on class ").append(target.getClass().getName()).toString(), e);
      } catch (IllegalAccessException e) {
        throw new ActivitiException(new StringBuilder().append("Illegal access when calling '").append(declaration.getName()).append("' on class ").append(target.getClass().getName()).toString(), e);
      } catch (InvocationTargetException e) {
        throw new ActivitiException(new StringBuilder().append("Exception while invoking '").append(declaration.getName()).append("' on class ").append(target.getClass().getName()).toString(), e);
      }
    } else {
      Field field = ReflectUtil.getField(declaration.getName(), target);
      if (field == null) {
        throw new ActivitiIllegalArgumentException(new StringBuilder().append("Field definition uses non-existing field '").append(declaration.getName()).append("' on class ").append(target.getClass().getName()).toString());
      }
      // Check if the delegate field's type is correct
      if (!fieldTypeCompatible(declaration, field)) {
        throw new ActivitiIllegalArgumentException(new StringBuilder().append("Incompatible type set on field declaration '").append(declaration.getName()).append("' for class ").append(target.getClass().getName()).append(". Declared value has type ").append(declaration.getValue().getClass().getName())
				.append(", while expecting ").append(field.getType().getName()).toString());
      }
      ReflectUtil.setField(field, target, declaration.getValue());
    }
  }

  public static boolean fieldTypeCompatible(FieldDeclaration declaration, Field field) {
    if (declaration.getValue() != null) {
      return field.getType().isAssignableFrom(declaration.getValue().getClass());
    } else {
      // Null can be set any field type
      return true;
    }
  }

}

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

package org.activiti.engine.impl.el;

import java.beans.FeatureDescriptor;
import java.util.Iterator;
import java.util.Map;

import javax.el.ELContext;
import javax.el.ELResolver;

import org.activiti.engine.ActivitiException;

/**
 * An {@link ELResolver} that exposed object values in the map, under the name of the entry's key. The values in the map are only returned when requested property has no 'base', meaning it's a
 * root-object.
 * 

 */
public class ReadOnlyMapELResolver extends ELResolver {

  protected Map<Object, Object> wrappedMap;

  public ReadOnlyMapELResolver(Map<Object, Object> map) {
    this.wrappedMap = map;
  }

  @Override
public Object getValue(ELContext context, Object base, Object property) {
    boolean condition = base == null && wrappedMap.containsKey(property);
	if (condition) {
        context.setPropertyResolved(true);
        return wrappedMap.get(property);
      }
    return null;
  }

  @Override
public boolean isReadOnly(ELContext context, Object base, Object property) {
    return true;
  }

  @Override
public void setValue(ELContext context, Object base, Object property, Object value) {
    boolean condition = base == null && wrappedMap.containsKey(property);
	if (condition) {
        throw new ActivitiException(new StringBuilder().append("Cannot set value of '").append(property).append("', it's readonly!").toString());
      }
  }

  @Override
public Class<?> getCommonPropertyType(ELContext context, Object arg) {
    return Object.class;
  }

  @Override
public Iterator<FeatureDescriptor> getFeatureDescriptors(ELContext context, Object arg) {
    return null;
  }

  @Override
public Class<?> getType(ELContext context, Object arg1, Object arg2) {
    return Object.class;
  }
}

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
package org.activiti.engine.impl.transformer;

import org.activiti.engine.ActivitiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Transformer is responsible of transforming an object into a different object
 * 

 */
public abstract class AbstractTransformer implements Transformer {

  private static final Logger logger = LoggerFactory.getLogger(AbstractTransformer.class);

/**
   * {@inheritDoc}
   */
  @Override
public Object transform(Object anObject) {
    try {
      return this.primTransform(anObject);
    } catch (Exception e) {

      logger.error(e.getMessage(), e);
	throw new ActivitiException(new StringBuilder().append("Error while executing transformation from object: ").append(anObject).append(" using transformer ").append(this).toString());
    }
  }

  /**
   * Transforms anObject into a different object
   * 
   * @param anObject
   *          the object to be transformed
   * @return the transformed object
   */
  protected abstract Object primTransform(Object anObject) throws Exception;
}

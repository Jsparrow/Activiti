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

package org.activiti.engine.test.history;

import java.io.Serializable;

/**

 */
public class SerializableVariable implements Serializable {

  private static final long serialVersionUID = 1L;

  public String text;

  public SerializableVariable(String text) {
    this.text = text;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((text == null) ? 0 : text.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
		return true;
	}
    if (obj == null) {
		return false;
	}
    if (getClass() != obj.getClass()) {
		return false;
	}
    SerializableVariable other = (SerializableVariable) obj;
    if (text == null) {
      if (other.text != null) {
		return false;
	}
    } else if (!text.equals(other.text)) {
		return false;
	}
    return true;
  }
}

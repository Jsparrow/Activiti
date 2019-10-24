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
package org.activiti.engine.impl.calendar;

import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.api.internal.Internal;

@Internal
public class MapBusinessCalendarManager implements BusinessCalendarManager {

  private final Map<String, BusinessCalendar> businessCalendars;
  
  public MapBusinessCalendarManager(){
    this.businessCalendars = new HashMap<>();
  }
  
  public MapBusinessCalendarManager(Map<String, BusinessCalendar> businessCalendars) {
    if (businessCalendars == null) {
      throw new IllegalArgumentException("businessCalendars can not be null");
    }
  
    this.businessCalendars = new HashMap<>(businessCalendars);
  }

  @Override
public BusinessCalendar getBusinessCalendar(String businessCalendarRef) {
    BusinessCalendar businessCalendar = businessCalendars.get(businessCalendarRef);
    if (businessCalendar == null) {
      throw new ActivitiException(new StringBuilder().append("Requested business calendar ").append(businessCalendarRef).append(" does not exist. Allowed calendars are ").append(this.businessCalendars.keySet()).append(".").toString());
    }
    return businessCalendar;
  }

  public BusinessCalendarManager addBusinessCalendar(String businessCalendarRef, BusinessCalendar businessCalendar) {
    businessCalendars.put(businessCalendarRef, businessCalendar);
    return this;
  }
}

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

package org.activiti.engine.impl.asyncexecutor.multitenant;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.activiti.engine.impl.asyncexecutor.AsyncExecutor;
import org.activiti.engine.impl.asyncexecutor.DefaultAsyncJobExecutor;
import org.activiti.engine.impl.asyncexecutor.JobManager;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.cfg.multitenant.TenantInfoHolder;
import org.activiti.engine.runtime.Job;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An {@link AsyncExecutor} that has one {@link AsyncExecutor} per tenant.
 * So each tenant has its own acquiring threads and it's own threadpool for executing jobs.
 * 

 */
public class ExecutorPerTenantAsyncExecutor implements TenantAwareAsyncExecutor {
  
  private static final Logger logger = LoggerFactory.getLogger(ExecutorPerTenantAsyncExecutor.class);
  
  protected TenantInfoHolder tenantInfoHolder;
  protected TenantAwareAsyncExecutorFactory tenantAwareAyncExecutorFactory;
  
  protected Map<String, AsyncExecutor> tenantExecutors = new HashMap<>();
  
  protected ProcessEngineConfigurationImpl processEngineConfiguration;
  protected boolean active;
  protected boolean autoActivate;
  
  public ExecutorPerTenantAsyncExecutor(TenantInfoHolder tenantInfoHolder) {
    this(tenantInfoHolder, null);
  }
  
  public ExecutorPerTenantAsyncExecutor(TenantInfoHolder tenantInfoHolder, TenantAwareAsyncExecutorFactory tenantAwareAyncExecutorFactory) {
    this.tenantInfoHolder = tenantInfoHolder;
    this.tenantAwareAyncExecutorFactory = tenantAwareAyncExecutorFactory;
  }
  
  @Override
  public Set<String> getTenantIds() {
    return tenantExecutors.keySet();
  }

  @Override
public void addTenantAsyncExecutor(String tenantId, boolean startExecutor) {
    AsyncExecutor tenantExecutor = null;
    
    if (tenantAwareAyncExecutorFactory == null) {
      tenantExecutor = new DefaultAsyncJobExecutor();
    } else {
      tenantExecutor = tenantAwareAyncExecutorFactory.createAsyncExecutor(tenantId);
    }

    tenantExecutor.setProcessEngineConfiguration(processEngineConfiguration);
    
    if (tenantExecutor instanceof DefaultAsyncJobExecutor) {
      DefaultAsyncJobExecutor defaultAsyncJobExecutor = (DefaultAsyncJobExecutor) tenantExecutor;
      defaultAsyncJobExecutor.setAsyncJobsDueRunnable(new TenantAwareAcquireAsyncJobsDueRunnable(defaultAsyncJobExecutor, tenantInfoHolder, tenantId));
      defaultAsyncJobExecutor.setTimerJobRunnable(new TenantAwareAcquireTimerJobsRunnable(defaultAsyncJobExecutor, tenantInfoHolder, tenantId));
      defaultAsyncJobExecutor.setExecuteAsyncRunnableFactory(new TenantAwareExecuteAsyncRunnableFactory(tenantInfoHolder, tenantId));
      defaultAsyncJobExecutor.setResetExpiredJobsRunnable(new TenantAwareResetExpiredJobsRunnable(defaultAsyncJobExecutor, tenantInfoHolder, tenantId));
    }
    
    tenantExecutors.put(tenantId, tenantExecutor);
    
    if (startExecutor) {
      tenantExecutor.start();
    }
  }
  
    @Override
    public void removeTenantAsyncExecutor(String tenantId) {
      shutdownTenantExecutor(tenantId);
      tenantExecutors.remove(tenantId);
    }
  
  protected AsyncExecutor determineAsyncExecutor() {
    return tenantExecutors.get(tenantInfoHolder.getCurrentTenantId());
  }

  @Override
public boolean executeAsyncJob(Job job) {
    return determineAsyncExecutor().executeAsyncJob(job);
  }

  public JobManager getJobManager() {
    // Should never be accessed on this class, should be accessed on the actual AsyncExecutor
    throw new UnsupportedOperationException(); 
  }
  
  @Override
  public void setProcessEngineConfiguration(ProcessEngineConfigurationImpl processEngineConfiguration) {
    this.processEngineConfiguration = processEngineConfiguration;
    tenantExecutors.values().forEach(asyncExecutor -> asyncExecutor.setProcessEngineConfiguration(processEngineConfiguration));
  }
  
  @Override
  public ProcessEngineConfigurationImpl getProcessEngineConfiguration() {
    throw new UnsupportedOperationException();
  }

  @Override
public boolean isAutoActivate() {
    return autoActivate;
  }

  @Override
public void setAutoActivate(boolean isAutoActivate) {
    autoActivate = isAutoActivate;
  }

  @Override
public boolean isActive() {
    return active;
  }

  @Override
public void start() {
    tenantExecutors.values().forEach(AsyncExecutor::start);
    active = true;
  }

  @Override
public synchronized void shutdown() {
    tenantExecutors.keySet().forEach(this::shutdownTenantExecutor);
    active = false;
  }
  
  protected void shutdownTenantExecutor(String tenantId) {
    logger.info("Shutting down async executor for tenant " + tenantId);
    tenantExecutors.get(tenantId).shutdown();
  }

  @Override
public String getLockOwner() {
    return determineAsyncExecutor().getLockOwner();
  }

  @Override
public int getTimerLockTimeInMillis() {
    return determineAsyncExecutor().getTimerLockTimeInMillis();
  }

  @Override
public void setTimerLockTimeInMillis(int lockTimeInMillis) {
    tenantExecutors.values().forEach(asyncExecutor -> asyncExecutor.setTimerLockTimeInMillis(lockTimeInMillis));
  }

  @Override
public int getAsyncJobLockTimeInMillis() {
    return determineAsyncExecutor().getAsyncJobLockTimeInMillis();
  }

  @Override
public void setAsyncJobLockTimeInMillis(int lockTimeInMillis) {
    tenantExecutors.values().forEach(asyncExecutor -> asyncExecutor.setAsyncJobLockTimeInMillis(lockTimeInMillis));
  }

  @Override
public int getDefaultTimerJobAcquireWaitTimeInMillis() {
    return determineAsyncExecutor().getDefaultTimerJobAcquireWaitTimeInMillis();
  }

  @Override
public void setDefaultTimerJobAcquireWaitTimeInMillis(int waitTimeInMillis) {
    tenantExecutors.values().forEach(asyncExecutor -> asyncExecutor.setDefaultTimerJobAcquireWaitTimeInMillis(waitTimeInMillis));
  }

  @Override
public int getDefaultAsyncJobAcquireWaitTimeInMillis() {
    return determineAsyncExecutor().getDefaultAsyncJobAcquireWaitTimeInMillis();
  }

  @Override
public void setDefaultAsyncJobAcquireWaitTimeInMillis(int waitTimeInMillis) {
    tenantExecutors.values().forEach(asyncExecutor -> asyncExecutor.setDefaultAsyncJobAcquireWaitTimeInMillis(waitTimeInMillis));
  }
  
  @Override
public int getDefaultQueueSizeFullWaitTimeInMillis() {
    return determineAsyncExecutor().getDefaultQueueSizeFullWaitTimeInMillis();
  }
  
  @Override
public void setDefaultQueueSizeFullWaitTimeInMillis(int defaultQueueSizeFullWaitTimeInMillis) {
    tenantExecutors.values().forEach(asyncExecutor -> asyncExecutor.setDefaultQueueSizeFullWaitTimeInMillis(defaultQueueSizeFullWaitTimeInMillis));
  }

  @Override
public int getMaxAsyncJobsDuePerAcquisition() {
    return determineAsyncExecutor().getMaxAsyncJobsDuePerAcquisition();
  }

  @Override
public void setMaxAsyncJobsDuePerAcquisition(int maxJobs) {
    tenantExecutors.values().forEach(asyncExecutor -> asyncExecutor.setMaxAsyncJobsDuePerAcquisition(maxJobs));
  }

  @Override
public int getMaxTimerJobsPerAcquisition() {
    return determineAsyncExecutor().getMaxTimerJobsPerAcquisition();
  }

  @Override
public void setMaxTimerJobsPerAcquisition(int maxJobs) {
    tenantExecutors.values().forEach(asyncExecutor -> asyncExecutor.setMaxTimerJobsPerAcquisition(maxJobs));
  }

  @Override
public int getRetryWaitTimeInMillis() {
    return determineAsyncExecutor().getRetryWaitTimeInMillis();
  }

  @Override
public void setRetryWaitTimeInMillis(int retryWaitTimeInMillis) {
    tenantExecutors.values().forEach(asyncExecutor -> asyncExecutor.setRetryWaitTimeInMillis(retryWaitTimeInMillis));
  }
  
  @Override
  public int getResetExpiredJobsInterval() {
    return determineAsyncExecutor().getResetExpiredJobsInterval();
  }
  
  @Override
  public void setResetExpiredJobsInterval(int resetExpiredJobsInterval) {
    tenantExecutors.values().forEach(asyncExecutor -> asyncExecutor.setResetExpiredJobsInterval(resetExpiredJobsInterval));
  }
  
  @Override
  public int getResetExpiredJobsPageSize() {
    return determineAsyncExecutor().getResetExpiredJobsPageSize();
  }
  
  @Override
  public void setResetExpiredJobsPageSize(int resetExpiredJobsPageSize) {
    tenantExecutors.values().forEach(asyncExecutor -> asyncExecutor.setResetExpiredJobsPageSize(resetExpiredJobsPageSize));    
  }

}

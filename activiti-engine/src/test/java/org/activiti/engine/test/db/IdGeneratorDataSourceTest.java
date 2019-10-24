package org.activiti.engine.test.db;

import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.impl.test.ResourceActivitiTestCase;
import org.activiti.engine.test.Deployment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IdGeneratorDataSourceTest extends ResourceActivitiTestCase {

  private static final Logger logger = LoggerFactory.getLogger(IdGeneratorDataSourceTest.class);

public IdGeneratorDataSourceTest() {
    super("org/activiti/engine/test/db/IdGeneratorDataSourceTest.activiti.cfg.xml");
  }

  @Deployment
  public void testIdGeneratorDataSource() {
    List<Thread> threads = new ArrayList<>();
    for (int i = 0; i < 20; i++) {
      Thread thread = new Thread() {
        @Override
		public void run() {
          for (int j = 0; j < 5; j++) {
            runtimeService.startProcessInstanceByKey("idGeneratorDataSource");
          }
        }
      };
      thread.start();
      threads.add(thread);
    }

    threads.forEach(thread -> {
      try {
        thread.join();
      } catch (InterruptedException e) {
        logger.error(e.getMessage(), e);
      }
    });
  }
}

package org.activiti.engine.test.api.repository;

import org.activiti.bpmn.exceptions.XMLException;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.repository.ProcessDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

/**

 */
public class DeployInvalidXmlTest extends PluggableActivitiTestCase {

  private static final Logger logger = LoggerFactory.getLogger(DeployInvalidXmlTest.class);
// Need to put this in a String here, if we use a separate file, the cpu
  // usage
  // of Eclipse skyrockets, regardless of the file is opened or not

  private static String unsafeXml = new StringBuilder().append("<?xml version='1.0' encoding='UTF-8'?>").append("<!-- Billion Laugh attacks : http://portal.sliderocket.com/CJAKM/xml-attacks -->").append("<!DOCTYPE lols [").append("<!ENTITY lol 'lol'>").append("<!ENTITY lol1 '&lol;&lol;&lol;&lol;&lol;&lol;&lol;&lol;&lol;&lol;&lol;'>").append("<!ENTITY lol2 '&lol1;&lol1;&lol1;&lol1;&lol1;&lol1;&lol1;&lol1;&lol1;&lol1;&lol1;'>").append("<!ENTITY lol3 '&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;'>").append("<!ENTITY lol4 '&lol3;&lol3;&lol3;&lol3;&lol3;&lol3;&lol3;&lol3;&lol3;&lol3;&lol3;'>").append("<!ENTITY lol5 '&lol4;&lol4;&lol4;&lol4;&lol4;&lol4;&lol4;&lol4;&lol4;&lol4;&lol4;'>")
		.append("<!ENTITY lol6 '&lol5;&lol5;&lol5;&lol5;&lol5;&lol5;&lol5;&lol5;&lol5;&lol5;&lol5;'>").append("<!ENTITY lol7 '&lol6;&lol6;&lol6;&lol6;&lol6;&lol6;&lol6;&lol6;&lol6;&lol6;&lol6;'>").append("<!ENTITY lol8 '&lol7;&lol7;&lol7;&lol7;&lol7;&lol7;&lol7;&lol7;&lol7;&lol7;&lol7;'>").append("<!ENTITY lol9 '&lol8;&lol8;&lol8;&lol8;&lol8;&lol8;&lol8;&lol8;&lol8;&lol8;&lol8;'>").append("]>").append("<lolz>&lol9;</lolz>").append("<definitions ").append("xmlns='http://www.omg.org/spec/BPMN/20100524/MODEL'").append("xmlns:activiti='http://activiti.org/bpmn'").append("targetNamespace='Examples'>")
		.append("<process id='oneTaskProcess' name='The One Task Process'>").append("  <documentation>This is a process for testing purposes</documentation>").append(" <startEvent id='theStart' />").append(" <sequenceFlow id='flow1' sourceRef='theStart' targetRef='theTask' />").append(" <userTask id='theTask' name='my task' />").append(" <sequenceFlow id='flow2' sourceRef='theTask' targetRef='theEnd' />").append(" <endEvent id='theEnd' />").append("</process>").append("</definitions>").toString();

@Override
  protected void setUp() throws Exception {
    super.setUp();

    processEngineConfiguration.setEnableSafeBpmnXml(true); // Needs to be
                                                           // enabled to
                                                           // test this
  }

@Override
  protected void tearDown() throws Exception {
    processEngineConfiguration.setEnableSafeBpmnXml(false); // set back to
                                                            // default
    super.tearDown();
  }

public void testDeployNonSchemaConformantXml() {
    try {
      repositoryService.createDeployment().addClasspathResource("org/activiti/engine/test/api/repository/nonSchemaConformantXml.bpmn20.xml").deploy().getId();
      fail();
    } catch (XMLException e) {
		logger.error(e.getMessage(), e);
      // expected exception
    }

  }

public void testDeployWithMissingWaypointsForSequenceflowInDiagramInterchange() {
    try {
      repositoryService.createDeployment().addClasspathResource("org/activiti/engine/test/api/repository/noWayPointsForSequenceFlowInDiagramInterchange.bpmn20.xml").deploy().getId();
      fail();
    } catch (XMLException e) {
		logger.error(e.getMessage(), e);
      // expected exception
    }
  }

// See
  // https://activiti.atlassian.net/browse/ACT-1579?focusedCommentId=319886#comment-319886
  public void testProcessEngineDenialOfServiceAttackUsingUnsafeXmlTest() throws InterruptedException {

    // Putting this in a Runnable so we can time it out
    // Without safe xml, this would run forever
    MyRunnable runnable = new MyRunnable(repositoryService);
    Thread thread = new Thread(runnable);
    thread.start();

    long waitTime = 60000L;
    thread.join(waitTime);

    assertTrue(runnable.finished);

  }

public void testExternalEntityResolvingTest() {
    String deploymentId = repositoryService.createDeployment().addClasspathResource("org/activiti/engine/test/api/repository/DeployInvalidXmlTest.testExternalEntityResolvingTest.bpmn20.xml").deploy()
        .getId();
    try {
      ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
      assertEquals("Test 1 2 3 null", processDefinition.getDescription());
    } finally {
      repositoryService.deleteDeployment(deploymentId, true);
    }
  }

static class MyRunnable implements Runnable {

    private final Logger logger1 = LoggerFactory.getLogger(MyRunnable.class);

	public boolean finished;

    protected RepositoryService repositoryService;

    public MyRunnable(RepositoryService repositoryService) {
      this.repositoryService = repositoryService;
    }

    @Override
	public void run() {
      try {
        String deploymentId = repositoryService.createDeployment().addString("test.bpmn20.xml", unsafeXml).deploy().getId();
        assertEquals(1, repositoryService.createProcessDefinitionQuery().singleResult());
        repositoryService.deleteDeployment(deploymentId, true);
      } catch (Exception e) {
		logger1.error(e.getMessage(), e);
        // Exception is expected
      } finally {
        finished = true;
      }
    }

  }

}

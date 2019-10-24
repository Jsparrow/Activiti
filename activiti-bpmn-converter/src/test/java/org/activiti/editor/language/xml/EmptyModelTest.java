package org.activiti.editor.language.xml;

import static org.junit.Assert.fail;

import org.activiti.bpmn.exceptions.XMLException;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EmptyModelTest extends AbstractConverterTest {

  private static final Logger logger = LoggerFactory.getLogger(EmptyModelTest.class);

@Test
  public void convertXMLToModel() throws Exception {
    try {
      readXMLFile();
      fail("Expected xml exception");
    } catch (XMLException e) {
		logger.error(e.getMessage(), e);
      // exception expected
    }
  }

  @Test
  public void convertModelToXML() throws Exception {
    try {
      readXMLFile();
      fail("Expected xml exception");
    } catch (XMLException e) {
		logger.error(e.getMessage(), e);
      // exception expected
    }
  }

  @Override
protected String getResource() {
    return "empty.bpmn";
  }
}

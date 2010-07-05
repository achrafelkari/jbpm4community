
package org.jbpm.pvm.internal.el;

import java.util.*;
import junit.framework.*;
import org.jbpm.pvm.internal.model.*;

public class ResourceBundleELResovlerTest extends TestCase {
  public void testResourceBundleELResovler() {
    MyResources myResources = new MyResources();
    ExecutionImpl execution = new ExecutionImpl();
    execution.setVariable("resourceBundle", myResources);

    Expression expr = Expression.create("#{resourceBundle['text.title']}", Expression.LANGUAGE_UEL_VALUE);
    Object result = expr.evaluate(execution);

    assertEquals("Title", result);
  }

  public static class MyResources extends ListResourceBundle {
     public Object[][] getContents() {
       return contents;
     }
     static final Object[][] contents = {
       {"text.title", "Title"}
     };
   }
}

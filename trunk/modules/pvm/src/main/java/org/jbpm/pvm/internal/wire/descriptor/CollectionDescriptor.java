package org.jbpm.pvm.internal.wire.descriptor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;

import org.jbpm.internal.log.Log;
import org.jbpm.pvm.internal.util.ReflectUtil;
import org.jbpm.pvm.internal.wire.Descriptor;
import org.jbpm.pvm.internal.wire.WireContext;
import org.jbpm.pvm.internal.wire.WireException;


/**
 * @author Tom Baeyens
 */
public class CollectionDescriptor extends AbstractDescriptor implements Descriptor {

  private static final long serialVersionUID = 1L;

  private static Log log = Log.getLog(CollectionDescriptor.class.getName());

  protected String className;
  protected List<Descriptor> valueDescriptors;
  protected boolean isSynchronized;

  protected CollectionDescriptor() { }

  public CollectionDescriptor(String defaultImplClassName) {
    this.className = defaultImplClassName;
  }

  public Object construct(WireContext wireContext) {
    Object object = null;
    try {
      // instantiate
      Class<?> clazz = ReflectUtil.classForName(className);
      object = clazz.newInstance();

      if (isSynchronized) {
        if (object instanceof SortedSet) {
          object = Collections.synchronizedSortedSet((SortedSet) object);
        } else if (object instanceof SortedMap) {
          object = Collections.synchronizedSortedMap((SortedMap) object);
        } else if (object instanceof Set) {
          object = Collections.synchronizedSet((Set) object);
        } else if (object instanceof Map) {
          object = Collections.synchronizedMap((Map) object);
        } else if (object instanceof List) {
          object = Collections.synchronizedList((List) object);
        } else if (object instanceof Collection) {
          object = Collections.synchronizedCollection((Collection) object);
        }
      }

    } catch (Exception e) {
      throw new WireException("couldn't create '"+(name!=null ? name : className)+"': "+e.getMessage(), e);
    }
    return object;
  }

  public void initialize(Object object, WireContext wireContext) {
    Collection<Object> collection = (Collection<Object>) object;
    try {
      if (valueDescriptors!=null) {
        for(Descriptor descriptor: valueDescriptors) {
          Object element = wireContext.create(descriptor, true);
          log.trace("adding element "+element+" to collection");
          collection.add(element);
        }
      }
    } catch (WireException e) {
      throw e;
    } catch (Exception e) {
      throw new WireException("couldn't initialize object '"+(name!=null ? name : className)+"'", e);
    }
  }

  public void addValueDescriptors(List<Descriptor> otherValueDescriptors) {
    if (valueDescriptors==null) {
      valueDescriptors = new ArrayList<Descriptor>();
    }
    if (otherValueDescriptors!=null) {
      valueDescriptors.addAll(otherValueDescriptors);
    }
  }

  public String getClassName() {
    return className;
  }
  public void setClassName(String className) {
    this.className = className;
  }
  public List<Descriptor> getValueDescriptors() {
    return valueDescriptors;
  }
  public void setValueDescriptors(List<Descriptor> valueDescriptors) {
    this.valueDescriptors = valueDescriptors;
  }
  public boolean isSynchronized() {
    return isSynchronized;
  }
  public void setSynchronized(boolean isSynchronized) {
    this.isSynchronized = isSynchronized;
  }
}

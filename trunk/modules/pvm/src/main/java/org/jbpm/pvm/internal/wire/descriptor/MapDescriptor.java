package org.jbpm.pvm.internal.wire.descriptor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbpm.pvm.internal.wire.Descriptor;
import org.jbpm.pvm.internal.wire.WireContext;
import org.jbpm.pvm.internal.wire.WireException;

/**
 *
 * <p>This {@link Descriptor} creates a {@link Map}.</p>
 *
 * <p>If no specific implementation for the {@link Map} is specified, a {@link HashMap} will be used.</p>
 *
 * <p>Entries can be added during the map initialization. The list of entries (key, value) to add must be specified with two operations:
 * <ol>
 * <li>{@link #setKeyDescriptors(List)} sets the list of the keys to generate.</li>
 * <li>{@link #setValueDescriptors(List)} sets the list of value associated with these keys.</li>
 * </ol>
 * The two lists must be in the same order
 * (the n-th element of the key list will be associated with the n-th element of the value list).</p>
 *
 * @author Tom Baeyens
 * @author Guillaume Porcher (documentation)
 *
 * @see Descriptor
 */
public class MapDescriptor extends CollectionDescriptor {

  private static final long serialVersionUID = 1L;

  List<Descriptor> keyDescriptors;

  public MapDescriptor() {
    super(HashMap.class.getName());
  }

  public void initialize(Object object, WireContext wireContext) {
    Map<Object,Object> map = (Map<Object,Object>) object;
    try {
      if (keyDescriptors!=null) {
        for (int i=0; i<keyDescriptors.size(); i++) {
          Descriptor keyDescriptor = keyDescriptors.get(i);
          Descriptor valueDescriptor = valueDescriptors.get(i);
          Object key = wireContext.create(keyDescriptor, true);
          Object value = wireContext.create(valueDescriptor, true);
          map.put(key, value);
        }
      }
    } catch (WireException e) {
      throw e;
    } catch (Exception e) {
      throw new WireException("couldn't initialize object '"+(name!=null ? name : className)+"'", e);
    }
  }
  
  

  public List<Descriptor> getKeyDescriptors() {
    return keyDescriptors;
  }

  public void setKeyDescriptors(List<Descriptor> keyDescriptors) {
    this.keyDescriptors = keyDescriptors;
  }
}

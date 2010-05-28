package org.jbpm.pvm.internal.wire.xml;

import java.net.URL;
import java.util.Enumeration;
import java.util.List;

import org.jbpm.api.JbpmException;
import org.jbpm.internal.log.Log;
import org.jbpm.pvm.internal.util.XmlUtil;
import org.jbpm.pvm.internal.wire.Descriptor;
import org.jbpm.pvm.internal.wire.WireContext;
import org.jbpm.pvm.internal.wire.WireDefinition;
import org.jbpm.pvm.internal.wire.binding.ByteBinding;
import org.jbpm.pvm.internal.wire.binding.CharBinding;
import org.jbpm.pvm.internal.wire.binding.ClassBinding;
import org.jbpm.pvm.internal.wire.binding.DoubleBinding;
import org.jbpm.pvm.internal.wire.binding.FloatBinding;
import org.jbpm.pvm.internal.wire.binding.IntBinding;
import org.jbpm.pvm.internal.wire.binding.ListBinding;
import org.jbpm.pvm.internal.wire.binding.MapBinding;
import org.jbpm.pvm.internal.wire.binding.NullBinding;
import org.jbpm.pvm.internal.wire.binding.ObjectBinding;
import org.jbpm.pvm.internal.wire.binding.RefBinding;
import org.jbpm.pvm.internal.wire.binding.SetBinding;
import org.jbpm.pvm.internal.wire.binding.StringBinding;
import org.jbpm.pvm.internal.wire.descriptor.AbstractDescriptor;
import org.jbpm.pvm.internal.wire.descriptor.ObjectDescriptor;
import org.jbpm.pvm.internal.xml.Bindings;
import org.jbpm.pvm.internal.xml.Parse;
import org.jbpm.pvm.internal.xml.Parser;
import org.w3c.dom.Element;

/**
 * parses object wiring xml and constructs a WireDefinition.
 *
 * <p>To learn the full XML syntax, check out the xsd docs
 * </p>
 * <ul><li>To describe an object:<br/>
 * <ul>
 *   <li><b><code>&lt;object .../&gt;</code></b>: see {@link ObjectBinding}</li>
 * </ul></li>
 * <li>To describe basic types:<br/>
 * <ul>
 *   <li><b><code>&lt;boolean .../&gt;</code>, <code>&lt;true /&gt;</code> and <code>&lt;false/&gt;</code></b>: see {@link BooleanBinding}</li>
 *   <li><b><code>&lt;byte .../&gt;</code></b>: see {@link ByteBinding}</li>
 *   <li><b><code>&lt;char .../&gt;</code></b>: see {@link CharBinding}</li>
 *   <li><b><code>&lt;double .../&gt;</code></b>: see {@link DoubleBinding}</li>
 *   <li><b><code>&lt;float .../&gt;</code></b>: see {@link FloatBinding}</li>
 *   <li><b><code>&lt;int .../&gt;</code></b>: see {@link IntBinding}</li>
 *   <li><b><code>&lt;string .../&gt;</code></b>: see {@link StringBinding}</li>
 * </ul></li>
 * <li>To describe collections:<br/>
 * <ul>
 *   <li><b><code>&lt;map .../&gt;</code></b>: see {@link MapBinding}</li>
 *   <li><b><code>&lt;set .../&gt;</code></b>: see {@link SetBinding}</li>
 *   <li><b><code>&lt;list .../&gt;</code></b>: see {@link ListBinding}</li>
 * </ul></li>
 * <li>Others:<br/>
 * <ul>
 *   <li><b><code>&lt;class .../&gt;</code></b>: see {@link ClassBinding}</li>
 *   <li><b><code>&lt;ref .../&gt;</code></b>: see {@link RefBinding}</li>
 *   <li><b><code>&lt;null .../&gt;</code></b>: see {@link NullBinding}</li>
 * </ul></li>
 * </ul>
 *
 * <h3>Bindings</h3>
 *
 * <p>The defaults bindings for the Wiring XML are divided in two categories:
 * </p>
 *
 * <ul>
 * 	<li>Descriptors, registered with the {@link #CATEGORY_DESCRIPTOR} category</li>
 * 	<li>Operations, registered with the {@link #CATEGORY_OPERATION} category</li>
 * </ul>
 *
 * <p>Once a parser is created, bindings can be added, overwritten and removed
 * to customize that parser instance.
 * </p>
 *
 * <h3 id='args'>Describing arguments</h3>
 *
 * <p>An ArgDescriptor is defined by a <b><code>&lt;arg&gt;</code></b> xml element.</p>
 *
 * <p>This element can have an attribute "type", which specifies name of the argument's type.</p>
 * <p>This element contains <b>one</b> child element that defines a {@link Descriptor}. This descriptor specifies the value to give to the argument.</p>
 *
 * <h4>Example</h4>
 *
 * Consider the following class:
 * <pre> public class Hello {
 *   public static String sayHello(String name) {
 *     return "Hello " + name + " !";
 *   }
 * }</pre>
 *
 * The following Xml declaration will create an object 's' of class 'String' (see {@link ObjectDescriptor}).
 * This object is created by invoking <code>Hello.sayHello</code> with the value <code>world</code> as a parameter.
 *
 * <pre> &lt;objects&gt;
 *   &lt;object name="s" class='Hello' method='sayHello'&gt;
 *     &lt;arg&gt;
 *      &lt;string value='world' /&gt;
 *     &lt;/arg&gt;
 *   &lt;/object&gt;
 * &lt;/objects&gt;</pre>
 *
 * The created object 's' will be a String, containing "Hello world !".
 * 
 * <h3 id='init'>Initialization</h3>
 * 
 * <p>The initialization method can be defined with the <code>init</code> attribute. 
 * For more details on how initialization works, see section 'Initialization' of {@link WireContext}.</p>
 * 
 * The <code>init</code> attribute can have these values:
 * <ul>
 *   <li><code>lazy</code>: for lazy creation and delayed initialization</li>
 *   <li><code>required</code>: for lazy creation and immediate initialization</li>
 *   <li><code>eager</code>: for eager creation and delayed initialization</li>
 *   <li><code>immediate</code>: for eager creation and immediate initialization</li>
 * </ul>
 *
 * @author Tom Baeyens
 * @author Guillaume Porcher (documentation)
 */
public class WireParser extends Parser {

  public static final String[] DEFAULT_WIRE_BINDING_RESOURCES = new String[]{
    "jbpm.wire.bindings.xml",
    "jbpm.user.wire.bindings.xml"
  }; 

  private static final long serialVersionUID = 1L;
  
  private static final Log log = Log.getLog(WireParser.class.getName());

  public static final String CATEGORY_DESCRIPTOR = "descriptor";
  public static final String CATEGORY_OPERATION = "operation";
  public static final String CATEGORY_INTERCEPTOR = "interceptor";

  /** static instance of WireParser */
  private static WireParser instance;
  /** default bindings for handling wiring xml elements. */
  private static Bindings defaultBindings; // initialized at the bottom of this file
  
  /**
   * Constructs a new WireParser with the default bindings.
   */
  public WireParser() {
    super(defaultBindings);
  }

  /**
   * Default method to get an instance of the WireParser
   * @return the static instance of WireParser
   */
  public static WireParser getInstance() {
    if (instance==null) {
      synchronized (WireParser.class) {
        if (instance == null) instance = new WireParser();
      }
    }
    return instance;
  }

  /**
   * Convenience method to parse a wiring xml.
   * @param xmlString the xml string to parse
   * @return the WireDefinition created by parsing the xml given in input.
   * @see #parseXmlString(String)
   */
  public static WireDefinition parseXmlString(String xmlString) {
    return (WireDefinition) getInstance()
            .createParse()
            .setString(xmlString)
            .execute()
            .checkErrors("wire definition xml string")
            .getDocumentObject();
  }

  // document element parsing /////////////////////////////////////////////////

  /**
   * This method builds the WireDefinition from the DOM tree.
   * This methods parses all child activities of the documentElement that correspond to a Descriptor definition.
   * @param documentElement the root element of the document
   * @param parse Parse object that contains all information for the current parse operation.
   * @return an instance of WireDefinition containing the resulting WireDefinition.
   * @see Parser#parseDocumentElement(Element, Parse)
   */
  public Object parseDocumentElement(Element documentElement, Parse parse) {
    List<Element> elements = XmlUtil.elements(documentElement);

    
    WireDefinition wireDefinition = parse.contextStackFind(WireDefinition.class);
    if (wireDefinition==null) {
      wireDefinition = new WireDefinition();
    }
    
    parse.contextStackPush(wireDefinition);
    try {
      for (Element descriptorElement: elements) {
        Descriptor descriptor = (Descriptor) parseElement(descriptorElement, parse, CATEGORY_DESCRIPTOR);
        
        // add the descriptor
        if ( (wireDefinition!=null)
             && (descriptor!=null)
           ) {
          wireDefinition.addDescriptor(descriptor);
        }
      }
    } finally {
      parse.contextStackPop();
    }

    return wireDefinition;
  }

  /**
   *  This method parses an arbitrary element in the document based on the bindings in the given category.
   *  This method calls the {@link Parser#parseElement(Element, Parse, String)} method to build the resulting object.
   *  If the resulting object is a subclass of {@link AbstractDescriptor}, the related fields are initialized.
   *
   *  @param element the element to parse
   *  @param parse Parse object that contains all information for the current parse operation.
   *  @param category is the category in which the tagName should be resolved to an ElementHandler.
   *  If category is null, all the categories will be scanned for an appropriate binding in random order.
   *  @return the java object created from the DOM element
   */
  public Object parseElement(Element element, Parse parse, String category) {
    if (element==null) return null;
    Object object = super.parseElement(element, parse, category);
    if ( (object!=null)
         && (object instanceof Descriptor)
       ) {
      
      Descriptor descriptor = (Descriptor) object;
      
      if (descriptor instanceof AbstractDescriptor) {
        AbstractDescriptor abstractDescriptor = (AbstractDescriptor) descriptor;
        if(element.hasAttribute("name")){
          String name = element.getAttribute("name");
          // get the name
          abstractDescriptor.setName(name);
        }

        if (element.hasAttribute("init")) {
          // get the init
          String initText = element.getAttribute("init");

          if("eager".equalsIgnoreCase(initText)){
            abstractDescriptor.setInit(AbstractDescriptor.INIT_EAGER);
          }else if("immediate".equalsIgnoreCase(initText)){
            abstractDescriptor.setInit(AbstractDescriptor.INIT_IMMEDIATE);
          } else if("required".equalsIgnoreCase(initText)){
            abstractDescriptor.setInit(AbstractDescriptor.INIT_REQUIRED);
          } else {
            // init='lazy' or default value
            abstractDescriptor.setInit(AbstractDescriptor.INIT_LAZY);
          }
        }
      }
    }
    return object;
  }

  // other methods ////////////////////////////////////////////////////////////

  static {
    // default descriptor parsers ///////////////////////////////////////////////
    defaultBindings = new Bindings();
    
    BindingParser bindingParser = new BindingParser();
 
    for (String wireResource: DEFAULT_WIRE_BINDING_RESOURCES) {
      ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
      Enumeration<URL> resourceUrls;
      try {
        resourceUrls = classLoader.getResources(wireResource);

        if (!resourceUrls.hasMoreElements()) {
          resourceUrls = WireParser.class.getClassLoader().getResources(wireResource);
        }
      } catch (Exception e) {
        throw new JbpmException("couldn't get resource urls for "+wireResource, e);
      }
      if (resourceUrls.hasMoreElements()) {
        while (resourceUrls.hasMoreElements()) {
          URL resourceUrl = resourceUrls.nextElement();
          log.trace("loading wire bindings from resource: "+resourceUrl);
          bindingParser.createParse()
            .setUrl(resourceUrl)
            .contextStackPush(defaultBindings)
            .execute()
            .checkErrors(resourceUrl.toString());
        }
      } else {
        log.trace("skipping unavailable wire bindings resource "+wireResource);
      }
    }
  }
}
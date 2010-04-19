/*
 * JBoss, Home of Professional Open Source
 * Copyright 2005, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jbpm.pvm.internal.env;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.jbpm.internal.log.Log;
import org.springframework.context.ApplicationContext;

/**
 * @author Andries Inze
 * 
 */
public class SpringContext implements Context {
  
  private static final Log LOG = Log.getLog(SpringContext.class.getName());

	private ApplicationContext applicationContext;

	public SpringContext(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jbpm.api.env.Context#get(java.lang.String)
	 */
	public Object get(String key) {
		if (applicationContext.containsBean(key)) {
			return applicationContext.getBean(key);
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public <T> T get(Class<T> type) {
		String[] names = applicationContext.getBeanNamesForType(type);
		if (names.length > 1 && LOG.isWarnEnabled()) {
		    LOG.warn("Multiple Spring beans found for type " + type + " returning the first one found");
		}
		
		if (names.length >= 1) {
		  return (T) applicationContext.getBean(names[0]);
		} 
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jbpm.api.env.Context#getName()
	 */
	public String getName() {
		return "spring";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jbpm.api.env.Context#has(java.lang.String)
	 */
	public boolean has(String key) {
		return applicationContext.containsBean(key);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jbpm.api.env.Context#keys()
	 */
	public Set<String> keys() {
		Set<String> set = new HashSet<String>(Arrays.asList(applicationContext.getBeanDefinitionNames()));
		return set;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jbpm.api.env.Context#set(java.lang.String, java.lang.Object)
	 */
	public Object set(String key, Object value) {
		throw new RuntimeException("Can't add to the spring context");
	}

}

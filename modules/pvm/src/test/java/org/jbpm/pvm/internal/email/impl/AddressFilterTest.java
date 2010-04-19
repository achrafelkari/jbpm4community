package org.jbpm.pvm.internal.email.impl;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import javax.mail.Address;
import javax.mail.internet.InternetAddress;

import junit.framework.TestCase;

public class AddressFilterTest extends TestCase {

  /**
   * When no filter is provided, all addresses should be contained in the filtered list.
   */
  public void testWildCardIncludesNoFilters() throws Exception {
    AddressFilter filter = new AddressFilter();

    InternetAddress jbossAddress = new InternetAddress("test@jboss.org");
    List<Address> filterAddresses = Arrays.asList(filter.filter(jbossAddress));

    assertTrue(filterAddresses.contains(jbossAddress));
  }

  /**
   * If the includes is provided, no other addresses except those explicitly listed should be
   * included in the filtered list.
   */
  public void testWildCardIncludes() throws Exception {
    AddressFilter filter = new AddressFilter();
    filter.addIncludePattern(Pattern.compile(".+@jboss.org"));

    InternetAddress jbossAddress = new InternetAddress("test@jboss.org");
    InternetAddress amentraAddress = new InternetAddress("test@amentra.com");
    List<Address> filterAddresses = Arrays.asList(filter.filter(jbossAddress, amentraAddress));

    assertTrue(filterAddresses.contains(jbossAddress));
    assertFalse(filterAddresses.contains(amentraAddress));
  }

  /**
   * The includes should always override the excludes.
   */
  public void testWildCardIncludesOverridesExcludes() throws Exception {
    AddressFilter filter = new AddressFilter();
    filter.addIncludePattern(Pattern.compile(".+@jboss.org"));
    filter.addExcludePattern(Pattern.compile(".+@amentra.com"));

    InternetAddress jbossAddress = new InternetAddress("test@jboss.org");
    InternetAddress amentraAddress = new InternetAddress("test@amentra.com");
    List<Address> filterAddresses = Arrays.asList(filter.filter(jbossAddress, amentraAddress));

    // Includes jboss
    assertTrue(filterAddresses.contains(jbossAddress));
    // Does not include amentra.
    assertFalse(filterAddresses.contains(amentraAddress));
  }

  /**
   * When the excludes is specified but no includes, all addresses except those explicitly stated
   * should be contained in the filtered set.
   */
  public void testWildCardExcludes() throws Exception {
    AddressFilter filter = new AddressFilter();
    filter.addExcludePattern(Pattern.compile(".+@jboss.org"));

    InternetAddress jbossAddress = new InternetAddress("test@jboss.org");
    InternetAddress amentraAddress = new InternetAddress("test@amentra.com");
    InternetAddress redhatAddress = new InternetAddress("test@redhat.com");
    List<Address> filterAddresses =
        Arrays.asList(filter.filter(jbossAddress, amentraAddress, redhatAddress));

    // Does not include jboss
    assertFalse(filterAddresses.contains(jbossAddress));
    // Does include amentra
    assertTrue(filterAddresses.contains(amentraAddress));
    // Does include redhat
    assertTrue(filterAddresses.contains(redhatAddress));
  }
}

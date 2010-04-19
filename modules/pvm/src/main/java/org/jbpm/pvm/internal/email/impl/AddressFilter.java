package org.jbpm.pvm.internal.email.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.mail.Address;

/**
 * Allows filtering of to/cc/bcc recipient lists based on regular expressions for include and
 * exclude patterns.
 * 
 * @author Brad Davis
 */
public class AddressFilter {

  /**
   * patterns of addresses to be included. all addresses are included when omitted.
   */
  private List<Pattern> includePatterns;
  /**
   * patterns of addresses to be excluded. no addresses are excluded when omitted.
   */
  private List<Pattern> excludePatterns;

  public Address[] filter(Address... addresses) {
    List<Address> filteredAddresses = new ArrayList<Address>();
    // Loop over for addresses to decide what to keep.
    for (Address address : addresses) {
      if (includeAddress(address) && !excludeAddress(address)) {
        filteredAddresses.add(address);
      }
    }
    return filteredAddresses.toArray(new Address[filteredAddresses.size()]);
  }

  /**
   * Determines whether the given address is included, based on regular expressions.
   * 
   * @param address email address to match against regex
   * @return <code>false</code> if include patterns are present and the address does not match any
   * pattern, <code>true</code> otherwise
   */
  protected boolean includeAddress(Address address) {
    if (includePatterns == null || includePatterns.isEmpty()) return true;
    for (Pattern pattern : includePatterns) {
      if (pattern.matcher(address.toString()).matches()) return true;
    }
    return false;
  }

  /**
   * Determines whether the given address is excluded, based on regular expressions.
   * 
   * @param address email address to match against regex
   * @return <code>true</code> if exclude patterns are present and the address matches a pattern,
   * <code>false</code> otherwise
   */
  protected boolean excludeAddress(Address address) {
    if (excludePatterns == null) return false;
    for (Pattern pattern : excludePatterns) {
      if (pattern.matcher(address.toString()).matches()) return true;
    }
    return false;
  }

  /**
   * Gets the patterns of addresses to be included. All addresses are included when omitted.
   */
  public List<Pattern> getIncludePatterns() {
    return includePatterns;
  }

  public void addIncludePattern(Pattern includePattern) {
    if (includePatterns == null) includePatterns = new ArrayList<Pattern>();
    includePatterns.add(includePattern);
  }

  protected void setIncludePatterns(List<Pattern> includePatterns) {
    this.includePatterns = includePatterns;
  }

  /**
   * Gets the patterns of addresses to be excluded. No addresses are excluded when omitted.
   */
  public List<Pattern> getExcludePatterns() {
    return excludePatterns;
  }

  public void addExcludePattern(Pattern excludePattern) {
    if (excludePatterns == null) excludePatterns = new ArrayList<Pattern>();
    excludePatterns.add(excludePattern);
  }

  protected void setExcludePatterns(List<Pattern> excludePatterns) {
    this.excludePatterns = excludePatterns;
  }
}

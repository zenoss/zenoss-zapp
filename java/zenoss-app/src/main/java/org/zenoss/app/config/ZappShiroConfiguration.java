package org.zenoss.app.config;


import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Override the default Shiro Configuration to provide some default values.
 */
public class ZappShiroConfiguration  {


    /**
     * Default URL pattern for the ShiroFilter.
     */
    final static String DEFAULT_FILTER_URL_PATTERN = "/*";

    /**
     * Default is {@link #DEFAULT_FILTER_URL_PATTERN}.
     */
    @JsonProperty("filter_url_pattern")
    private String filterUrlPattern = DEFAULT_FILTER_URL_PATTERN;

    /**
     * Currently supports a single URL-pattern.
     * @return the ShiroFilter will be configured to intercept URLS matching the returned url pattern.
     */
    public String getFilterUrlPattern() {
        return filterUrlPattern;
    }

    /**
     * Default is {@code true}.
     */
    @JsonProperty
    private boolean enabled = true;


    /**
     * Default is {@code true}.
     */
    @JsonProperty("dropwizard_session_handler")
    private boolean dropwizardSessionHandler = true;


    /**
     * Whether this bundle is enabled.
     * @return  value of the {@code enabled} field.  This is {@code false} by default.
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Either this is true, or you've already turned on the Servlet Sessions, or you have enabled Shiro's own "native" session-management mechanism.
     * @return true if the {@code }dropwizard_session_handler} configuration property is true; false otherwise
     */
    public boolean isDropwizardSessionHandler() {
        return dropwizardSessionHandler;
    }
}

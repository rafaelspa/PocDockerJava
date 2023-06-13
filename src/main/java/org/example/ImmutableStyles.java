package org.example;

import org.immutables.value.Value.Style;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Styles for immutable classes.
 * author: qinfchen https://github.com/palantir/dropwizard-web-security/blob/develop/src/main/java/com/palantir/websecurity/ImmutableStyles.java
 * issue: https://github.com/immutables/immutables/issues/291
 */

@Target({ElementType.PACKAGE, ElementType.TYPE})
@Style(
        visibility = Style.ImplementationVisibility.PACKAGE
)
@interface ImmutableStyles {}
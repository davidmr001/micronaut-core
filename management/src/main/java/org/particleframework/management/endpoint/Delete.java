/*
 * Copyright 2017 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.particleframework.management.endpoint;

import org.particleframework.context.annotation.Executable;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * A method designed to annotate an {@link Endpoint} delete operation
 *
 * @author James Kleeh
 * @since 1.0
 */
@Documented
@Retention(RUNTIME)
@Target(ElementType.METHOD)
@Executable
public @interface Delete {
    /**
     * @return Description of the operation
     */
    String description() default "";

    /**
     * @return The produced MediaType values. Defaults to application/json
     */
    String[] produces() default {"application/json"};

}
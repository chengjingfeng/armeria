/*
 * Copyright 2017 LINE Corporation
 *
 * LINE Corporation licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.linecorp.armeria.server;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

import com.linecorp.armeria.common.HttpHeaderNames;
import com.linecorp.armeria.common.HttpMethod;
import com.linecorp.armeria.common.HttpRequest;
import com.linecorp.armeria.common.MediaType;
import com.linecorp.armeria.internal.ArmeriaHttpUtil;

/**
 * Holds the parameters which are required to find a service available to handle the request.
 */
public interface RoutingContext {

    /**
     * Returns the {@link VirtualHost} instance which belongs to this {@link RoutingContext}.
     */
    VirtualHost virtualHost();

    /**
     * Returns the virtual host name of the request.
     */
    String hostname();

    /**
     * Returns {@link HttpMethod} of the request.
     */
    HttpMethod method();

    /**
     * Returns the absolute path retrieved from the request,
     * as defined in <a href="https://tools.ietf.org/html/rfc3986">RFC3986</a>.
     */
    String path();

    /**
     * Returns the query retrieved from the request,
     * as defined in <a href="https://tools.ietf.org/html/rfc3986">RFC3986</a>.
     */
    @Nullable
    String query();

    /**
     * Returns {@link MediaType} specified by 'Content-Type' header of the request.
     */
    @Nullable
    MediaType contentType();

    /**
     * Returns a list of {@link MediaType}s that are specified in {@link HttpHeaderNames#ACCEPT} in the order
     * of client-side preferences. If the client does not send the header, this will contain only
     * {@link MediaType#ANY_TYPE}.
     */
    List<MediaType> acceptTypes();

    /**
     * Returns an identifier of this {@link RoutingContext} instance.
     * It would be used as a cache key to reduce pattern list traversal.
     */
    List<Object> summary();

    /**
     * Delays throwing a {@link Throwable} until reaching the end of the service list.
     */
    void delayThrowable(Throwable cause);

    /**
     * Returns a delayed {@link Throwable} set before via {@link #delayThrowable(Throwable)}.
     */
    Optional<Throwable> delayedThrowable();

    /**
     * Returns a wrapped {@link RoutingContext} which holds the specified {@code path}.
     * It is usually used to find a {@link Service} with a prefix-stripped path.
     */
    default RoutingContext overridePath(String path) {
        requireNonNull(path, "path");
        return new DefaultRoutingContext(virtualHost(), hostname(), method(), path, query(),
                                         contentType(), acceptTypes(), isCorsPreflight());
    }

    /**
     * Returns {@code true} if this context is for a CORS preflight request.
     *
     * @see ArmeriaHttpUtil#isCorsPreflightRequest(HttpRequest)
     */
    boolean isCorsPreflight();
}

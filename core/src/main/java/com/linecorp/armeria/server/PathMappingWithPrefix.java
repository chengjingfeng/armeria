/*
 * Copyright 2019 LINE Corporation
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

import static com.linecorp.armeria.internal.RouteUtil.PREFIX;
import static com.linecorp.armeria.internal.RouteUtil.newLoggerName;
import static java.util.Objects.requireNonNull;

import java.util.Optional;
import java.util.Set;

import javax.annotation.Nullable;

final class PathMappingWithPrefix extends AbstractPathMapping {

    private final String pathPrefix;
    private final PathMapping mapping;
    private final String loggerName;
    private final String meterTag;

    PathMappingWithPrefix(String pathPrefix, PathMapping mapping) {
        requireNonNull(mapping, "mapping");
        // mapping should be GlobPathMapping or RegexPathMapping
        assert mapping.regex().isPresent() : "unexpected mapping type: " + mapping.getClass().getName();
        this.pathPrefix = requireNonNull(pathPrefix, "pathPrefix");
        this.mapping = mapping;
        loggerName = newLoggerName(pathPrefix) + '.' + mapping.loggerName();
        meterTag = PREFIX + pathPrefix + ',' + mapping.meterTag();
    }

    @Nullable
    @Override
    RoutingResultBuilder doApply(RoutingContext routingCtx) {
        final String path = routingCtx.path();
        if (!path.startsWith(pathPrefix)) {
            return null;
        }

        final RoutingResultBuilder builder =
                mapping.apply(routingCtx.overridePath(path.substring(pathPrefix.length() - 1)));
        if (builder != null) {
            // Replace the path.
            builder.path(path);
        }

        return builder;
    }

    @Override
    public Set<String> paramNames() {
        return mapping.paramNames();
    }

    @Override
    public String loggerName() {
        return loggerName;
    }

    @Override
    public String meterTag() {
        return meterTag;
    }

    @Override
    public Optional<String> prefix() {
        return Optional.of(pathPrefix);
    }

    @Override
    public Optional<String> regex() {
        return mapping.regex();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PathMappingWithPrefix)) {
            return false;
        }

        final PathMappingWithPrefix that = (PathMappingWithPrefix) o;
        return pathPrefix.equals(that.pathPrefix) && mapping.equals(that.mapping);
    }

    @Override
    public int hashCode() {
        return 31 * pathPrefix.hashCode() + mapping.hashCode();
    }

    @Override
    public String toString() {
        return '[' + PREFIX + pathPrefix + ", " + mapping + ']';
    }
}

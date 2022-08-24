/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.ignite.internal.processors.query;

import java.util.Collection;
import org.apache.ignite.cache.QueryIndexType;

/**
 * Sys Indexes descriptor.
 */
public class QuerySysIndexDescriptorImpl implements GridQueryIndexDescriptor {
    /** Index name. */
    private final String name;

    /** Index fields. */
    private final Collection<String> fields;

    /**
     * Constructor.
     *
     * @param name Index name.
     * @param fields Index fields.
     */
    public QuerySysIndexDescriptorImpl(String name, Collection<String> fields) {
        this.name = name;
        this.fields = fields;
    }

    /** {@inheritDoc} */
    @Override public String name() {
        return name;
    }

    /** {@inheritDoc} */
    @Override public Collection<String> fields() {
        return fields;
    }

    /** {@inheritDoc} */
    @Override public boolean descending(String field) {
        return false;
    }

    /** {@inheritDoc} */
    @Override public QueryIndexType type() {
        throw new UnsupportedOperationException("Not implemented");
    }

    /** {@inheritDoc} */
    @Override public int inlineSize() {
        throw new UnsupportedOperationException("Not implemented");
    }
}

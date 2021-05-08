/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.ctrip.framework.apollo.model;

import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.ConfigChangeListener;
import com.ctrip.framework.apollo.spring.annotation.ApolloConfigChangeListener;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * A change event when a namespace's config is changed.
 * @author Jason Song(song_s@ctrip.com)
 */
public class ConfigChangeEvent {
  private final String m_namespace;
  private final Map<String, ConfigChange> m_changes;

  /**
   * @see Config#addChangeListener(ConfigChangeListener, Set)
   * @see Config#addChangeListener(ConfigChangeListener, Set, Set)
   * @see ApolloConfigChangeListener#interestedKeys()
   * @see ApolloConfigChangeListener#interestedKeyPrefixes()
   */
  private final Set<String> m_interestedChangedKeys;

  /**
   * Constructor.
   * @param namespace the namespace of this change
   * @param changes the actual changes
   */
  public ConfigChangeEvent(String namespace,
                           Map<String, ConfigChange> changes) {
    this(namespace, changes, Collections.<String>emptySet());
  }

  public ConfigChangeEvent(String namespace,
      Map<String, ConfigChange> changes, Set<String> interestedChangedKeys) {
    this.m_namespace = namespace;
    this.m_changes = changes;
    this.m_interestedChangedKeys = interestedChangedKeys;
  }

  /**
   * Get the keys changed.
   * @return the list of the keys
   */
  public Set<String> changedKeys() {
    return m_changes.keySet();
  }

  /**
   * @return interested and changed keys
   */
  public Set<String> interestedChangedKeys() {
    return Collections.unmodifiableSet(this.m_interestedChangedKeys);
  }

  /**
   * Get a specific change instance for the key specified.
   * @param key the changed key
   * @return the change instance
   */
  public ConfigChange getChange(String key) {
    return m_changes.get(key);
  }

  /**
   * Check whether the specified key is changed
   * @param key the key
   * @return true if the key is changed, false otherwise.
   */
  public boolean isChanged(String key) {
    return m_changes.containsKey(key);
  }

  /**
   * Get the namespace of this change event.
   * @return the namespace
   */
  public String getNamespace() {
    return m_namespace;
  }

  /**
   * @return all changed keys and instances
   */
  public Map<String, ConfigChange> getChanges() {
    return Collections.unmodifiableMap(this.m_changes);
  }
}

/*
 * Copyright 2022 TR Software Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package solutions.trsoftware.commons.server.testutil.rpc;

import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.server.rpc.SerializationPolicy;

import java.io.Serializable;

/**
 * A dummy {@link SerializationPolicy} that allows serializing any {@linkplain Serializable serializable} type.
 */
public class SimpleSerializationPolicy extends SerializationPolicy {

  public boolean isSerializable(Class<?> cls) {
    return cls != null && (cls.isPrimitive() || Serializable.class.isAssignableFrom(cls));
  }

  @Override
  public boolean shouldDeserializeFields(Class<?> clazz) {
    return isSerializable(clazz);
  }

  @Override
  public boolean shouldSerializeFields(Class<?> clazz) {
    return isSerializable(clazz);
  }

  @Override
  public void validateDeserialize(Class<?> clazz) throws SerializationException {
    // TODO: should this method do anything?
  }

  @Override
  public void validateSerialize(Class<?> clazz) throws SerializationException {
    // TODO: should this method do anything?;
  }
}

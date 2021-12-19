/*
 * Copyright 2019-2021 CloudNetService team & contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.dytanic.cloudnet.driver.service.property;

import com.google.common.base.Preconditions;
import de.dytanic.cloudnet.driver.service.ServiceInfoSnapshot;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

public class DefaultFunctionalServiceProperty<T> implements ServiceProperty<T> {

  private Function<ServiceInfoSnapshot, T> getFunction;
  private BiConsumer<ServiceInfoSnapshot, T> setConsumer;

  public static <T> @NonNull DefaultFunctionalServiceProperty<T> create() {
    return new DefaultFunctionalServiceProperty<>();
  }

  public @NonNull DefaultFunctionalServiceProperty<T> reader(@NonNull Function<ServiceInfoSnapshot, T> getFunction) {
    this.getFunction = getFunction;
    return this;
  }

  public @NonNull DefaultFunctionalServiceProperty<T> writer(@NonNull BiConsumer<ServiceInfoSnapshot, T> setConsumer) {
    this.setConsumer = setConsumer;
    return this;
  }

  @Override
  public @NonNull Optional<T> read(@NonNull ServiceInfoSnapshot serviceInfoSnapshot) {
    Preconditions.checkNotNull(this.getFunction, "This property doesn't support getting a value");
    return Optional.ofNullable(this.getFunction.apply(serviceInfoSnapshot));
  }

  @Override
  public void write(@NonNull ServiceInfoSnapshot serviceInfoSnapshot, @Nullable T value) {
    Preconditions.checkNotNull(this.setConsumer, "This property doesn't support modifying a value");
    this.setConsumer.accept(serviceInfoSnapshot, value);
  }
}

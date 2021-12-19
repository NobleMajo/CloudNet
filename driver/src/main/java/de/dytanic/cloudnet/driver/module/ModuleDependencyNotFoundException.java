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

package de.dytanic.cloudnet.driver.module;

import lombok.NonNull;

/**
 * Thrown when a module depends on another module which is not loaded.
 */
public class ModuleDependencyNotFoundException extends RuntimeException {

  /**
   * Constructs a new instance of this class.
   *
   * @param dependency      the name of the dependency which is missing.
   * @param requiringModule the module which required the dependency to be present.
   */
  public ModuleDependencyNotFoundException(@NonNull String dependency, @NonNull String requiringModule) {
    super(String.format("Missing module dependency %s required by %s", dependency, requiringModule));
  }
}

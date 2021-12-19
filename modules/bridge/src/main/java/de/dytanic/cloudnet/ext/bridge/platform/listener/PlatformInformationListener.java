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

package de.dytanic.cloudnet.ext.bridge.platform.listener;

import de.dytanic.cloudnet.driver.event.EventListener;
import de.dytanic.cloudnet.driver.event.events.service.CloudServiceLifecycleChangeEvent;
import de.dytanic.cloudnet.driver.event.events.service.CloudServiceUpdateEvent;
import de.dytanic.cloudnet.driver.event.events.task.ServiceTaskAddEvent;
import de.dytanic.cloudnet.driver.event.events.task.ServiceTaskRemoveEvent;
import de.dytanic.cloudnet.ext.bridge.platform.PlatformBridgeManagement;
import de.dytanic.cloudnet.wrapper.event.service.ServiceInfoSnapshotConfigureEvent;
import lombok.NonNull;

public final class PlatformInformationListener {

  private final PlatformBridgeManagement<?, ?> management;

  public PlatformInformationListener(@NonNull PlatformBridgeManagement<?, ?> management) {
    this.management = management;
  }

  @EventListener
  public void handle(@NonNull ServiceInfoSnapshotConfigureEvent event) {
    this.management.appendServiceInformation(event.serviceInfo());
  }

  @EventListener
  public void handleLifecycleChange(@NonNull CloudServiceLifecycleChangeEvent event) {
    this.management.handleServiceUpdate(event.serviceInfo());
  }

  @EventListener
  public void handleLifecycleChange(@NonNull CloudServiceUpdateEvent event) {
    this.management.handleServiceUpdate(event.serviceInfo());
  }

  @EventListener
  public void handleServiceTaskAdd(@NonNull ServiceTaskAddEvent event) {
    this.management.handleTaskUpdate(event.task().name(), event.task());
  }

  @EventListener
  public void handleServiceTaskRemove(@NonNull ServiceTaskRemoveEvent event) {
    this.management.handleTaskUpdate(event.task().name(), null);
  }
}

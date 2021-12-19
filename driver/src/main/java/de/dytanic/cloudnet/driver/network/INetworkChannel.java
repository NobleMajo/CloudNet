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

package de.dytanic.cloudnet.driver.network;

import de.dytanic.cloudnet.common.concurrent.ITask;
import de.dytanic.cloudnet.driver.network.protocol.IPacket;
import de.dytanic.cloudnet.driver.network.protocol.IPacketListenerRegistry;
import de.dytanic.cloudnet.driver.network.protocol.IPacketSender;
import de.dytanic.cloudnet.driver.network.protocol.QueryPacketManager;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

/**
 * A NetworkChannel instance represents an open connection
 */
public interface INetworkChannel extends IPacketSender {

  /**
   * Returns the unique channelId. The Channel Id begins with 1 and ends with Long.MAX_VALUE
   */
  long channelId();

  /**
   * Returns the server address from this channel
   */
  @NonNull HostAndPort serverAddress();

  /**
   * Returns the client address from this channel
   */
  @NonNull HostAndPort clientAddress();

  /**
   * Returns the networkChannelHandler from this channel
   */
  @NonNull INetworkChannelHandler handler();

  /**
   * Sets the channel handler for the channels. That is important for the handling of receiving packets or channel
   * closing and connect handler
   *
   * @param handler the handler, that should handle this channel
   */
  void handler(@NonNull INetworkChannelHandler handler);

  /**
   * Returns the own packet listener registry. The packetRegistry is a sub registry of the network component packet
   * listener registry
   */
  @NonNull IPacketListenerRegistry packetRegistry();

  @NonNull QueryPacketManager queryPacketManager();

  /**
   * Returns that, the channel based of the client site connection
   */
  boolean clientProvidedChannel();

  @Nullable IPacket sendQuery(@NonNull IPacket packet);

  @NonNull ITask<IPacket> sendQueryAsync(@NonNull IPacket packet);

  boolean writeable();

  boolean active();

  void close();
}

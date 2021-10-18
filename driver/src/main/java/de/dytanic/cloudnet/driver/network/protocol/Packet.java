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

package de.dytanic.cloudnet.driver.network.protocol;

import de.dytanic.cloudnet.driver.network.buffer.DataBuf;
import java.util.UUID;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The default simple implementation of the IPacket interface. You can create with the constructor all new packets or
 * use the class as superclass from an another for specific constructor usage.
 * <p>
 * All packets require a channel, header and a body.
 * <p>
 * The channel id is the id from that the listeners should be filter The header has the specify information or the data
 * that is important The body has binary packet information like for files, or zip compressed data
 */
@ToString
@EqualsAndHashCode
public class Packet implements IPacket {

  public static final IPacket EMPTY = new Packet(-1, null);

  protected final int channel;
  protected final DataBuf dataBuf;
  protected final long creationMillis;

  protected UUID uniqueId;

  public Packet() {
    this(0, null);
  }

  public Packet(int channel, DataBuf dataBuf) {
    this.channel = channel;
    this.dataBuf = dataBuf;
    this.creationMillis = System.currentTimeMillis();
  }

  @Override
  public IPacket constructResponse(DataBuf content) {
    Packet packet = new Packet(-1, content);
    packet.setUniqueId(this.getUniqueId());
    return packet;
  }

  @Override
  public @Nullable UUID getUniqueId() {
    return this.uniqueId;
  }

  @Override
  public void setUniqueId(@Nullable UUID uniqueId) {
    this.uniqueId = uniqueId;
  }

  @Override
  public int getChannel() {
    return this.channel;
  }

  @Override
  public @NotNull DataBuf getContent() {
    return this.dataBuf;
  }

  @Override
  public long getCreationMillis() {
    return this.creationMillis;
  }
}
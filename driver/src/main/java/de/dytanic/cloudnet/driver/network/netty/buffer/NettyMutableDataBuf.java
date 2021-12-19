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

package de.dytanic.cloudnet.driver.network.netty.buffer;

import de.dytanic.cloudnet.driver.network.buffer.DataBuf;
import de.dytanic.cloudnet.driver.network.netty.NettyUtils;
import de.dytanic.cloudnet.driver.network.rpc.defaults.object.DefaultObjectMapper;
import io.netty.buffer.ByteBuf;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.function.BiConsumer;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

public class NettyMutableDataBuf extends NettyImmutableDataBuf implements DataBuf.Mutable {

  public NettyMutableDataBuf(ByteBuf byteBuf) {
    super(byteBuf);
  }

  @Override
  public @NonNull DataBuf.Mutable writeBoolean(boolean b) {
    this.byteBuf.writeBoolean(b);
    return this;
  }

  @Override
  public @NonNull DataBuf.Mutable writeInt(int integer) {
    this.byteBuf.writeInt(integer);
    return this;
  }

  @Override
  public @NonNull DataBuf.Mutable writeByte(byte b) {
    this.byteBuf.writeByte(b);
    return this;
  }

  @Override
  public @NonNull DataBuf.Mutable writeShort(short s) {
    this.byteBuf.writeShort(s);
    return this;
  }

  @Override
  public @NonNull DataBuf.Mutable writeLong(long l) {
    this.byteBuf.writeLong(l);
    return this;
  }

  @Override
  public @NonNull DataBuf.Mutable writeFloat(float f) {
    this.byteBuf.writeFloat(f);
    return this;
  }

  @Override
  public @NonNull DataBuf.Mutable writeDouble(double d) {
    this.byteBuf.writeDouble(d);
    return this;
  }

  @Override
  public @NonNull DataBuf.Mutable writeChar(char c) {
    this.byteBuf.writeChar(c);
    return this;
  }

  @Override
  public @NonNull DataBuf.Mutable writeByteArray(byte[] b) {
    return this.writeByteArray(b, b.length);
  }

  @Override
  public @NonNull DataBuf.Mutable writeByteArray(byte[] b, int amount) {
    NettyUtils.writeVarInt(this.byteBuf, amount);
    this.byteBuf.writeBytes(b, 0, amount);
    return this;
  }

  @Override
  public @NonNull DataBuf.Mutable writeUniqueId(@NonNull UUID uuid) {
    return this.writeLong(uuid.getMostSignificantBits()).writeLong(uuid.getLeastSignificantBits());
  }

  @Override
  public @NonNull DataBuf.Mutable writeString(@NonNull String string) {
    return this.writeByteArray(string.getBytes(StandardCharsets.UTF_8));
  }

  @Override
  public @NonNull DataBuf.Mutable writeDataBuf(@NonNull DataBuf buf) {
    buf.startTransaction();
    // write the content
    this.writeInt(buf.readableBytes());
    this.byteBuf.writeBytes(((NettyImmutableDataBuf) buf).byteBuf);
    // reset the data for later use
    buf.redoTransaction();

    return this;
  }

  @Override
  public @NonNull DataBuf.Mutable writeObject(@Nullable Object obj) {
    return DefaultObjectMapper.DEFAULT_MAPPER.writeObject(this, obj);
  }

  @Override
  public @NonNull <T> Mutable writeNullable(@Nullable T object, @NonNull BiConsumer<Mutable, T> handlerWhenNonNull) {
    this.writeBoolean(object != null);
    if (object != null) {
      handlerWhenNonNull.accept(this, object);
    }
    return this;
  }

  @Override
  public @NonNull DataBuf asImmutable() {
    return new NettyImmutableDataBuf(this.byteBuf);
  }
}

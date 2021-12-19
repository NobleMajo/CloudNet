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

package de.dytanic.cloudnet.ext.bridge.platform.waterdog;

import de.dytanic.cloudnet.ext.bridge.platform.PlatformBridgeManagement;
import dev.waterdog.waterdogpe.ProxyServer;
import dev.waterdog.waterdogpe.network.serverinfo.ServerInfo;
import dev.waterdog.waterdogpe.player.ProxiedPlayer;
import dev.waterdog.waterdogpe.utils.types.IForcedHostHandler;
import dev.waterdog.waterdogpe.utils.types.IReconnectHandler;
import java.util.Locale;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

final class WaterDogPEHandlers implements IForcedHostHandler, IReconnectHandler {

  private final PlatformBridgeManagement<ProxiedPlayer, ?> management;

  public WaterDogPEHandlers(@NonNull PlatformBridgeManagement<ProxiedPlayer, ?> management) {
    this.management = management;
  }

  @Override
  public ServerInfo resolveForcedHost(@Nullable String domain, @NonNull ProxiedPlayer player) {
    return this.management.fallback(player.getUniqueId(), null, domain, player::hasPermission)
      .map(server -> ProxyServer.getInstance().getServerInfo(server.name()))
      .orElse(null);
  }

  @Override
  public ServerInfo getFallbackServer(
    @NonNull ProxiedPlayer player,
    @NonNull ServerInfo oldServer,
    @NonNull String kickMessage
  ) {
    // send the player the reason for the disconnect
    player.sendMessage(this.management.configuration().message(Locale.ENGLISH, "error-connecting-to-server")
      .replace("%server%", oldServer.getServerName())
      .replace("%reason%", kickMessage));
    // filter the next fallback for the player
    return this.management.fallback(player, oldServer.getServerName())
      .map(server -> ProxyServer.getInstance().getServerInfo(server.name()))
      .orElse(null);
  }
}

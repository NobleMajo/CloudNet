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

package de.dytanic.cloudnet.setup;

import de.dytanic.cloudnet.console.IConsole;
import de.dytanic.cloudnet.console.animation.setup.ConsoleSetupAnimation;
import java.util.Collection;
import java.util.concurrent.CopyOnWriteArraySet;
import org.jetbrains.annotations.NotNull;

public class DefaultInstallation {

  private final Object monitor = new Object();
  private final ConsoleSetupAnimation animation = createAnimation();
  private final Collection<DefaultSetup> setups = new CopyOnWriteArraySet<>();

  private static @NotNull ConsoleSetupAnimation createAnimation() {
    return new ConsoleSetupAnimation(
      "&f   ___  _                    _ &b     __    __  _____  &3  _____              _           _  _ \n" +
        "&f  / __\\| |  ___   _   _   __| |&b  /\\ \\ \\  /__\\/__   \\ &3  \\_   \\ _ __   ___ | |_   __ _ | || |\n" +
        "&f / /   | | / _ \\ | | | | / _` |&b /  \\/ / /_\\    / /\\/ &3   / /\\/| '_ \\ / __|| __| / _` || || |\n" +
        "&f/ /___ | || (_) || |_| || (_| |&b/ /\\  / //__   / /    &3/\\/ /_  | | | |\\__ \\| |_ | (_| || || |\n" +
        "&f\\____/ |_| \\___/  \\__,_| \\__,_|&b\\_\\ \\/  \\__/   \\/     &3\\____/  |_| |_||___/ \\__| \\__,_||_||_|\n"
        +
        "&f                               &b                      &3                                      ",
      null,
      "&r> &e");
  }

  public void executeFirstStartSetup(@NotNull IConsole console) {
    if (!Boolean.getBoolean("cloudnet.installation.skip") && !this.setups.isEmpty()) {
      // apply all questions of all setups to the animation
      this.setups.forEach(setup -> setup.applyQuestions(this.animation));
      // start the animation
      this.animation.setCancellable(false);
      console.startAnimation(this.animation);

      this.animation.addFinishHandler(() -> {
        // post the finish handling to the installations
        this.setups.forEach(setup -> setup.handleResults(animation));
        // notify the monitor about the success
        synchronized (this.monitor) {
          this.monitor.notifyAll();
        }
      });

      try {
        // wait for the finish signal
        synchronized (this.monitor) {
          this.monitor.wait();
        }
      } catch (InterruptedException exception) {
        throw new RuntimeException("Interrupted while waiting for the setup process to complete", exception);
      }
    }
  }

  public void registerSetup(@NotNull DefaultSetup setup) {
    this.setups.add(setup);
  }
}

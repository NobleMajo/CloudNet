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

package de.dytanic.cloudnet.command.defaults;

import cloud.commandframework.Command;
import cloud.commandframework.CommandManager;
import cloud.commandframework.annotations.AnnotationParser;
import cloud.commandframework.exceptions.ArgumentParseException;
import cloud.commandframework.exceptions.InvalidCommandSenderException;
import cloud.commandframework.exceptions.InvalidSyntaxException;
import cloud.commandframework.exceptions.NoPermissionException;
import cloud.commandframework.exceptions.NoSuchCommandException;
import cloud.commandframework.extra.confirmation.CommandConfirmationManager;
import cloud.commandframework.meta.CommandMeta;
import cloud.commandframework.meta.CommandMeta.Key;
import cloud.commandframework.meta.SimpleCommandMeta;
import de.dytanic.cloudnet.command.CommandProvider;
import de.dytanic.cloudnet.command.annotation.CommandAlias;
import de.dytanic.cloudnet.command.exception.ArgumentNotAvailableException;
import de.dytanic.cloudnet.command.exception.CommandExceptionHandler;
import de.dytanic.cloudnet.command.source.CommandSource;
import de.dytanic.cloudnet.command.sub.CommandClear;
import de.dytanic.cloudnet.command.sub.CommandCopy;
import de.dytanic.cloudnet.command.sub.CommandCreate;
import de.dytanic.cloudnet.command.sub.CommandDebug;
import de.dytanic.cloudnet.command.sub.CommandExit;
import de.dytanic.cloudnet.command.sub.CommandGroups;
import de.dytanic.cloudnet.command.sub.CommandMe;
import de.dytanic.cloudnet.command.sub.CommandMigrate;
import de.dytanic.cloudnet.command.sub.CommandPermissions;
import de.dytanic.cloudnet.command.sub.CommandService;
import de.dytanic.cloudnet.command.sub.CommandTasks;
import de.dytanic.cloudnet.command.sub.CommandTemplate;
import de.dytanic.cloudnet.common.log.LogManager;
import de.dytanic.cloudnet.common.log.Logger;
import de.dytanic.cloudnet.console.IConsole;
import de.dytanic.cloudnet.driver.command.CommandInfo;
import io.leangen.geantyref.TypeToken;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DefaultCommandProvider implements CommandProvider {

  private static final Logger LOGGER = LogManager.getLogger(DefaultCommandProvider.class);
  private static final Key<Collection<String>> ALIAS_KEY = Key.of(new TypeToken<Collection<String>>() {
  }, "alias");

  private final CommandManager<CommandSource> commandManager;
  private final AnnotationParser<CommandSource> annotationParser;
  private final CommandConfirmationManager<CommandSource> confirmationManager;
  private final Collection<CommandInfo> registeredCommands;
  private final CommandExceptionHandler exceptionHandler;

  public DefaultCommandProvider() {
    this.commandManager = new DefaultCommandManager();
    this.annotationParser = new AnnotationParser<>(this.commandManager, CommandSource.class,
      parameters -> SimpleCommandMeta.empty());

    this.registeredCommands = new ArrayList<>();
    //TODO: maybe this should be moved too
    this.commandManager.registerCommandPreProcessor(new DefaultCommandPreProcessor());
    this.commandManager.registerCommandPostProcessor(new DefaultCommandPostProcessor());

    this.annotationParser.registerBuilderModifier(CommandAlias.class,
      (alias, builder) -> builder.meta(ALIAS_KEY, Arrays.asList(alias.value())));

    this.confirmationManager = new CommandConfirmationManager<>(
      30L,
      TimeUnit.SECONDS,
      context -> context.getCommandContext().getSender().sendMessage("Confirmation required."),
      //TODO: message configurable
      sender -> sender.sendMessage("No requests.")  //TODO: message configurable
    );
    this.confirmationManager.registerConfirmationProcessor(this.commandManager);
    this.commandManager.command(this.commandManager.commandBuilder("confirm")
      .handler(this.confirmationManager.createConfirmationExecutionHandler()));

    this.exceptionHandler = new CommandExceptionHandler();

  }

  @Override
  public @NotNull List<String> suggest(@NotNull CommandSource source, @NotNull String input) {
    return this.commandManager.suggest(source, input);
  }

  @Override
  public void execute(@NotNull CommandSource source, @NotNull String input) {
    try {
      //join the future to handle the occurring exceptions
      this.commandManager.executeCommand(source, input).join();
    } catch (CompletionException exception) {
      Throwable cause = exception.getCause();

      // cloud wraps the exception in the cause, if no cause is found we can't handle the exception
      if (cause == null) {
        return;
      }

      // determine the exception type and apply the specific handler
      if (cause instanceof InvalidSyntaxException) {
        this.exceptionHandler.handleInvalidSyntaxException(source, (InvalidSyntaxException) cause);
      } else if (cause instanceof NoPermissionException) {
        this.exceptionHandler.handleNoPermissionException(source, (NoPermissionException) cause);
      } else if (cause instanceof NoSuchCommandException) {
        this.exceptionHandler.handleNoSuchCommandException(source, (NoSuchCommandException) cause);
      } else if (cause instanceof InvalidCommandSenderException) {
        this.exceptionHandler.handleInvalidCommandSourceException(source, (InvalidCommandSenderException) cause);
      } else if (cause instanceof ArgumentParseException) {
        Throwable deepCause = cause.getCause();
        if (deepCause instanceof ArgumentNotAvailableException) {
          this.exceptionHandler.handleArgumentNotAvailableException(source, (ArgumentNotAvailableException) deepCause);
        } else {
          this.exceptionHandler.handleArgumentParseException(source, (ArgumentParseException) cause);
        }
      } else {
        LOGGER.severe("Exception during command execution", exception.getCause());
      }
    }
  }

  @Override
  public void register(@NotNull Object command) {
    Iterator<Command<CommandSource>> cloudCommands = this.annotationParser.parse(command).iterator();
    if (cloudCommands.hasNext()) {
      Command<CommandSource> parsedCommand = cloudCommands.next();
      String permission = parsedCommand.getCommandPermission()
        .toString();
      String description = parsedCommand.getCommandMeta().get(CommandMeta.DESCRIPTION)
        .orElse("No description provided");

      Collection<String> aliases = parsedCommand.getCommandMeta().getOrDefault(ALIAS_KEY, Collections.emptyList());
      String name = parsedCommand.getArguments().get(0).getName();

      this.registeredCommands.add(new CommandInfo(name, aliases, permission, description, "")); //TODO: usage
    }
  }

  @Override
  public void registerConsoleHandler(IConsole console) {
    console.addCommandHandler(UUID.randomUUID(), input -> {
      String trimmedInput = input.trim();
      if (trimmedInput.isEmpty()) {
        return;
      }
      this.execute(CommandSource.console(), trimmedInput);
    });
  }

  @Override
  public void registerDefaultCommands() {
    this.register(new CommandTemplate());
    this.register(new CommandExit());
    this.register(new CommandGroups());
    this.register(new CommandTasks());
    this.register(new CommandCreate());
    this.register(new CommandMe());
    this.register(new CommandService());
    this.register(new CommandPermissions());
    this.register(new CommandClear());
    this.register(new CommandDebug());
    this.register(new CommandCopy());
    this.register(new CommandMigrate());
  }

  @Override
  public @Nullable CommandInfo getCommand(@NotNull String input) {
    String lowerCaseInput = input.toLowerCase();
    return this.registeredCommands.stream()
      .filter(commandInfo -> commandInfo.getAliases().contains(input.toLowerCase())
        || commandInfo.getName().equals(lowerCaseInput))
      .findFirst()
      .orElse(null);
  }

  @Override
  public @NotNull Collection<CommandInfo> getCommands() {
    return Collections.unmodifiableCollection(this.registeredCommands);
  }


}

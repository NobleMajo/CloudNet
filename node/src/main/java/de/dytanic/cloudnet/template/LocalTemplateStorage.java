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

package de.dytanic.cloudnet.template;

import de.dytanic.cloudnet.common.io.FileUtils;
import de.dytanic.cloudnet.common.log.LogManager;
import de.dytanic.cloudnet.common.log.Logger;
import de.dytanic.cloudnet.driver.service.ServiceTemplate;
import de.dytanic.cloudnet.driver.template.FileInfo;
import de.dytanic.cloudnet.driver.template.TemplateStorage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.zip.ZipInputStream;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

public class LocalTemplateStorage implements TemplateStorage {

  public static final String LOCAL_TEMPLATE_STORAGE = "local";
  protected static final Logger LOGGER = LogManager.logger(LocalTemplateStorage.class);

  private final Path storageDirectory;

  public LocalTemplateStorage(@NonNull Path storageDirectory) {
    this.storageDirectory = storageDirectory;
    FileUtils.createDirectory(storageDirectory);
  }

  @Override
  public @NonNull String name() {
    return LOCAL_TEMPLATE_STORAGE;
  }

  @Override
  public boolean deployDirectory(
    @NonNull Path directory,
    @NonNull ServiceTemplate target,
    @Nullable Predicate<Path> fileFilter
  ) {
    if (Files.exists(directory)) {
      FileUtils.copyDirectory(
        directory,
        this.getTemplatePath(target),
        fileFilter == null ? null : fileFilter::test);
      return true;
    }
    return false;
  }

  @Override
  public boolean deploy(@NonNull InputStream inputStream, @NonNull ServiceTemplate target) {
    FileUtils.extractZipStream(new ZipInputStream(inputStream), this.getTemplatePath(target));
    return true;
  }

  @Override
  public boolean copy(@NonNull ServiceTemplate template, @NonNull Path directory) {
    FileUtils.copyDirectory(this.getTemplatePath(template), directory);
    return true;
  }

  @Override
  public @Nullable InputStream zipTemplate(@NonNull ServiceTemplate template) throws IOException {
    if (this.has(template)) {
      // create a new temp file
      var temp = FileUtils.createTempFile();
      var zippedFile = FileUtils.zipToFile(this.getTemplatePath(template), temp);
      // open a stream to the file if possible
      if (zippedFile != null) {
        return Files.newInputStream(zippedFile, StandardOpenOption.DELETE_ON_CLOSE, LinkOption.NOFOLLOW_LINKS);
      }
    }
    return null;
  }

  @Override
  public boolean delete(@NonNull ServiceTemplate template) {
    var templateDir = this.getTemplatePath(template);
    if (Files.notExists(templateDir)) {
      return false;
    } else {
      FileUtils.delete(templateDir);
      return true;
    }
  }

  @Override
  public boolean create(@NonNull ServiceTemplate template) {
    var templateDir = this.getTemplatePath(template);
    if (Files.notExists(templateDir)) {
      FileUtils.createDirectory(templateDir);
      return true;
    }

    return false;
  }

  @Override
  public boolean has(@NonNull ServiceTemplate template) {
    return Files.exists(this.getTemplatePath(template));
  }

  @Override
  public @Nullable OutputStream appendOutputStream(
    @NonNull ServiceTemplate template,
    @NonNull String path
  ) throws IOException {
    var filePath = this.getTemplatePath(template).resolve(path);
    if (Files.notExists(filePath)) {
      Files.createDirectories(filePath.getParent());
    }

    return Files.newOutputStream(filePath, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
  }

  @Override
  public @Nullable OutputStream newOutputStream(
    @NonNull ServiceTemplate template,
    @NonNull String path
  ) throws IOException {
    var filePath = this.getTemplatePath(template).resolve(path);
    if (Files.notExists(filePath)) {
      Files.createDirectories(filePath.getParent());
    }

    return Files.newOutputStream(filePath);
  }

  @Override
  public boolean createFile(
    @NonNull ServiceTemplate template,
    @NonNull String path
  ) throws IOException {
    var filePath = this.getTemplatePath(template).resolve(path);
    if (Files.exists(filePath)) {
      return false;
    } else {
      Files.createDirectories(filePath.getParent());
      Files.createFile(filePath);
      return true;
    }
  }

  @Override
  public boolean createDirectory(
    @NonNull ServiceTemplate template,
    @NonNull String path
  ) throws IOException {
    var dirPath = this.getTemplatePath(template).resolve(path);
    if (Files.exists(dirPath)) {
      return false;
    } else {
      Files.createDirectories(dirPath);
      return true;
    }
  }

  @Override
  public boolean hasFile(@NonNull ServiceTemplate template, @NonNull String path) {
    return Files.exists(this.getTemplatePath(template).resolve(path));
  }

  @Override
  public boolean deleteFile(@NonNull ServiceTemplate template, @NonNull String path) throws IOException {
    var filePath = this.getTemplatePath(template).resolve(path);
    if (Files.exists(filePath)) {
      Files.delete(filePath);
      return true;
    }
    return false;
  }

  @Override
  public @Nullable InputStream newInputStream(
    @NonNull ServiceTemplate template,
    @NonNull String path
  ) throws IOException {
    var filePath = this.getTemplatePath(template).resolve(path);
    return Files.notExists(filePath) || Files.isDirectory(filePath) ? null : Files.newInputStream(filePath);
  }

  @Override
  public @Nullable FileInfo fileInfo(
    @NonNull ServiceTemplate template,
    @NonNull String path
  ) throws IOException {
    var filePath = this.getTemplatePath(template).resolve(path);
    return Files.exists(filePath) ? FileInfo.of(filePath, Path.of(path)) : null;
  }

  @Override
  public @Nullable FileInfo[] listFiles(
    @NonNull ServiceTemplate template,
    @NonNull String dir,
    boolean deep
  ) {
    List<FileInfo> out = new ArrayList<>();
    var root = this.getTemplatePath(template).resolve(dir);
    // walk over all files
    FileUtils.walkFileTree(root, (parent, file) -> {
      try {
        out.add(FileInfo.of(file, root.relativize(file)));
      } catch (IOException ignored) {
      }
    }, deep, $ -> true);
    // collect to an array
    return out.toArray(new FileInfo[0]);
  }

  @Override
  public @NonNull Collection<ServiceTemplate> templates() {
    try {
      return Files.list(this.storageDirectory)
        .filter(Files::isDirectory)
        .flatMap(directory -> {
          try {
            return Files.list(directory);
          } catch (IOException exception) {
            return null;
          }
        })
        .filter(Objects::nonNull)
        .filter(Files::isDirectory)
        .map(path -> {
          var relative = this.storageDirectory.relativize(path);
          return ServiceTemplate.builder()
            .prefix(relative.getName(0).toString())
            .name(relative.getName(1).toString())
            .build();
        })
        .collect(Collectors.toSet());
    } catch (IOException exception) {
      LOGGER.severe("Unable to collect templates in local template storage", exception);
      return Collections.emptyList();
    }
  }

  @Override
  public void close() {
  }

  protected @NonNull Path getTemplatePath(@NonNull ServiceTemplate template) {
    return this.storageDirectory.resolve(template.prefix()).resolve(template.name());
  }
}

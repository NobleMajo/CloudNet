/*
 * Copyright 2019-2024 CloudNetService team & contributors
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

package eu.cloudnetservice.modules.s3.config;

import java.net.URI;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public record S3TemplateStorageConfig(
  @NonNull String name,
  @NonNull String bucket,
  @NonNull String region,
  @NonNull String accessKey,
  @NonNull String secretKey,
  @Nullable String endpointOverride,
  boolean accelerateMode,
  boolean pathStyleAccess,
  boolean chunkedEncoding,
  boolean checksumValidation,
  boolean dualstackEndpointEnabled
) {

  private static final Logger LOGGER = LoggerFactory.getLogger(S3TemplateStorageConfig.class);

  public @Nullable URI resolveEndpointOverride() {
    if (this.endpointOverride != null) {
      try {
        var uri = URI.create(this.endpointOverride);
        // validate the given uri
        if (uri.getScheme() != null) {
          return uri;
        }
        LOGGER.error("Endpoint override for s3 config must contain a valid scheme: {}", this.endpointOverride);
      } catch (IllegalArgumentException exception) {
        LOGGER.error("Unable to parse uri for s3 endpoint override: {}", this.endpointOverride);
      }
    }
    // illegal uri or not given
    return null;
  }
}

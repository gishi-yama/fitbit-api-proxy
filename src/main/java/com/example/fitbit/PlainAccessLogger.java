package com.example.fitbit;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Log4j2
@Repository
public class PlainAccessLogger {

  @Value("${access.logging.path}")
  private String loggingPath;

  public void log(URI uri) {
    var query = uri.getQuery();
    Optional.ofNullable(query).ifPresent(this::writeFile);
  }
  
  public void log(String gakuseki) {
    Optional.ofNullable(gakuseki).ifPresent(this::writeFile);
  }

  private void writeFile(String query) {
    if (!StringUtils.hasText(loggingPath)) {
      try {
        var timestamp = ZonedDateTime.now(ZoneId.of("Asia/Tokyo")).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        var line = String.join(",", query, timestamp);
        Files.write(Paths.get(loggingPath), List.of(line), StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }
}
package com.attestorforensics.mobifumecore.model.element.filter;

import com.attestorforensics.mobifumecore.util.FileManager;
import com.google.gson.Gson;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class FilterFileHandler {

  private final File filterFolder;

  public FilterFileHandler() {
    filterFolder = new File(FileManager.getInstance().getDataFolder(), "filter");
    if (!filterFolder.exists()) {
      filterFolder.mkdir();
    }
  }

  public List<Filter> loadFilters() {
    List<Filter> filters = new ArrayList<>();

    Gson gson = new Gson();
    File[] files = filterFolder.listFiles();
    if (files == null) {
      return filters;
    }
    for (File filterJsonFile : files) {
      try {
        String content = new String(Files.readAllBytes(filterJsonFile.toPath()));
        MobiFilter filter = gson.fromJson(content, MobiFilter.class);
        filter.setFileHandler(this);
        filters.add(filter);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    return filters;
  }

  public void saveFilter(Filter filter) {
    File filterFile = new File(filterFolder, filter.getId() + ".filter");
    Gson gson = new Gson();
    String json = gson.toJson(filter);
    try {
      Files.write(filterFile.toPath(), json.getBytes(StandardCharsets.UTF_8));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}

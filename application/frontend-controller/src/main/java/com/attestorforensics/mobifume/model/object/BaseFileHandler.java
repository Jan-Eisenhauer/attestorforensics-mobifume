package com.attestorforensics.mobifume.model.object;

import com.attestorforensics.mobifume.util.FileManager;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

public class BaseFileHandler {

  private File baseFile;

  public BaseFileHandler() {
    baseFile = new File(FileManager.getInstance().getDataFolder(), "bases");
    if (!baseFile.exists()) {
      try {
        baseFile.createNewFile();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  public BaseContent loadBase(String id) {
    List<BaseContent> bases = getBaseArray();
    if (bases == null) {
      return null;
    }
    return bases.stream().filter(bc -> bc.getId().equals(id)).findFirst().orElse(null);
  }

  private List<BaseContent> getBaseArray() {
    List<BaseContent> bases = new ArrayList<>();
    try {
      Type type = new TypeToken<List<BaseContent>>() {
      }.getType();
      Gson gson = new Gson();
      String content = new String(Files.readAllBytes(baseFile.toPath()));
      bases = gson.fromJson(content, type);
    } catch (IOException e) {
      e.printStackTrace();
    }

    return bases;
  }

  public void saveBase(Base base) {
    List<BaseContent> bases = getBaseArray();
    if (bases == null) {
      bases = new ArrayList<>();
    }

    BaseContent baseContent = bases.stream()
        .filter(bc -> bc.getId().equals(base.getId()))
        .findFirst()
        .orElse(null);
    if (baseContent != null) {
      baseContent.setHumidityOffset(base.getHumidityOffset().orElse(null));
      baseContent.setHumidityGradient(base.getHumidityGradient().orElse(null));
      baseContent.setTemperatureOffset(base.getTemperatureOffset().orElse(null));
      baseContent.setTemperatureGradient(base.getTemperatureGradient().orElse(null));
    } else {
      bases.add(new BaseContent(base.getId(), base.getHumidityOffset().orElse(null),
          base.getHumidityGradient().orElse(null), base.getTemperatureOffset().orElse(null),
          base.getTemperatureGradient().orElse(null)));
    }

    Type type = new TypeToken<List<BaseContent>>() {
    }.getType();
    Gson gson = new Gson();
    String json = gson.toJson(bases, type);
    try {
      Files.write(baseFile.toPath(), json.getBytes(StandardCharsets.UTF_8));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Getter
  @Setter
  @AllArgsConstructor
  public static class BaseContent {

    private String id;
    private Float humidityOffset;
    private Float humidityGradient;
    private Float temperatureOffset;
    private Float temperatureGradient;
  }
}

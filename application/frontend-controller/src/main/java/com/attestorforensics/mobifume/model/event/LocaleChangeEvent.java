package com.attestorforensics.mobifume.model.event;

import com.attestorforensics.mobifume.model.listener.Event;
import java.util.Locale;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class LocaleChangeEvent implements Event {

  private final Locale locale;
}

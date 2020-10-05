package com.attestorforensics.mobifume.model.event;

import com.attestorforensics.mobifume.model.listener.Event;
import com.attestorforensics.mobifume.model.object.Device;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class BaseErrorResolvedEvent implements Event {

  private final Device base;
  private final BaseErrorEvent.ErrorType resolved;
}

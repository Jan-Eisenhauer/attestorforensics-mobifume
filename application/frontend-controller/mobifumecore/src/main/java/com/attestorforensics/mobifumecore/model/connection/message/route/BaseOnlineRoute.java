package com.attestorforensics.mobifumecore.model.connection.message.route;

import com.attestorforensics.mobifumecore.model.connection.message.in.BaseOnline;

public class BaseOnlineRoute implements MessageRoute<BaseOnline> {

  private BaseOnlineRoute() {
  }

  public static BaseOnlineRoute create() {
    return new BaseOnlineRoute();
  }

  @Override
  public void onReceived(BaseOnline message) {

  }
}

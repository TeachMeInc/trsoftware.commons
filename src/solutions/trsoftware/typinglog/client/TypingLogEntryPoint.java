/*
 * Copyright 2021 TR Software Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package solutions.trsoftware.typinglog.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.RootPanel;
import solutions.trsoftware.commons.shared.text.TypingLog;
import solutions.trsoftware.commons.shared.text.TypingLogFormatV1;
import solutions.trsoftware.typinglog.client.replay.TypingLogReplayPlayer;

/**
 * @author Alex, 6/8/2017
 */
public class TypingLogEntryPoint implements EntryPoint {
  public void onModuleLoad() {
    String typingLogStr = getHostpageStringVariable("typingLog");
    if (typingLogStr != null) {
      TypingLog typingLog = TypingLogFormatV1.parseTypingLog(typingLogStr);
      RootPanel playerContainer = RootPanel.get("typingLogReplayPlayer");
      if (playerContainer != null)
        playerContainer.add(new TypingLogReplayPlayer(typingLog));
    }
  }

  protected static native String getHostpageStringVariable(String name)/*-{
    return $wnd[name];
  }-*/;
}

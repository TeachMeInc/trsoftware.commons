/*
 *  Copyright 2017 TR Software Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *  use this file except in compliance with the License. You may obtain a copy of
 *  the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 */

package solutions.trsoftware.commons.client.util;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Timer;
import solutions.trsoftware.commons.shared.util.callables.Function0;

/**
 * Runs the timer until a condition is satisfied, at which point the timer is
 * canceled and the passed-in command is called.  Optionally, can also
 * specify a command to be invoked every time the condition is not met (caution:
 * this can happen quite frequently, depending on the timer's interval.
 *
 * Another way of using this class is to provide onConditionNotMet but not onConditionMet.
 *
 * It's easiest to just use one of the static methods in this class.
 *
 * @author Alex
 */
public class Waiter extends Timer {

  // TODO: merge this class with RetryableCommand (use one to implement the other)

  private Function0<Boolean> condition;
  private Command onConditionMet;
  private Command onConditionNotMet;

  public Waiter(Function0<Boolean> condition, Command onConditionMet) {
    this.condition = condition;
    this.onConditionMet = onConditionMet;
  }

  public Waiter(Function0<Boolean> condition, Command onConditionMet, Command onConditionNotMet) {
    this(condition, onConditionMet);
    this.onConditionNotMet = onConditionNotMet;
  }

  /**
   * This method will be called when a timer fires. Override it to implement the
   * timer's logic.
   */
  @Override
  public final void run() {
    if (condition.call()) {
      // condition met
      cancel();
      executeIfNotNull(onConditionMet);
    } else {
      executeIfNotNull(onConditionNotMet);
    }
  }

  private void executeIfNotNull(Command command) {
    if (command != null)
      command.execute();
  }

  public static void repeatUntilConditionMet(Function0<Boolean> condition, Command onConditionNotMet, int periodMillis) {
    new Waiter(condition, null, onConditionNotMet).scheduleRepeating(periodMillis);
  }

  public static void waitForCondition(Function0<Boolean> condition, Command onConditionMet, int periodMillis) {
    new Waiter(condition, onConditionMet).scheduleRepeating(periodMillis);
  }
  
}
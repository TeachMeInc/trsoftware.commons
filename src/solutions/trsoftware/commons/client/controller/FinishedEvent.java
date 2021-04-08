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

package solutions.trsoftware.commons.client.controller;

/**
 * Fired from {@link BaseRpcAction#onFinished()}, which is invoked after the RPC call returns (either successfully or not).
 * This event will usually be preceded by a {@link SuccessEvent} or a {@link FailureEvent}.
 *
 * The action instance that fired this event can be obtained by calling {@link #getSource()}
 *
 * @author Alex
 * @since 2/14/2018
 */
public class FinishedEvent extends RpcEvent<FinishedEvent.Handler> {

  public interface Handler extends RpcEvent.Handler {
    void onFinished(FinishedEvent event);
  }

  public static final Type<Handler> TYPE = new Type<Handler>();

  @Override
  public Type<Handler> getAssociatedType() {
    return TYPE;
  }

  @Override
  protected void dispatch(Handler handler) {
    handler.onFinished(this);
  }


}

package org.szimano.game;
/*
 * Copyright 2013 Red Hat, Inc.
 *
 * Red Hat licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 */

import com.pi4j.io.gpio.PinState;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.platform.Verticle;

import java.util.concurrent.atomic.AtomicBoolean;

/*
This is a simple Java verticle which receives `ping` messages on the event bus and sends back `pong` replies
 */
public class GameSolverVerticle extends Verticle {

    public static final String POINTS = "points";
    public static final String PIGAME = "pigame";

    public void start() {

        vertx.sharedData().getMap(PIGAME).put(POINTS, 0);

        final AtomicBoolean winState = new AtomicBoolean(false);

        vertx.eventBus().registerHandler("winbus", new Handler<Message<String>>() {
            @Override
            public void handle(Message<String> message) {

                winState.set(message.body().equals("1") ? true : false);
            }
        });

        vertx.eventBus().registerHandler("buttonbus", new Handler<Message<String>>() {
            @Override
            public void handle(Message<String> message) {

                if (PinState.HIGH.equals(PinState.getState(Integer.valueOf(message.body())))) {
                    if (winState.get() == true) {
                        container.logger().info("YOU WIN BITCH!");
                        vertx.eventBus().send("speedygonzalezbus", "blah");

                        vertx.sharedData().getMap(PIGAME).put(POINTS, (Integer)vertx.sharedData().getMap(PIGAME).get(POINTS) + 1);
                    }
                    else {
                        container.logger().info("YOU LOOSE!");
                        vertx.eventBus().send("bzykbus", "blah");

                        vertx.sharedData().getMap(PIGAME).put(POINTS, (Integer)vertx.sharedData().getMap(PIGAME).get(POINTS) - 1);
                    }
                }
            }
        });
    }
}

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

import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.platform.Verticle;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/*
This is a simple Java verticle which receives `ping` messages on the event bus and sends back `pong` replies
 */
public class RandomLedVerticle extends Verticle {

    public void start() {

        final AtomicInteger i = new AtomicInteger(50000);

        final Random r = new Random();
        vertx.setTimer(i.get() / 100, new Handler<Long>() {
            @Override
            public void handle(Long timerID) {
                vertx.eventBus().send("ledbus", r.nextInt(3) + "" + r.nextInt(2));

                vertx.setTimer(Math.max(1, i.get() / 100), this);
            }
        });

        vertx.eventBus().registerHandler("speedygonzalezbus", new Handler<Message<String>>() {

            @Override
            public void handle(Message<String> stringMessage) {
                i.set(i.get()-1000);
            }
        });


    }
}

package org.szimano;
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

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.platform.Verticle;

/*
This is a simple Java verticle which receives `ping` messages on the event bus and sends back `pong` replies
 */
public class HardwareVerticle extends Verticle {

    public void start() {

        // create gpio controller
        final GpioController gpio = GpioFactory.getInstance();

        System.out.println("GPIO PSTRYK LOADED");

        final GpioPinDigitalInput pin = gpio.provisionDigitalInputPin(RaspiPin.GPIO_02, "MyPstryk");

        vertx.setPeriodic(100, new Handler<Long>() {
            @Override
            public void handle(Long timerID) {
                vertx.eventBus().send("buttonbus", String.valueOf(pin.getState().getValue()));
            }
        });

        final GpioPinDigitalOutput ledPin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_00, "MyLED", PinState.LOW);
        final GpioPinDigitalOutput bzykPin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_03, "MyBzyk", PinState.LOW);

        vertx.eventBus().registerHandler("outputbus", new Handler<Message<String>>() {
            @Override
            public void handle(Message<String> message) {
                container.logger().info("Got state: " + message.body());

                ledPin.setState(PinState.getState(Integer.parseInt(message.body())));
                bzykPin.setState(PinState.getState(Integer.parseInt(message.body())));

            }
        });
    }
}

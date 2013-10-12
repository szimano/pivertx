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

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.platform.Verticle;

import java.util.concurrent.atomic.AtomicBoolean;

/*
This is a simple Java verticle which receives `ping` messages on the event bus and sends back `pong` replies
 */
public class HardwareVerticle extends Verticle {

    public void start() {

        // create gpio controller
        final GpioController gpio = GpioFactory.getInstance();

        System.out.println("GPIO PSTRYK LOADED");

        final GpioPinDigitalInput pin = gpio.provisionDigitalInputPin(RaspiPin.GPIO_02, "MyPstryk");

        final AtomicBoolean buttonState = new AtomicBoolean(false);

        vertx.setPeriodic(10, new Handler<Long>() {
            @Override
            public void handle(Long timerID) {

                int buttonValue = pin.getState().getValue();

                boolean currentButtonState = buttonValue == 0 ? false : true;
                if (buttonState.get() != currentButtonState) {
                    buttonState.set(currentButtonState);
                    vertx.eventBus().send("buttonbus", String.valueOf(buttonValue));
                }
            }
        });

        final GpioPinDigitalOutput ledPin1 = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_00, "MyLED1", PinState.HIGH);
        final GpioPinDigitalOutput ledPin2 = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_04, "MyLED2", PinState.HIGH);
        final GpioPinDigitalOutput ledPin3 = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_05, "MyLED3", PinState.HIGH);
        final GpioPinDigitalOutput bzykPin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_03, "MyBzyk", PinState.LOW);

        vertx.eventBus().registerHandler("ledbus", new Handler<Message<String>>() {
            @Override
            public void handle(Message<String> message) {
                PinState state = PinState.getState(Integer.parseInt(message.body().substring(1, 2)));
                switch(message.body().charAt(0)){
                    case '0': ledPin1.setState(state);
                        break;
                    case '1': ledPin2.setState(state);
                        break;
                    case '2': ledPin3.setState(state);
                        break;
                }
                if((ledPin1.getState().getValue() & ledPin2.getState().getValue() & ledPin3.getState().getValue()) == 1) {
                    vertx.eventBus().send("winbus", "1");
                }
                else {
                    vertx.eventBus().send("winbus", "0");
                }
            }
        });

        vertx.eventBus().registerHandler("bzykbus", new Handler<Message<String>>() {

            @Override
            public void handle(Message<String> stringMessage) {
                bzykPin.setState(PinState.HIGH);
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                }
                bzykPin.setState(PinState.LOW);
            }
        });
    }
}

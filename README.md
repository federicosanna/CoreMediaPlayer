# CoreMediaPlayer

*Play, mix and create music with gestures using a Primo Core board and your smartphone.*

### Description

This repository contains the Arduino code to be uploaded on the board and the Android Studio one for the android app 
that should be uploaded on your smartphone.
This example shows how to to use the sensors on the **Primo Core** to make your phone play and combine different tracks and
sounds with a gesture of your hand. An Android Application is used to process the commands coming from the Primo Core and
and make your phone play the tracks enbedded in the application. 

You can choose which gesture corresponds to which sound

### Hardware

- [Arduino Primo Core](http://www.arduino.org/products/boards/arduino-primo-core)

- Coin battery CR2032

- Smartphone with Bluetooth enabled

### Circuit

It doesn’t need any circuit but it need an [Arduino Primo](http://www.arduino.org/products/boards/arduino-primo) 

or an external programmer only  for uploading the sketch. 

For more information, about how to upload a sketch, visit the [Getting Started](http://www.arduino.org/learning/getting-started/getting-started-with-arduino-primo-core).

### Code

Refer to the file : **MouseControlSmartphone.ino** in this folder

### Warning

Use the *Arduino IDE 1.8.x* and check that you have already downloaded the corresponding platform (*Arduino NRF52 Boards*) 

from the **Board Manager**.

### Output

After connecting your smartphone to the board, you will be able to move the mouse with your board.

Tilt the board on the direction where you want to move the pointer, tap on it once to press the mouse, 

and tap again to release.



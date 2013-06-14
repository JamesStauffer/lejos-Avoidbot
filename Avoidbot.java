import java.lang.System;
import java.util.*;
import lejos.nxt.*;
import lejos.util.*;

/**
 * Ports:
 * B: Right wheel
 * C: Left Wheel
 * 1: Sound Sensor -- controls speed, yell to slow down
 * 2: Light Sensor -- headlight
 * 3: Touch Sensor -- stops program
 * 4: Distance Sensor -- point forward, controls turning
 *
 * Slows down when nearing an obstable or with loud noises. When really close to an obstacle it turns
 */
public class Avoidbot {
 
    public static void main (String[] aArg) throws Exception {
        System.out.println("Started AvoidBot");
        new ButtonWatcherThread(new TimerListener() {
            public void timedOut() {
                running = !running;
            }
        }).start();
        final float circumference = 17.5f;
        final int distanceBuffer = 50;
        final int rotateDistance = 80;
        UltrasonicSensor distSensor = new UltrasonicSensor(SensorPort.S4);
        LightSensor litSensor = new LightSensor(SensorPort.S2);
        litSensor.setFloodlight(true);
  
        int endTime = (int)System.currentTimeMillis() + 60*1000;
        boolean blinkLight = true;
        Random random = new Random();
  
        while(endTime > (int)System.currentTimeMillis() && running) {
            int cmDist = distSensor.getDistance();
            while(running && cmDist > distanceBuffer) {//Far from obstacle so go forward
                System.out.println("Distance: " + cmDist);
                int distance = cmDist - distanceBuffer;
                if(distance > 5) {
                    distance = 3;
                }
                int degrees = (int)(360 * distance / circumference);
                setSpeed();
                motorRight.rotate(degrees, true);
                motorLeft.rotate(degrees, true);
                cmDist = distSensor.getDistance();
                litSensor.setFloodlight(blinkLight = !blinkLight);
                System.out.println("Light: " + litSensor.readNormalizedValue());
            }
            boolean turnPositive = random.nextBoolean();
            System.out.println("Turn " + (turnPositive ? "right" : "left"));
            while(running && cmDist < distanceBuffer) {//Close to obstacle so turn
                System.out.println("Distance: " + cmDist);
                setSpeed();
                turn(turnPositive, 180);
                cmDist = distSensor.getDistance();
            }
            try {
                Thread.sleep(500);
            } catch(InterruptedException ie) {
             //Ignore
            }
        }
        System.out.println("AvoidBot Sleep");
        try {
            Thread.sleep(1000);
        }catch(InterruptedException ie) {
        }
    }

    private static void turn(boolean right, int degrees) {
        if(right) {
            motorRight.rotate(degrees, true);
            motorLeft.rotate(-degrees);
        } else {
            motorRight.rotate(-degrees, true);
            motorLeft.rotate(degrees);
        }
    }

    public static void setSpeed() {
        int soundLevel = sound.readValue();
        System.out.println("Sound: " + soundLevel);
        int speed = (100 - soundLevel) * 9;
        System.out.println("Speed: " + speed);
        motorRight.setSpeed(speed);
        motorLeft.setSpeed(speed);
    }
 
    static NXTRegulatedMotor motorRight = Motor.B;
    static NXTRegulatedMotor motorLeft = Motor.C;
    private static SoundSensor sound = new SoundSensor(SensorPort.S1);
    private static boolean running = true;
}

class ButtonWatcherThread extends Thread {
    public ButtonWatcherThread(TimerListener tl) {
        this.tl = tl;
        this.setDaemon(true);
    }

    public void run() {
        wasPressedLast = touchSensor.isPressed();
        while(true) {
            boolean isPressed = touchSensor.isPressed();
            if(wasPressedLast != isPressed) {
                if(isPressed) {
                    System.out.println("Button pressed");
                    try {
                        Thread.sleep(1000);
                    }catch(InterruptedException ie) {
                    }
                    System.exit(0);
                }
                wasPressedLast = isPressed;
            }
            try {
                Thread.sleep(100);
            } catch(InterruptedException ie) {
             //Ignore
            }
        }
        
    }
    private TimerListener tl;
    private boolean wasPressedLast;
    private TouchSensor touchSensor = new TouchSensor(SensorPort.S3);
}


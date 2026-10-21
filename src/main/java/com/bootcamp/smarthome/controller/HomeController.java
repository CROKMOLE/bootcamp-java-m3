package com.bootcamp.smarthome.controller;

import com.bootcamp.smarthome.device.Device;
import com.bootcamp.smarthome.device.SmartLock;
import com.bootcamp.smarthome.exception.DeviceNotFoundException;
import com.bootcamp.smarthome.exception.DeviceOfflineException;
import com.bootcamp.smarthome.exception.HomeAutomationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Central hub that manages all registered smart devices.
 * <p>
 * Devices are stored in a fixed-size array (maximum {@value #MAX_DEVICES}).
 * The controller routes commands to devices by their ID.
 */
public class HomeController {

    public static final int MAX_DEVICES = 8;

    private final Device[] devices = new Device[MAX_DEVICES];
    private int deviceCount = 0;
    private static final Logger logger = LoggerFactory.getLogger(SmartLock.class);

    // -------------------------------------------------------------------------
    // Device registration
    // -------------------------------------------------------------------------

    /**
     * Registers a new device with the controller.
     * The controller accepts at most {@value #MAX_DEVICES} devices.
     *
     * @param device the device to register
     * @throws IllegalStateException if the device limit has been reached
     */
    public void addDevice(Device device) {
        if (deviceCount >= MAX_DEVICES) {
            throw new IllegalStateException(
                    "Cannot add device '" + device.getDeviceId() +
                            "': controller is at maximum capacity (" + MAX_DEVICES + ").");
        }
        devices[deviceCount] = device;
        deviceCount++;
        System.out.println("Device registered: " + device);
    }

    // -------------------------------------------------------------------------
    // Device lookup
    // -------------------------------------------------------------------------

    /**
     * Finds a registered device by its ID.
     * <p>
     * Returns {@code null} when no matching device is found.
     */
    public Device findDevice(String deviceId) {
        //error was an selection of the array element with index which is not exist.
        //the reason is condition i <= deviceCount. That state adds one more cycle with usage of element of array that is not exist.
        //fix is simple change <= to <. It will remove last cycle that trying to use element with index that is not exists.
        //array starts from zero so array that has 4 elements will not have element with index == 4. In real it will be [0, 1, 2, 3]
        for (int i = 0; i < deviceCount; i++) {
            if (devices[i] != null && devices[i].getDeviceId().equals(deviceId)) {
                return devices[i];
            }
        }
        return null;
    }

    // -------------------------------------------------------------------------
    // Command routing
    // -------------------------------------------------------------------------

    /**
     * Parses {@code fullCommand}, resolves the target device, and delegates
     * execution to {@link Device#executeCommand(String)}.
     * <p>
     * Full command format: {@code "DEVICE_ID ACTION [VALUE]"}
     * Example: {@code "LIGHT_01 SET_BRIGHTNESS 75"}
     *
     * @param fullCommand the full command string
     */
    public void sendCommand(String fullCommand) {
        String deviceId = CommandParser.extractDeviceId(fullCommand);
        String command = CommandParser.extractCommand(fullCommand);

        logger.debug("A command is received for {}. Raw command: {}", deviceId, fullCommand);

        try {
            Device device = findDevice(deviceId);

            if (device == null) {
                throw new HomeAutomationException("Device not found: " + deviceId);
            }

            if (!device.isOnline()) {
                logger.warn("The target device is offline and the command is skipped");
                throw new DeviceOfflineException("WARNING: Device '" + deviceId + "' is offline — command skipped.");
            }

            device.executeCommand(command);

            logger.info("Command executed successfully");
        } catch (HomeAutomationException e) {
            logger.error("Command '{}' failed for device '{}'", fullCommand, deviceId);
        } finally {
            logger.info("Command processing ended for device {}", deviceId);
        }
    }

    // -------------------------------------------------------------------------
    // Utility
    // -------------------------------------------------------------------------

    /**
     * Prints the status of every registered device.
     */
    public void printAllDevices() {
        System.out.println("=== Registered Devices (" + deviceCount + "/" + MAX_DEVICES + ") ===");
        for (int i = 0; i < deviceCount; i++) {
            System.out.println("  " + devices[i]);
        }
    }

    public int getDeviceCount() {
        return deviceCount;
    }
}

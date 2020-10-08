/*  Copyright (C) 2016-2020 Andreas Shimokawa, Carsten Pfeiffer, Jean-François
    Greffier

    This file is part of Gadgetbridge.

    Gadgetbridge is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published
    by the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Gadgetbridge is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>. */
package custom.freeyourgadget.MiBandApp.service.devices.miscale2;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.UUID;

import custom.freeyourgadget.MiBandApp.deviceevents.GBDeviceEventVersionInfo;
import custom.freeyourgadget.MiBandApp.impl.GBDevice;
import custom.freeyourgadget.MiBandApp.model.Alarm;
import custom.freeyourgadget.MiBandApp.model.CalendarEventSpec;
import custom.freeyourgadget.MiBandApp.model.CallSpec;
import custom.freeyourgadget.MiBandApp.model.CannedMessagesSpec;
import custom.freeyourgadget.MiBandApp.model.MusicSpec;
import custom.freeyourgadget.MiBandApp.model.MusicStateSpec;
import custom.freeyourgadget.MiBandApp.model.NotificationSpec;
import custom.freeyourgadget.MiBandApp.model.WeatherSpec;
import custom.freeyourgadget.MiBandApp.service.btle.AbstractBTLEDeviceSupport;
import custom.freeyourgadget.MiBandApp.service.btle.GattCharacteristic;
import custom.freeyourgadget.MiBandApp.service.btle.GattService;
import custom.freeyourgadget.MiBandApp.service.btle.TransactionBuilder;
import custom.freeyourgadget.MiBandApp.service.btle.actions.SetDeviceStateAction;
import custom.freeyourgadget.MiBandApp.service.btle.profiles.IntentListener;
import custom.freeyourgadget.MiBandApp.service.btle.profiles.deviceinfo.DeviceInfoProfile;
import custom.freeyourgadget.MiBandApp.util.GB;

public class MiScale2DeviceSupport extends AbstractBTLEDeviceSupport {

    private static final Logger LOG = LoggerFactory.getLogger(MiScale2DeviceSupport.class);
    private static final String UNIT_KG = "kg";
    private static final String UNIT_LBS = "lb";
    private static final String UNIT_JIN = "jīn";
    private final DeviceInfoProfile<MiScale2DeviceSupport> deviceInfoProfile;
    private final GBDeviceEventVersionInfo versionCmd = new GBDeviceEventVersionInfo();
    private final IntentListener mListener = new IntentListener() {
        @Override
        public void notify(Intent intent) {
            String s = intent.getAction();
            if (s.equals(DeviceInfoProfile.ACTION_DEVICE_INFO)) {
                handleDeviceInfo((custom.freeyourgadget.MiBandApp.service.btle.profiles.deviceinfo.DeviceInfo) intent.getParcelableExtra(DeviceInfoProfile.EXTRA_DEVICE_INFO));
            }
        }
    };

    public MiScale2DeviceSupport() {
        super(LOG);
        addSupportedService(GattService.UUID_SERVICE_GENERIC_ACCESS);
        addSupportedService(GattService.UUID_SERVICE_GENERIC_ATTRIBUTE);
        addSupportedService(GattService.UUID_SERVICE_DEVICE_INFORMATION);
        addSupportedService(GattService.UUID_SERVICE_BODY_COMPOSITION);
        addSupportedService(UUID.fromString("00001530-0000-3512-2118-0009af100700"));

        deviceInfoProfile = new DeviceInfoProfile<>(this);
        deviceInfoProfile.addListener(mListener);
        addSupportedProfile(deviceInfoProfile);
    }

    @Override
    protected TransactionBuilder initializeDevice(TransactionBuilder builder) {
        builder.add(new SetDeviceStateAction(getDevice(), GBDevice.State.INITIALIZING, getContext()));

        LOG.debug("Requesting Device Info!");
        deviceInfoProfile.requestDeviceInfo(builder);
        builder.add(new SetDeviceStateAction(getDevice(), GBDevice.State.INITIALIZED, getContext()));

        // Weight and body composition
        builder.setGattCallback(this);
        builder.notify(getCharacteristic(GattCharacteristic.UUID_CHARACTERISTIC_BODY_COMPOSITION_MEASUREMENT), true);

        return builder;
    }

    @Override
    public boolean onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        super.onCharacteristicChanged(gatt, characteristic);

        UUID characteristicUUID = characteristic.getUuid();
        if (characteristicUUID.equals(GattCharacteristic.UUID_CHARACTERISTIC_BODY_COMPOSITION_MEASUREMENT)) {
            final byte[] data = characteristic.getValue();

            boolean stabilized = testBit(data[1], 5) && !testBit(data[1], 7);
            boolean isLbs = testBit(data[1], 0);
            boolean isJin = testBit(data[1], 4);
            boolean isKg = !(isLbs && isJin);
            String unit = "";
            if (isKg) {
                unit = UNIT_KG;
            } else if (isLbs) {
                unit = UNIT_LBS;
            } else if (isJin) {
                unit = UNIT_JIN;
            }

            if (stabilized) {
                int year = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 2);
                int month = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 4);
                int day = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 5);
                int hour = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 6);
                int minute = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 7);
                int second = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 8);
                Calendar c = GregorianCalendar.getInstance();
                c.set(year, month - 1, day, hour, minute, second);
                Date date = c.getTime();
                float weight = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 11) / (isKg ? 200.0f : 100.0f);
                handleWeightInfo(date, weight, unit);
            }

            return true;
        }

        return false;
    }

    private boolean testBit(byte value, int offset) {
        return ((value >> offset) & 1) == 1;
    }

    private void handleDeviceInfo(custom.freeyourgadget.MiBandApp.service.btle.profiles.deviceinfo.DeviceInfo info) {
        LOG.warn("Device info: " + info);
        versionCmd.hwVersion = info.getHardwareRevision();
        versionCmd.fwVersion = info.getSoftwareRevision();
        handleGBDeviceEvent(versionCmd);
    }

    private void handleWeightInfo(Date date, float weight, String unit) {
        // TODO
        LOG.warn("Weight info: " + weight + unit);
        GB.toast(weight + unit, Toast.LENGTH_SHORT, GB.INFO);
    }

    @Override
    public boolean useAutoConnect() {
        return false;
    }

    @Override
    public void onNotification(NotificationSpec notificationSpec) {

    }

    @Override
    public void onDeleteNotification(int id) {

    }

    @Override
    public void onSetTime() {

    }

    @Override
    public void onSetAlarms(ArrayList<? extends Alarm> alarms) {

    }

    @Override
    public void onSetCallState(CallSpec callSpec) {

    }

    @Override
    public void onSetCannedMessages(CannedMessagesSpec cannedMessagesSpec) {

    }

    @Override
    public void onSetMusicState(MusicStateSpec stateSpec) {

    }

    @Override
    public void onSetMusicInfo(MusicSpec musicSpec) {

    }

    @Override
    public void onEnableRealtimeSteps(boolean enable) {

    }

    @Override
    public void onInstallApp(Uri uri) {

    }

    @Override
    public void onAppInfoReq() {

    }

    @Override
    public void onAppStart(UUID uuid, boolean start) {

    }

    @Override
    public void onAppDelete(UUID uuid) {

    }

    @Override
    public void onAppConfiguration(UUID appUuid, String config, Integer id) {

    }

    @Override
    public void onAppReorder(UUID[] uuids) {

    }

    @Override
    public void onFetchRecordedData(int dataTypes) {

    }

    @Override
    public void onReset(int flags) {

    }

    @Override
    public void onHeartRateTest() {

    }

    @Override
    public void onEnableRealtimeHeartRateMeasurement(boolean enable) {

    }

    @Override
    public void onFindDevice(boolean start) {

    }

    @Override
    public void onSetConstantVibration(int integer) {

    }

    @Override
    public void onScreenshotReq() {

    }

    @Override
    public void onEnableHeartRateSleepSupport(boolean enable) {

    }

    @Override
    public void onSetHeartRateMeasurementInterval(int seconds) {

    }

    @Override
    public void onAddCalendarEvent(CalendarEventSpec calendarEventSpec) {

    }

    @Override
    public void onDeleteCalendarEvent(byte type, long id) {

    }

    @Override
    public void onSendConfiguration(String config) {

    }

    @Override
    public void onReadConfiguration(String config) {

    }

    @Override
    public void onTestNewFunction() {

    }

    @Override
    public void onSendWeather(WeatherSpec weatherSpec) {

    }
}

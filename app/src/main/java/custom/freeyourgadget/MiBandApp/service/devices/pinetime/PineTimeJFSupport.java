/*  Copyright (C) 2020 Andreas Shimokawa

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
package custom.freeyourgadget.MiBandApp.service.devices.pinetime;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Intent;
import android.net.Uri;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
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
import custom.freeyourgadget.MiBandApp.service.btle.BLETypeConversions;
import custom.freeyourgadget.MiBandApp.service.btle.GattCharacteristic;
import custom.freeyourgadget.MiBandApp.service.btle.GattService;
import custom.freeyourgadget.MiBandApp.service.btle.TransactionBuilder;
import custom.freeyourgadget.MiBandApp.service.btle.actions.SetDeviceStateAction;
import custom.freeyourgadget.MiBandApp.service.btle.profiles.IntentListener;
import custom.freeyourgadget.MiBandApp.service.btle.profiles.alertnotification.AlertCategory;
import custom.freeyourgadget.MiBandApp.service.btle.profiles.alertnotification.AlertNotificationProfile;
import custom.freeyourgadget.MiBandApp.service.btle.profiles.alertnotification.NewAlert;
import custom.freeyourgadget.MiBandApp.service.btle.profiles.alertnotification.OverflowStrategy;
import custom.freeyourgadget.MiBandApp.service.btle.profiles.deviceinfo.DeviceInfoProfile;

public class PineTimeJFSupport extends AbstractBTLEDeviceSupport {

    private static final Logger LOG = LoggerFactory.getLogger(PineTimeJFSupport.class);
    private final GBDeviceEventVersionInfo versionCmd = new GBDeviceEventVersionInfo();
    private final DeviceInfoProfile<PineTimeJFSupport> deviceInfoProfile;

    public PineTimeJFSupport() {
        super(LOG);
        addSupportedService(GattService.UUID_SERVICE_ALERT_NOTIFICATION);
        addSupportedService(GattService.UUID_SERVICE_CURRENT_TIME);
        addSupportedService(GattService.UUID_SERVICE_DEVICE_INFORMATION);
        deviceInfoProfile = new DeviceInfoProfile<>(this);
        IntentListener mListener = new IntentListener() {
            @Override
            public void notify(Intent intent) {
                String action = intent.getAction();
                if (DeviceInfoProfile.ACTION_DEVICE_INFO.equals(action)) {
                    handleDeviceInfo((custom.freeyourgadget.MiBandApp.service.btle.profiles.deviceinfo.DeviceInfo) intent.getParcelableExtra(DeviceInfoProfile.EXTRA_DEVICE_INFO));
                }
            }
        };
        deviceInfoProfile.addListener(mListener);
        AlertNotificationProfile<PineTimeJFSupport> alertNotificationProfile = new AlertNotificationProfile<>(this);
        addSupportedProfile(alertNotificationProfile);
        addSupportedProfile(deviceInfoProfile);
    }


    @Override
    protected TransactionBuilder initializeDevice(TransactionBuilder builder) {
        builder.add(new SetDeviceStateAction(getDevice(), GBDevice.State.INITIALIZING, getContext()));
        requestDeviceInfo(builder);
        onSetTime();
        setInitialized(builder);
        return builder;
    }


    private void setInitialized(TransactionBuilder builder) {
        builder.add(new SetDeviceStateAction(getDevice(), GBDevice.State.INITIALIZED, getContext()));
    }

    private void requestDeviceInfo(TransactionBuilder builder) {
        LOG.debug("Requesting Device Info!");
        deviceInfoProfile.requestDeviceInfo(builder);
    }

    private void handleDeviceInfo(custom.freeyourgadget.MiBandApp.service.btle.profiles.deviceinfo.DeviceInfo info) {
        LOG.warn("Device info: " + info);
        versionCmd.hwVersion = info.getHardwareRevision();
        versionCmd.fwVersion = info.getFirmwareRevision();
        handleGBDeviceEvent(versionCmd);
    }

    @Override
    public boolean useAutoConnect() {
        return false;
    }

    @Override
    public void onNotification(NotificationSpec notificationSpec) {
        TransactionBuilder builder = new TransactionBuilder("notification");
        NewAlert alert = new NewAlert(AlertCategory.CustomHuami, 1, notificationSpec.body + " "); // HACK: no idea why the last byte is swallowed
        AlertNotificationProfile<?> profile = new AlertNotificationProfile<>(this);
        profile.newAlert(builder, alert, OverflowStrategy.TRUNCATE);
        builder.queue(getQueue());
    }

    @Override
    public void onDeleteNotification(int id) {

    }

    @Override
    public void onSetTime() {
        // since this is a standard we should generalize this in Gadgetbridge (properly)
        GregorianCalendar now = BLETypeConversions.createCalendar();
        byte[] bytes = BLETypeConversions.calendarToRawBytes(now);
        byte[] tail = new byte[]{0, BLETypeConversions.mapTimeZone(now.getTimeZone(), BLETypeConversions.TZ_FLAG_INCLUDE_DST_IN_TZ)};
        byte[] all = BLETypeConversions.join(bytes, tail);

        TransactionBuilder builder = new TransactionBuilder("set time");
        builder.write(getCharacteristic(GattCharacteristic.UUID_CHARACTERISTIC_CURRENT_TIME), all);
        builder.queue(getQueue());
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
        onSetConstantVibration(start ? 0xff : 0x00);
    }

    @Override
    public void onSetConstantVibration(int intensity) {
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
    public boolean onCharacteristicChanged(BluetoothGatt gatt,
                                           BluetoothGattCharacteristic characteristic) {
        if (super.onCharacteristicChanged(gatt, characteristic)) {
            return true;
        }

        UUID characteristicUUID = characteristic.getUuid();
        LOG.info("Unhandled characteristic changed: " + characteristicUUID);
        return false;
    }

    @Override
    public boolean onCharacteristicRead(BluetoothGatt gatt,
                                        BluetoothGattCharacteristic characteristic, int status) {
        if (super.onCharacteristicRead(gatt, characteristic, status)) {
            return true;
        }
        UUID characteristicUUID = characteristic.getUuid();

        LOG.info("Unhandled characteristic read: " + characteristicUUID);
        return false;
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

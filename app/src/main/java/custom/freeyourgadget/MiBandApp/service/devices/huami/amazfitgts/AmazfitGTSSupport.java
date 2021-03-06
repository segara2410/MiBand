/*  Copyright (C) 2019-2020 Andreas Shimokawa, Manuel Ruß

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
package custom.freeyourgadget.MiBandApp.service.devices.huami.amazfitgts;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import custom.freeyourgadget.MiBandApp.GBApplication;
import custom.freeyourgadget.MiBandApp.R;
import custom.freeyourgadget.MiBandApp.devices.huami.HuamiConst;
import custom.freeyourgadget.MiBandApp.devices.huami.HuamiFWHelper;
import custom.freeyourgadget.MiBandApp.devices.huami.amazfitgts.AmazfitGTSFWHelper;
import custom.freeyourgadget.MiBandApp.model.NotificationSpec;
import custom.freeyourgadget.MiBandApp.service.btle.TransactionBuilder;
import custom.freeyourgadget.MiBandApp.service.devices.huami.amazfitbip.AmazfitBipSupport;
import custom.freeyourgadget.MiBandApp.service.devices.huami.operations.UpdateFirmwareOperationNew;
import custom.freeyourgadget.MiBandApp.util.Version;

public class AmazfitGTSSupport extends AmazfitBipSupport {
    private static final Logger LOG = LoggerFactory.getLogger(AmazfitGTSSupport.class);

    @Override
    public byte getCryptFlags() {
        return (byte) 0x80;
    }

    @Override
    protected byte getAuthFlags() {
        return 0x00;
    }

    @Override
    public void onNotification(NotificationSpec notificationSpec) {
        super.sendNotificationNew(notificationSpec, true);
    }


    @Override
    public HuamiFWHelper createFWHelper(Uri uri, Context context) throws IOException {
        return new AmazfitGTSFWHelper(uri, context);
    }

    @Override
    public UpdateFirmwareOperationNew createUpdateFirmwareOperation(Uri uri) {
        return new UpdateFirmwareOperationNew(uri, this);
    }

    @Override
    protected AmazfitGTSSupport setDisplayItems(TransactionBuilder builder) {
        if (gbDevice.getFirmwareVersion() == null) {
            LOG.warn("Device not initialized yet, won't set menu items");
            return this;
        }

        SharedPreferences prefs = GBApplication.getDeviceSpecificSharedPrefs(gbDevice.getAddress());
        Set<String> pages = prefs.getStringSet(HuamiConst.PREF_DISPLAY_ITEMS, new HashSet<>(Arrays.asList(getContext().getResources().getStringArray(R.array.pref_gts_display_items_default))));
        LOG.info("Setting display items to " + (pages == null ? "none" : pages));
        byte[] command = new byte[]{
                0x1E,
                0x00, 0x00, (byte) 0xFF, 0x01, // Status
                0x01, 0x00, (byte) 0xFF, 0x19, // PAI
                0x02, 0x00, (byte) 0xFF, 0x02, // HR
                0x03, 0x00, (byte) 0xFF, 0x03, // Workout
                0x04, 0x00, (byte) 0xFF, 0x14, // Activities
                0x05, 0x00, (byte) 0xFF, 0x04, // Weather
                0x06, 0x00, (byte) 0xFF, 0x0B, // Music
                0x07, 0x00, (byte) 0xFF, 0x06, // Notifications
                0x08, 0x00, (byte) 0xFF, 0x09, // Alarm
                0x09, 0x00, (byte) 0xFF, 0x15, // Event reminder
                0x0A, 0x00, (byte) 0xFF, 0x07, // More
                0x0B, 0x00, (byte) 0xFF, 0x13  // Settings
        };

        String[] keys = {"status", "pai", "hr", "workout", "activity", "weather", "music", "notifications", "alarm", "eventreminder", "more", "settings"};
        byte[] ids = {1, 25, 2, 3, 20, 4, 11, 6, 9, 21, 7, 19};

        if (pages != null) {
            pages.add("settings");
            // it seem that we first have to put all ENABLED items into the array
            int pos = 1;
            for (int i = 0; i < keys.length; i++) {
                String key = keys[i];
                byte id = ids[i];
                if (pages.contains(key)) {
                    command[pos + 1] = 0x00;
                    command[pos + 3] = id;
                    pos += 4;
                }
            }
            // And then all DISABLED ones
            for (int i = 0; i < keys.length; i++) {
                String key = keys[i];
                byte id = ids[i];
                if (!pages.contains(key)) {
                    command[pos + 1] = 0x01;
                    command[pos + 3] = id;
                    pos += 4;
                }
            }
            writeToChunked(builder, 2, command);
        }

        return this;
    }

    @Override
    protected void handleDeviceInfo(custom.freeyourgadget.MiBandApp.service.btle.profiles.deviceinfo.DeviceInfo info) {
        super.handleDeviceInfo(info);
        if (gbDevice.getFirmwareVersion() != null) {
            Version version = new Version(gbDevice.getFirmwareVersion());
            if (version.compareTo(new Version("0.0.9.00")) > 0) {
                mActivitySampleSize = 8;
            }
        }
    }
}

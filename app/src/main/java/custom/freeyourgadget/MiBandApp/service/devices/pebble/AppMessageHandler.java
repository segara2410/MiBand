/*  Copyright (C) 2015-2020 Andreas Shimokawa, Carsten Pfeiffer, Daniele
    Gobbetti

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
package custom.freeyourgadget.MiBandApp.service.devices.pebble;


import android.util.Pair;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

import custom.freeyourgadget.MiBandApp.deviceevents.GBDeviceEvent;
import custom.freeyourgadget.MiBandApp.deviceevents.GBDeviceEventSendBytes;
import custom.freeyourgadget.MiBandApp.impl.GBDevice;
import custom.freeyourgadget.MiBandApp.model.WeatherSpec;
import custom.freeyourgadget.MiBandApp.util.FileUtils;
import custom.freeyourgadget.MiBandApp.util.PebbleUtils;

class AppMessageHandler {
    final PebbleProtocol mPebbleProtocol;
    final UUID mUUID;
    Map<String, Integer> messageKeys;

    AppMessageHandler(UUID uuid, PebbleProtocol pebbleProtocol) {
        mUUID = uuid;
        mPebbleProtocol = pebbleProtocol;
    }

    public boolean isEnabled() {
        return true;
    }

    public UUID getUUID() {
        return mUUID;
    }

    public GBDeviceEvent[] handleMessage(ArrayList<Pair<Integer, Object>> pairs) {
        // Just ACK
        GBDeviceEventSendBytes sendBytesAck = new GBDeviceEventSendBytes();
        sendBytesAck.encodedBytes = mPebbleProtocol.encodeApplicationMessageAck(mUUID, mPebbleProtocol.last_id);
        return new GBDeviceEvent[]{sendBytesAck};
    }

    public GBDeviceEvent[] onAppStart() {
        return null;
    }

    public byte[] encodeUpdateWeather(WeatherSpec weatherSpec) {
        return null;
    }

    protected GBDevice getDevice() {
        return mPebbleProtocol.getDevice();
    }

    JSONObject getAppKeys() throws IOException, JSONException {
        File destDir = PebbleUtils.getPbwCacheDir();
        File configurationFile = new File(destDir, mUUID.toString() + ".json");
        if (configurationFile.exists()) {
            String jsonstring = FileUtils.getStringFromFile(configurationFile);
            JSONObject json = new JSONObject(jsonstring);
            return json.getJSONObject("appKeys");
        }
        throw new IOException();
    }
}
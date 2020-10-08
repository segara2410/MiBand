/*  Copyright (C) 2015-2020 Carsten Pfeiffer, Daniele Gobbetti

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
package custom.freeyourgadget.MiBandApp.service.devices.miband;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import custom.freeyourgadget.MiBandApp.devices.miband.VibrationProfile;
import custom.freeyourgadget.MiBandApp.service.btle.BtLEAction;
import custom.freeyourgadget.MiBandApp.service.btle.TransactionBuilder;
import custom.freeyourgadget.MiBandApp.service.devices.common.SimpleNotification;

/**
 * Does not do anything.
 */
public class NoNotificationStrategy implements NotificationStrategy {
    private static final Logger LOG = LoggerFactory.getLogger(NoNotificationStrategy.class);

    @Override
    public void sendDefaultNotification(TransactionBuilder builder, SimpleNotification simpleNotification, BtLEAction extraAction) {
        LOG.info("dummy notification stragegy: default notification");
    }

    @Override
    public void sendCustomNotification(VibrationProfile vibrationProfile, SimpleNotification simpleNotification, int flashTimes, int flashColour, int originalColour, long flashDuration, BtLEAction extraAction, TransactionBuilder builder) {
        LOG.info("dummy notification stragegy: custom notification: " + simpleNotification);
    }

    @Override
    public void stopCurrentNotification(TransactionBuilder builder) {
        LOG.info("dummy notification stragegy: stop notification");
    }
}

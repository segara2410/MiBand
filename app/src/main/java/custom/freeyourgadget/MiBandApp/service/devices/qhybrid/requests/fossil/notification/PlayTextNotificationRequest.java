package custom.freeyourgadget.MiBandApp.service.devices.qhybrid.requests.fossil.notification;

import custom.freeyourgadget.MiBandApp.service.devices.qhybrid.adapter.fossil.FossilWatchAdapter;

public class PlayTextNotificationRequest extends PlayNotificationRequest {
    public PlayTextNotificationRequest(String packageName, String sender, String message, FossilWatchAdapter adapter) {
        super(3, 2, packageName, sender, message, adapter);
    }

    public PlayTextNotificationRequest(String packageName, FossilWatchAdapter adapter) {
        super(3, 2, packageName, adapter);
    }
}

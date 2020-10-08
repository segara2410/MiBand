package custom.freeyourgadget.MiBandApp.service.devices.qhybrid.requests.fossil_hr.file;

import android.content.Context;

import custom.freeyourgadget.MiBandApp.GBApplication;
import custom.freeyourgadget.MiBandApp.R;
import custom.freeyourgadget.MiBandApp.service.btle.TransactionBuilder;
import custom.freeyourgadget.MiBandApp.service.btle.actions.SetProgressAction;
import custom.freeyourgadget.MiBandApp.service.devices.qhybrid.adapter.fossil_hr.FossilHRWatchAdapter;
import custom.freeyourgadget.MiBandApp.util.GB;

public class FirmwareFilePutRequest extends FilePutRawRequest {
    public FirmwareFilePutRequest(byte[] firmwareBytes, FossilHRWatchAdapter adapter) {
        super((short) 0x00FF, firmwareBytes, adapter);
    }

    @Override
    public void onPacketWritten(TransactionBuilder transactionBuilder, int packetNr, int packetCount) {
        int progressPercent = (int) ((((float) packetNr) / packetCount) * 100);
        transactionBuilder.add(new SetProgressAction(GBApplication.getContext().getString(R.string.updatefirmwareoperation_update_in_progress), true, progressPercent, GBApplication.getContext()));
    }

    @Override
    public void onFilePut(boolean success) {
        Context context = GBApplication.getContext();
        if (success) {
            GB.updateInstallNotification(context.getString(R.string.updatefirmwareoperation_update_complete), false, 100, context);
        } else {
            GB.updateInstallNotification(context.getString(R.string.updatefirmwareoperation_write_failed), false, 0, context);
        }
    }
}

package custom.freeyourgadget.MiBandApp.service.devices.qhybrid.requests.fossil_hr.json;

import org.json.JSONObject;

import custom.freeyourgadget.MiBandApp.service.devices.qhybrid.adapter.fossil_hr.FossilHRWatchAdapter;
import custom.freeyourgadget.MiBandApp.service.devices.qhybrid.requests.fossil_hr.file.FilePutRawRequest;

public class JsonPutRequest extends FilePutRawRequest {
    public JsonPutRequest(JSONObject object, FossilHRWatchAdapter adapter) {
        super((short)(0x0500 | (adapter.getJsonIndex() & 0xFF)), object.toString().getBytes(), adapter);
    }
}

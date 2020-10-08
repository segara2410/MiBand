package custom.freeyourgadget.MiBandApp.devices.tlw64;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.Property;
import custom.freeyourgadget.MiBandApp.devices.AbstractSampleProvider;
import custom.freeyourgadget.MiBandApp.entities.DaoSession;
import custom.freeyourgadget.MiBandApp.entities.TLW64ActivitySample;
import custom.freeyourgadget.MiBandApp.entities.TLW64ActivitySampleDao;
import custom.freeyourgadget.MiBandApp.impl.GBDevice;

public class TLW64SampleProvider extends AbstractSampleProvider<TLW64ActivitySample> {

    private GBDevice mDevice;
    private DaoSession mSession;

    public TLW64SampleProvider(GBDevice device, DaoSession session) {
        super(device, session);

        mSession = session;
        mDevice = device;
    }

    @Override
    public AbstractDao<TLW64ActivitySample, ?> getSampleDao() {
        return getSession().getTLW64ActivitySampleDao();
    }

    @Nullable
    @Override
    protected Property getRawKindSampleProperty() {
        return TLW64ActivitySampleDao.Properties.RawKind;
    }

    @NonNull
    @Override
    protected Property getTimestampSampleProperty() {
        return TLW64ActivitySampleDao.Properties.Timestamp;
    }

    @NonNull
    @Override
    protected Property getDeviceIdentifierSampleProperty() {
        return TLW64ActivitySampleDao.Properties.DeviceId;
    }

    @Override
    public int normalizeType(int rawType) {
        return rawType;
    }

    @Override
    public int toRawActivityKind(int activityKind) {
        return activityKind;
    }

    @Override
    public float normalizeIntensity(int rawIntensity) {
        return rawIntensity / (float) 4000.0;
    }

    @Override
    public TLW64ActivitySample createActivitySample() {
        return new TLW64ActivitySample();
    }
}

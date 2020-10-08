/*  Copyright (C) 2018-2020 Cre3per, Daniele Gobbetti, Sebastian Kranz

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
package custom.freeyourgadget.MiBandApp.devices.makibeshr3;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.Property;
import custom.freeyourgadget.MiBandApp.devices.AbstractSampleProvider;
import custom.freeyourgadget.MiBandApp.entities.DaoSession;
import custom.freeyourgadget.MiBandApp.entities.MakibesHR3ActivitySample;
import custom.freeyourgadget.MiBandApp.entities.MakibesHR3ActivitySampleDao;
import custom.freeyourgadget.MiBandApp.impl.GBDevice;

public class MakibesHR3SampleProvider extends AbstractSampleProvider<MakibesHR3ActivitySample> {

    private GBDevice mDevice;
    private DaoSession mSession;

    public MakibesHR3SampleProvider(GBDevice device, DaoSession session) {
        super(device, session);

        mSession = session;
        mDevice = device;
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
        return rawIntensity;
    }

    @Override
    public MakibesHR3ActivitySample createActivitySample() {
        return new MakibesHR3ActivitySample();
    }

    @Override
    public AbstractDao<MakibesHR3ActivitySample, ?> getSampleDao() {
        return getSession().getMakibesHR3ActivitySampleDao();
    }

    @Nullable
    @Override
    protected Property getRawKindSampleProperty() {
        return MakibesHR3ActivitySampleDao.Properties.RawKind;
    }

    @NonNull
    @Override
    protected Property getTimestampSampleProperty() {
        return MakibesHR3ActivitySampleDao.Properties.Timestamp;
    }

    @NonNull
    @Override
    protected Property getDeviceIdentifierSampleProperty() {
        return MakibesHR3ActivitySampleDao.Properties.DeviceId;
    }
}

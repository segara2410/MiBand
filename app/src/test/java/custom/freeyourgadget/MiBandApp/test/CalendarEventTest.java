package custom.freeyourgadget.MiBandApp.test;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import custom.freeyourgadget.MiBandApp.entities.CalendarSyncStateDao;
import custom.freeyourgadget.MiBandApp.externalevents.CalendarReceiver;
import custom.freeyourgadget.MiBandApp.impl.GBDevice;
import custom.freeyourgadget.MiBandApp.model.CalendarEvents;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class CalendarEventTest extends TestBase {
    private static final long BEGIN = 1;
    private static final long END = 2;
    private static final long ID_1 = 100;
    private static final long ID_2 = 101;
    private static final String CALNAME_1 = "cal1";

    @Test
    public void testHashCode()  {
        CalendarEvents.CalendarEvent c1 = new CalendarEvents.CalendarEvent(BEGIN, END, ID_1, "something", null, null, CALNAME_1, false);
        CalendarEvents.CalendarEvent c2 = new CalendarEvents.CalendarEvent(BEGIN, END, ID_1, null, "something", null, CALNAME_1, false);
        CalendarEvents.CalendarEvent c3 = new CalendarEvents.CalendarEvent(BEGIN, END, ID_1, null, null, "something", CALNAME_1, false);

        assertEquals(c1.hashCode(), c1.hashCode());
        assertNotEquals(c1.hashCode(), c2.hashCode());
        assertNotEquals(c2.hashCode(), c3.hashCode());
    }


    @Test
    public void testSync() {
        List<CalendarEvents.CalendarEvent> eventList = new ArrayList<>();
        eventList.add(new CalendarEvents.CalendarEvent(BEGIN, END, ID_1, null, "something", null, CALNAME_1, false));

        GBDevice dummyGBDevice = createDummyGDevice("00:00:01:00:03");
        dummyGBDevice.setState(GBDevice.State.INITIALIZED);
//        Device device = DBHelper.getDevice(dummyGBDevice, daoSession);
        CalendarReceiver testCR = new CalendarReceiver(dummyGBDevice);

        testCR.syncCalendar(eventList);

        eventList.add(new CalendarEvents.CalendarEvent(BEGIN, END, ID_2, null, "something", null, CALNAME_1, false));
        testCR.syncCalendar(eventList);

        CalendarSyncStateDao calendarSyncStateDao = daoSession.getCalendarSyncStateDao();
        assertEquals(2, calendarSyncStateDao.count());
    }

}

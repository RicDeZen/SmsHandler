package com.dezen.riccardo.smshandler;

import android.content.Context;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.dezen.riccardo.smshandler.database.SmsDatabase;
import com.dezen.riccardo.smshandler.database.SmsEntity;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.TestCase.assertEquals;

@RunWith(AndroidJUnit4.class)
public class DatabaseTest {

    private SmsDatabase db;

    @Before
    public void createDb() {
        Context context = ApplicationProvider.getApplicationContext();
        db = Room.databaseBuilder(context, SmsDatabase.class, "sms-db")
                .enableMultiInstanceInvalidation()
                .build();
    }

    @Test
    public void getCount() {
        SmsEntity entity = new SmsEntity(1, "3334455666", "text");
        db.access().insert(entity);
        assertEquals(1, db.access().getCount());
    }

    @Test
    public void loadAllSms() {
        SmsEntity entity = new SmsEntity(1, "3334455666", "text");
        db.access().insert(entity);
        assertEquals(entity.id, db.access().loadAllSms()[0].id);
    }

    @Test
    public void deleteSms() {
        SmsEntity entity = new SmsEntity(1, "3334455666", "text");
        db.access().insert(entity);
        db.access().deleteSms(entity);
        assertEquals(0, db.access().getCount());
    }
}
package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.junit.*
import org.junit.runner.RunWith
import java.util.*

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@SmallTest
class RemindersDaoTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: RemindersDatabase

    @Before
    fun initDb() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).build()
    }

    @After
    fun closeDb() = database.close()

    @Test
    fun getReminders() = runBlockingTest {
        // GIVEN
        val reminder = ReminderDTO(
            "title",
            "description",
            "location",
            (-360..360).random().toDouble(),
            (-360..360).random().toDouble()
        )
        database.reminderDao().saveReminder(reminder)

        // WHEN
        val reminders = database.reminderDao().getReminders()

        // THEN
        Assert.assertThat(reminders.size, `is`(1))
        Assert.assertThat(reminders[0].id, `is`(reminder.id))
        Assert.assertThat(reminders[0].title, `is`(reminder.title))
        Assert.assertThat(reminders[0].description, `is`(reminder.description))
        Assert.assertThat(reminders[0].location, `is`(reminder.location))
        Assert.assertThat(reminders[0].latitude, `is`(reminder.latitude))
        Assert.assertThat(reminders[0].longitude, `is`(reminder.longitude))
    }


    @Test
    fun insertReminder_GetById() = runBlockingTest {
        // GIVEN
        val reminder = ReminderDTO(
            "title",
            "description",
            "location",
            (-360..360).random().toDouble(),
            (-360..360).random().toDouble()
        )
        database.reminderDao().saveReminder(reminder)

        // WHEN
        val loaded = database.reminderDao().getReminderById(reminder.id)

        // THEN
        Assert.assertThat<ReminderDTO>(loaded as ReminderDTO, notNullValue())
        Assert.assertThat(loaded.id, `is`(reminder.id))
        Assert.assertThat(loaded.title, `is`(reminder.title))
        Assert.assertThat(loaded.description, `is`(reminder.description))
        Assert.assertThat(loaded.location, `is`(reminder.location))
        Assert.assertThat(loaded.latitude, `is`(reminder.latitude))
        Assert.assertThat(loaded.longitude, `is`(reminder.longitude))
    }

    @Test
    fun getReminderByIdNotFound() = runBlockingTest {
        // GIVEN
        val reminderId = UUID.randomUUID().toString()

        // WHEN
        val loaded = database.reminderDao().getReminderById(reminderId)

        // THEN
        Assert.assertNull(loaded)
    }


    @Test
    fun deleteReminders() = runBlockingTest {
        // GIVEN
        val remindersList = listOf<ReminderDTO>(
            ReminderDTO(
                "title",
                "description",
                "location",
                (-360..360).random().toDouble(),
                (-360..360).random().toDouble()
            ),
            ReminderDTO(
                "title",
                "description",
                "location",
                (-360..360).random().toDouble(),
                (-360..360).random().toDouble()
            ),
            ReminderDTO(
                "title",
                "description",
                "location",
                (-360..360).random().toDouble(),
                (-360..360).random().toDouble()
            ),
            ReminderDTO(
                "title",
                "description",
                "location",
                (-360..360).random().toDouble(),
                (-360..360).random().toDouble()
            )
        )
        remindersList.forEach {
            database.reminderDao().saveReminder(it)
        }

        // WHEN
        database.reminderDao().deleteAllReminders()

        // THEN
        val reminders = database.reminderDao().getReminders()
        Assert.assertThat(reminders.isEmpty(), `is`(true))
    }
}
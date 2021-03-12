package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.google.common.truth.Truth.assertThat
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.util.FakeReminderDao
import com.udacity.project4.util.MainCoroutineRule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@MediumTest
class RemindersLocalRepositoryTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    val list = listOf<ReminderDTO>(
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

    private val reminder1 = list[0]
    private val reminder2 = list[1]
    private val reminder3 = list[2]

    private val newReminder = list[3]

    private lateinit var fakeRemindersDao: FakeReminderDao
    private lateinit var remindersLocalRepository: RemindersLocalRepository

    @Before
    fun setup() {
        fakeRemindersDao = FakeReminderDao()
        remindersLocalRepository = RemindersLocalRepository(
            fakeRemindersDao, Dispatchers.Unconfined
        )
    }

    @Test
    fun savesToLocalCache() = runBlockingTest {
        // GIVEN
        var list = mutableListOf<ReminderDTO>()
        list.addAll(fakeRemindersDao.remindersServiceData.values)
        assertThat(list).doesNotContain(newReminder)
        assertThat((remindersLocalRepository.getReminders() as? Result.Success)?.data)
            .doesNotContain(
                newReminder
            )

        // WHEN
        remindersLocalRepository.saveReminder(newReminder)
        list = mutableListOf()
        list.addAll(fakeRemindersDao.remindersServiceData.values)

        // THEN
        assertThat(list).contains(newReminder)
        val result = remindersLocalRepository.getReminders() as? Result.Success
        assertThat(result?.data).contains(newReminder)
    }

    @Test
    fun getReminderByIdThatExistsInLocalCache() = runBlockingTest {
        // GIVEN
        assertThat((remindersLocalRepository.getReminder(reminder1.id) as? Result.Error)?.message)
            .isEqualTo(
                "Reminder not found!"
            )

        fakeRemindersDao.remindersServiceData[reminder1.id] = reminder1

        // THEN
        val loadedReminder =
            (remindersLocalRepository.getReminder(reminder1.id) as? Result.Success)?.data
        Assert.assertThat<ReminderDTO>(loadedReminder as ReminderDTO, CoreMatchers.notNullValue())
        Assert.assertThat(loadedReminder.id, `is`(reminder1.id))
        Assert.assertThat(loadedReminder.title, `is`(reminder1.title))
        Assert.assertThat(loadedReminder.description, `is`(reminder1.description))
        Assert.assertThat(loadedReminder.location, `is`(reminder1.location))
        Assert.assertThat(loadedReminder.latitude, `is`(reminder1.latitude))
        Assert.assertThat(loadedReminder.longitude, `is`(reminder1.longitude))
    }

    @Test
    fun deleteAllReminders_EmptyListFetchedFromLocalCache() = runBlockingTest {
        // GIVEN
        assertThat((remindersLocalRepository.getReminders() as? Result.Success)?.data)
            .isEmpty()
        fakeRemindersDao.remindersServiceData[reminder1.id] = reminder1
        fakeRemindersDao.remindersServiceData[reminder2.id] = reminder2
        fakeRemindersDao.remindersServiceData[reminder3.id] = reminder3

        // WHEN
        assertThat((remindersLocalRepository.getReminders() as? Result.Success)?.data)
            .isNotEmpty()
        remindersLocalRepository.deleteAllReminders()

        // THEN
        assertThat((remindersLocalRepository.getReminders() as? Result.Success)?.data)
            .isEmpty()
    }
}
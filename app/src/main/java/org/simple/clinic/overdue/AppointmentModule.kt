package org.simple.clinic.overdue

import com.f2prateek.rx.preferences2.Preference
import com.f2prateek.rx.preferences2.RxSharedPreferences
import dagger.Module
import dagger.Provides
import io.reactivex.Single
import org.simple.clinic.AppDatabase
import org.simple.clinic.home.overdue.OverdueAppointment
import org.simple.clinic.util.None
import org.simple.clinic.util.Optional
import org.simple.clinic.util.OptionalRxPreferencesConverter
import org.simple.clinic.util.StringPreferenceConverter
import org.threeten.bp.Period
import retrofit2.Retrofit
import javax.inject.Named

@Module
class AppointmentModule {

  @Provides
  fun config(): Single<AppointmentConfig> {
    return Single.just(AppointmentConfig(
        minimumOverduePeriodForHighRisk = Period.ofDays(30),
        overduePeriodForLowestRiskLevel = Period.ofDays(365),
        isApiV3Enabled = true,
        appointmentDuePeriodForDefaulters = Period.ofDays(30)
    ))
  }

  @Provides
  fun dao(appDatabase: AppDatabase): Appointment.RoomDao {
    return appDatabase.appointmentDao()
  }

  @Provides
  fun overdueAppointmentDao(appDatabase: AppDatabase): OverdueAppointment.RoomDao {
    return appDatabase.overdueAppointmentDao()
  }

  @Provides
  fun syncApiV2(retrofit: Retrofit): AppointmentSyncApiV2 {
    return retrofit.create(AppointmentSyncApiV2::class.java)
  }

  @Provides
  fun syncApiV3(retrofit: Retrofit): AppointmentSyncApiV3 {
    return retrofit.create(AppointmentSyncApiV3::class.java)
  }

  @Provides
  @Named("last_appointment_pull_token")
  fun lastPullTokenV2(rxSharedPrefs: RxSharedPreferences): Preference<Optional<String>> {
    return rxSharedPrefs.getObject("last_appointment_pull_token_v2", None, OptionalRxPreferencesConverter(StringPreferenceConverter()))
  }

  @Provides
  @Named("last_appointment_pull_token_v3")
  fun lastPullTokenV3(rxSharedPrefs: RxSharedPreferences): Preference<Optional<String>> {
    return rxSharedPrefs.getObject("last_appointment_pull_token_v3", None, OptionalRxPreferencesConverter(StringPreferenceConverter()))
  }
}

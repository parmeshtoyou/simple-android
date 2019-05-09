package org.simple.clinic.home.overdue.phonemask

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.ofType
import org.simple.clinic.ReplayUntilScreenIsDestroyed
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.phone.Caller
import org.simple.clinic.phone.MaskedPhoneCaller
import org.simple.clinic.widgets.UiEvent
import javax.inject.Inject

private typealias Ui = PhoneMaskBottomSheet
private typealias UiChange = (Ui) -> Unit

class PhoneMaskBottomSheetController @Inject constructor(
    private val maskedPhoneCaller: MaskedPhoneCaller
) : ObservableTransformer<UiEvent, UiChange> {

  override fun apply(events: Observable<UiEvent>): ObservableSource<UiChange> {
    val replayedEvents = ReplayUntilScreenIsDestroyed(events)
        .compose(ReportAnalyticsEvents())
        .replay()

    return Observable.mergeArray(
        makeNormalCall(replayedEvents),
        makeSecureCall(replayedEvents)
    )
  }

  private fun makeNormalCall(events: Observable<UiEvent>): Observable<UiChange> {
    val patientStream = events.ofType<>()

    return events
        .ofType<NormalCallClicked>()
        .flatMap {
          val phoneNumber = "1234567890"
          maskedPhoneCaller
              .maskAndCall(phoneNumber, caller = Caller.WithoutDialer)
              .andThen(Observable.empty<UiChange>())
        }
  }

  private fun makeSecureCall(events: Observable<UiEvent>): Observable<UiChange> =
      events
          .ofType<SecureCallClicked>()
          .map { { ui: Ui -> ui.makeSecureCall() } }
}

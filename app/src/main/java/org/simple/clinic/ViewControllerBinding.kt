package org.simple.clinic

import android.annotation.SuppressLint
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import io.reactivex.schedulers.Schedulers.io
import org.simple.clinic.widgets.ScreenDestroyed
import org.simple.clinic.widgets.UiEvent
import org.threeten.bp.Duration
import java.util.concurrent.TimeUnit

@SuppressLint("CheckResult")
fun <T> bindUiToController(
    ui: T,
    events: Observable<UiEvent>,
    controller: ObservableTransformer<UiEvent, (T) -> Unit>,
    screenDestroys: Observable<ScreenDestroyed>,
    uiChangeDelay: Duration = SCREEN_CHANGE_ANIMATION_DURATION
) {
  events
      .mergeWith(screenDestroys)
      .observeOn(io())
      .compose(controller)
      .compose(delayEmissionsBy(uiChangeDelay))
      .observeOn(mainThread())
      .takeUntil(screenDestroys)
      .subscribe { uiChange -> uiChange(ui) }
}

private fun <T> delayEmissionsBy(duration: Duration): ObservableTransformer<(T) -> Unit, (T) -> Unit> {
  return ObservableTransformer { upstream ->
    when (duration) {
      Duration.ZERO -> upstream
      else -> upstream.delaySubscription(duration.toMillis(), TimeUnit.MILLISECONDS)
    }
  }
}

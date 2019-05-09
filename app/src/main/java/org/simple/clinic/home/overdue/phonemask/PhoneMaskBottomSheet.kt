package org.simple.clinic.home.overdue.phonemask

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.jakewharton.rxbinding2.view.RxView
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import kotterknife.bindView
import org.simple.clinic.R
import org.simple.clinic.activity.TheActivity
import org.simple.clinic.bindUiToController
import org.simple.clinic.bp.entry.LinearLayoutWithPreImeKeyEventListener
import org.simple.clinic.home.overdue.OverdueListItem.Patient
import org.simple.clinic.widgets.BottomSheetActivity
import org.simple.clinic.widgets.ScreenDestroyed
import org.simple.clinic.widgets.UiEvent
import javax.inject.Inject

class PhoneMaskBottomSheet : BottomSheetActivity() {
  private val rootLayout by bindView<LinearLayoutWithPreImeKeyEventListener>(R.id.phonemask_root)
  private val normalCallButton by bindView<View>(R.id.phonemask_normal_call_button)
  private val secureCallButton by bindView<View>(R.id.phonemask_secure_call_button)

  @Inject
  lateinit var controller: PhoneMaskBottomSheetController

  private val onDestroys = PublishSubject.create<ScreenDestroyed>()

  @SuppressLint("CheckResult")
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.sheet_phone_mask)
    TheActivity.component.inject(this)

    bindUiToController(
        ui = this,
        events = Observable.mergeArray(
            sheetCreates(),
            normalCallClicks(),
            secureCallClicks()
        ),
        controller = controller,
        screenDestroys = onDestroys
    )

    // Dismiss this sheet when the keyboard is dismissed.
    rootLayout.backKeyPressInterceptor = { super.onBackgroundClick() }
  }

  private fun normalCallClicks() =
      RxView
          .clicks(normalCallButton)
          .map { NormalCallClicked }

  private fun secureCallClicks() =
      RxView
          .clicks(secureCallButton)
          .map { SecureCallClicked }

  private fun sheetCreates(): Observable<UiEvent> {
    val patient = intent.getParcelableExtra<Patient>(PATIENT_KEY)
    return Observable.just(PhoneMaskBottomSheetCreated(patient))
  }

  override fun onDestroy() {
    onDestroys.onNext(ScreenDestroyed())
    super.onDestroy()
  }

  fun makeSecureCall() {

  }

  companion object {

    private const val PATIENT_KEY = "PATIENT_KEY"

    fun intentForPhoneMaskBottomSheet(context: Context, patient: Patient): Intent =
        Intent(context, PhoneMaskBottomSheet::class.java).apply {
          putExtra(PATIENT_KEY, patient)
        }
  }
}

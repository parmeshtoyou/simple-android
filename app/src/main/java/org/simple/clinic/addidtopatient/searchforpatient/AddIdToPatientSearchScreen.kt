package org.simple.clinic.addidtopatient.searchforpatient

import android.content.Context
import android.util.AttributeSet
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import com.google.android.material.textfield.TextInputLayout
import com.jakewharton.rxbinding2.view.RxView
import com.jakewharton.rxbinding2.widget.RxTextView
import io.reactivex.Observable
import kotterknife.bindView
import org.simple.clinic.R
import org.simple.clinic.activity.TheActivity
import org.simple.clinic.addidtopatient.searchresults.AddIdToPatientSearchResultsScreenKey
import org.simple.clinic.bindUiToController
import org.simple.clinic.router.screen.ScreenRouter
import org.simple.clinic.text.style.TextAppearanceWithLetterSpacingSpan
import org.simple.clinic.util.Truss
import org.simple.clinic.util.identifierdisplay.IdentifierDisplayAdapter
import org.simple.clinic.util.unsafeLazy
import org.simple.clinic.widgets.PrimarySolidButtonWithFrame
import org.simple.clinic.widgets.ScreenDestroyed
import org.simple.clinic.widgets.showKeyboard
import javax.inject.Inject

class AddIdToPatientSearchScreen(context: Context, attrs: AttributeSet) : RelativeLayout(context, attrs) {

  @Inject
  lateinit var screenRouter: ScreenRouter

  @Inject
  lateinit var activity: TheActivity

  @Inject
  lateinit var controller: AddIdToPatientSearchScreenController

  @Inject
  lateinit var identifierDisplayAdapter: IdentifierDisplayAdapter

  private val toolBar by bindView<Toolbar>(R.id.addidtopatientsearch_toolbar)
  private val titleTextView by bindView<TextView>(R.id.addidtopatientsearch_title)
  private val fullNameEditText by bindView<EditText>(R.id.addidtopatientsearch_fullname)
  private val fullNameInputLayout by bindView<TextInputLayout>(R.id.addidtopatientsearch_fullname_inputlayout)
  private val searchButtonFrame by bindView<PrimarySolidButtonWithFrame>(R.id.addidtopatientsearch_search_frame)
  private val screenKey by unsafeLazy {
    screenRouter.key<AddIdToPatientSearchScreenKey>(this)
  }

  override fun onFinishInflate() {
    super.onFinishInflate()
    if (isInEditMode) {
      return
    }
    TheActivity.component.inject(this)

    fullNameEditText.showKeyboard()
    toolBar.setNavigationOnClickListener {
      screenRouter.pop()
    }
    displayScreenTitle()

    bindUiToController(
        ui = this,
        events = Observable.merge(nameChanges(), searchClicks()),
        controller = controller,
        screenDestroys = RxView.detaches(this).map { ScreenDestroyed() }
    )
  }

  private fun displayScreenTitle() {
    val identifierType = identifierDisplayAdapter.typeAsText(screenKey.identifier)
    val identifierValue = identifierDisplayAdapter.valueAsText(screenKey.identifier)

    val identifierTextAppearanceSpan = TextAppearanceWithLetterSpacingSpan(
        context,
        R.style.Clinic_V2_TextAppearance_Body0Left_NumericBold_White100
    )

    titleTextView.text = Truss()
        .append(resources.getString(R.string.addidtopatientsearch_add, identifierType))
        .pushSpan(identifierTextAppearanceSpan)
        .append(identifierValue)
        .popSpan()
        .append(resources.getString(R.string.addidtopatientsearch_to_patient))
        .build()
  }

  private fun nameChanges() =
      RxTextView
          .textChanges(fullNameEditText)
          .map(CharSequence::toString)
          .map(::SearchQueryNameChanged)

  private fun searchClicks(): Observable<SearchClicked> {
    val imeSearchClicks = RxTextView
        .editorActionEvents(fullNameEditText)
        .filter { it.actionId() == EditorInfo.IME_ACTION_SEARCH }

    return RxView
        .clicks(searchButtonFrame.button)
        .mergeWith(imeSearchClicks)
        .map { SearchClicked }
  }

  fun showSearchButtonAsEnabled() {
    searchButtonFrame.isEnabled = true
  }

  fun showSearchButtonAsDisabled() {
    searchButtonFrame.isEnabled = false
  }

  fun openAddIdToPatientSearchResultsScreen(name: String) {
    screenRouter.push(AddIdToPatientSearchResultsScreenKey(
        fullName = name,
        identifier = screenKey.identifier
    ))
  }

  fun setEmptyFullNameErrorVisible(visible: Boolean) {
    fullNameInputLayout.error = if (visible) {
      resources.getString(R.string.addidtopatientsearch_error_empty_fullname)
    } else {
      null
    }
    fullNameInputLayout.isErrorEnabled = visible
  }
}

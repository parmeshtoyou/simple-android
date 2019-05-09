package org.simple.clinic.home.overdue.phonemask

import org.simple.clinic.home.overdue.OverdueListItem.Patient
import org.simple.clinic.widgets.UiEvent

data class PhoneMaskBottomSheetCreated(val patient: Patient) : UiEvent

object NormalCallClicked : UiEvent

object SecureCallClicked : UiEvent

package org.simple.clinic.router.screen

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.ColorRes
import androidx.annotation.IdRes
import androidx.core.content.ContextCompat
import flow.Direction
import flow.KeyChanger
import flow.State
import flow.TraversalCallback

/**
 * Coordinates changes between [FullScreenKey]s.
 *
 * @param [screenLayoutContainerRes] ViewGroup where layouts for [FullScreenKey] will be inflated.
 */
class FullScreenKeyChanger(
    private val activity: Activity,
    @IdRes private val screenLayoutContainerRes: Int,
    @ColorRes private val screenBackgroundRes: Int,
    private val onKeyChange: (FullScreenKey?, FullScreenKey) -> Unit = { _, _ -> },
    keyChangeAnimator: KeyChangeAnimator<FullScreenKey>
) : BaseViewGroupKeyChanger<FullScreenKey>(keyChangeAnimator), KeyChanger {

  private val containerIds = mutableMapOf<FullScreenKey, Int>()

  override fun layoutResForKey(screenKey: FullScreenKey): Int {
    return screenKey.layoutRes()
  }

  override fun canHandleKey(incomingKey: Any): Boolean {
    return incomingKey is FullScreenKey
  }

  override fun screenLayoutContainer(): ViewGroup {
    return activity.findViewById(screenLayoutContainerRes)
  }

  override fun inflateIncomingView(incomingContext: Context, incomingKey: FullScreenKey, frame: ViewGroup): View {
    // If the backstack is changed while a screen change animation was ongoing, the screens
    // end up overlapping with each other. It's difficult to debug if it's a problem with Flow
    // or in our code. As a workaround, the window background is applied on every screen.
    val container = FrameLayout(incomingContext)

    // The ID for each screen's container should remain the same for View state restoration to work.
    if (containerIds.containsKey(incomingKey).not()) {
      containerIds[incomingKey] = View.generateViewId()
    }
    container.id = containerIds[incomingKey]!!

    val contentView = super.inflateIncomingView(incomingContext, incomingKey, container)
    container.addView(contentView)

    if (contentView.background == null) {
      container.setBackgroundColor(ContextCompat.getColor(incomingContext, screenBackgroundRes))
    } else {
      container.background = contentView.background
      contentView.background = null
    }

    return container
  }

  override fun changeKey(
      outgoingState: State?,
      incomingState: State,
      direction: Direction,
      incomingContexts: Map<Any, Context>,
      callback: TraversalCallback
  ) {
    super.changeKey(outgoingState, incomingState, direction, incomingContexts, callback)
    val outgoingKey = outgoingState?.getKey<FullScreenKey>()
    val incomingKey = incomingState.getKey<FullScreenKey>()
    onKeyChange(outgoingKey, incomingKey)
  }
}

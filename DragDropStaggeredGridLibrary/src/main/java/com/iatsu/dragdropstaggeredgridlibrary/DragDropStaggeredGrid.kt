package com.iatsu.dragdropstaggeredgridlibrary

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.DragEvent
import android.view.LayoutInflater
import android.view.View
import android.view.animation.OvershootInterpolator
import android.widget.*
import com.iatsu.dragdropstaggeredgridlibrary.Helper.Companion.addToMainLayout
import com.iatsu.dragdropstaggeredgridlibrary.Helper.Companion.belongsRightLayout
import java.util.concurrent.atomic.AtomicBoolean

class DragDropStaggeredGrid(context: Context, attrs: AttributeSet? = null) : RelativeLayout(context, attrs) {
    internal var leftLayout: LinearLayout
    internal var rightLayout: LinearLayout
    internal var pageLayout: RelativeLayout

    internal var mainScroller: ScrollView
    internal var mAnimator: ValueAnimator? = null
    private val mIsScrolling = AtomicBoolean(false)
    var callback: Draggable? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.staggered_layout, this, true)

        //region get layouts and set drag listeners
        pageLayout = findViewById(R.id.pagelayout)
        leftLayout = findViewById(R.id.leftLayout)
        rightLayout = findViewById(R.id.rightLayout)
        mainScroller = findViewById(R.id.mainscroller)

        mainScroller.isSmoothScrollingEnabled = true
        leftLayout.setOnDragListener(DragListener())
        rightLayout.setOnDragListener(DragListener())
        //endregion
    }

    fun setViews(views: MutableList<View>, margin: Int) {
        for (i in 0 until views.size) {
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(margin, margin, margin, margin)

            val currentView: View = views[i]
            currentView.layoutParams = params
            addToMainLayout(i, currentView, leftLayout, rightLayout)
            currentView.setOnLongClickListener(
                Helper.Companion.LongPressListener(
                    currentView
                )
            )
        }
    }

    fun setViewByTag(newView: View, oldViewTag: String) {
        val view = getViewByTag(oldViewTag)
        val index: Int
        if (view == null)
            return

        newView.layoutParams = view.layoutParams

        val belongsRight = belongsRightLayout(view, leftLayout, rightLayout)
        if (belongsRight) {
            index = rightLayout.indexOfChild(view)
            rightLayout.removeView(view)
            rightLayout.addView(newView, index)
        } else {
            index = leftLayout.indexOfChild(view)
            leftLayout.removeView(view)
            leftLayout.addView(newView, index)
        }
    }

    fun getViews(): MutableList<View> {
        //create sorted note array
        val totalChildCount = leftLayout.childCount + rightLayout.childCount
        var currentCount = 0
        val sortedViewArray = mutableListOf<View>()

        for (i in 0 until totalChildCount) {
            if (i % 2 == 0)
                sortedViewArray.add(leftLayout.getChildAt(currentCount))
            else {
                sortedViewArray.add(rightLayout.getChildAt(currentCount))
                currentCount++
            }
        }

        return sortedViewArray
    }

    fun hasChangedPlaces(): Boolean {
        return hasChangedPlaces
    }

    fun getViewByTag(tag: String): View? {
        try {
            //create sorted note array
            val totalChildCount = leftLayout.childCount + rightLayout.childCount
            var currentCount = 0
            var viewWithTag: View? = null

            for (i in 0 until totalChildCount) {
                if (i % 2 == 0 && leftLayout.getChildAt(currentCount).tag.equals(tag)) {
                    viewWithTag = leftLayout.getChildAt(currentCount)
                    break
                } else {
                    if (rightLayout.getChildAt(currentCount).tag.equals(tag))
                        viewWithTag = rightLayout.getChildAt(currentCount)
                    currentCount++
                }
            }

            return viewWithTag
        } catch (ex: Exception) {
            return null
        }
    }

    private var hasChangedPlaces = false

    private inner class DragListener : View.OnDragListener {

        override fun onDrag(v: View?, event: DragEvent?): Boolean {
            val view = event!!.localState as View
            var startingIndex: Int? = null
            if (view.parent != null)
                startingIndex = (view.parent as LinearLayout).indexOfChild(view)

            when (event.getAction()) {
                DragEvent.ACTION_DRAG_LOCATION -> {
                    // do nothing if hovering above own position
                    if (view === v) return true

                    callback?.dragging(view, v)

                    //<-- check layout switches
                    val leftOrRightView: Int
                    if (v!!.x != 0f) {
                        leftOrRightView = 1
                        if (view.parent == leftLayout) {
                            val beginningIndex = leftLayout.indexOfChild(view)

                            leftLayout.removeView(view)
                            val index = calculateNewIndex(view, event.x, event.y, leftOrRightView)
                            rightLayout.addView(view, index)
                            hasChangedPlaces = true

                            val nextChildToMove = rightLayout.getChildAt(index + 1)
                            if (nextChildToMove != null) {
                                rightLayout.removeView(nextChildToMove)
                                leftLayout.addView(nextChildToMove, beginningIndex)
                            }
                        }
                    } else {
                        leftOrRightView = 0
                        if (view.parent == rightLayout) {
                            val beginningIndex = rightLayout.indexOfChild(view)

                            rightLayout.removeView(view)
                            val index = calculateNewIndex(view, event.x, event.y, leftOrRightView)
                            leftLayout.addView(view, index)
                            hasChangedPlaces = true

                            val nextChildToMove = leftLayout.getChildAt(index + 1)
                            if (nextChildToMove != null) {
                                leftLayout.removeView(nextChildToMove)
                                rightLayout.addView(nextChildToMove, beginningIndex)
                            }
                        }
                    }
                    //check layout switches -->

                    val layoutToBeUsed: LinearLayout
                    if (leftOrRightView == 0)
                        layoutToBeUsed = leftLayout
                    else
                        layoutToBeUsed = rightLayout

                    // get the new list index
                    val index = calculateNewIndex(view, event.x, event.y, leftOrRightView)
                    if (startingIndex != index && startingIndex != null)
                        hasChangedPlaces = true

                    //for scroll
                    val scrollY: Int = mainScroller.scrollY
                    val rect = Rect()
                    mainScroller.getHitRect(rect)

                    if (event.y - scrollY > mainScroller.bottom - 250) {
                        startScrolling(scrollY, layoutToBeUsed.height)
                    } else if (event.y - scrollY < mainScroller.top + 250) {
                        startScrolling(scrollY, 0)
                    } else {
                        stopScrolling()
                    }
                    //for scroll

                    // remove the view from the old position
                    layoutToBeUsed.removeView(view)
                    // and push to the new
                    layoutToBeUsed.addView(view, index)
                }
                DragEvent.ACTION_DRAG_ENDED -> {
                    view.visibility = View.VISIBLE
                    callback?.dragEnded(view, v)
                }
            }
            return true
        }

        fun startScrolling(from: Int, to: Int) {
            if (from != to && mAnimator == null) {
                mIsScrolling.set(true)
                mAnimator = ValueAnimator()
                mAnimator!!.setInterpolator(OvershootInterpolator())
                mAnimator!!.setDuration(Math.abs(to - from).toLong())
                mAnimator!!.setIntValues(from, to)
                mAnimator!!.addUpdateListener(object : ValueAnimator.AnimatorUpdateListener {

                    override fun onAnimationUpdate(valueAnimator: ValueAnimator?) {
                        mainScroller.smoothScrollTo(0, valueAnimator!!.animatedValue as Int)
                    }
                })
                mAnimator!!.addListener(object : AnimatorListenerAdapter() {

                    override fun onAnimationEnd(animation: Animator?) {
                        mIsScrolling.set(false)
                        mAnimator = null
                    }

                })
                mAnimator!!.start()
            }
        }

        fun stopScrolling() {
            if (mAnimator != null) {
                mAnimator!!.cancel()
            }
        }

        fun calculateNewIndex(view: View, x: Float, y: Float, leftOrRightView: Int): Int {
            val layoutToBeUsed: LinearLayout
            if (leftOrRightView == 0) layoutToBeUsed = leftLayout
            else layoutToBeUsed = rightLayout

            var row = layoutToBeUsed.indexOfChild(view)
            if(row == -1) row = layoutToBeUsed.childCount - 1

            for (i in 0 until layoutToBeUsed.childCount) {
                val card = layoutToBeUsed.getChildAt(i)
                if (y - (view.height) < card.y) {
                    row = i
                    break
                }
            }

            return row
        }
    }
}

interface Draggable {
    fun dragging(localStateView: View, view: View?)
    fun dragEnded(localStateView: View, view: View?)
}
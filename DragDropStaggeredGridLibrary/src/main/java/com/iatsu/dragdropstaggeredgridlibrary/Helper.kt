package com.iatsu.dragdropstaggeredgridlibrary

import android.content.ClipData
import android.view.View
import android.widget.LinearLayout

class Helper {
    companion object {
        fun addToMainLayout(i: Int, view: View, leftLayout: LinearLayout, rightLayout: LinearLayout) {
            if (i % 2 == 0)
                leftLayout.addView(view, i / 2)
            else
                rightLayout.addView(view, ((i + 1) / 2) - 1)
        }

        fun belongsRightLayout(view: View, leftLayout: LinearLayout, rightLayout: LinearLayout): Boolean {
            if (view.parent == leftLayout)
                return false
            else if (view.parent == rightLayout)
                return true
            else {
                leftLayout.addView(view)
                return false
            }
        }

        class LongPressListener(val vi: View) : View.OnLongClickListener {
            override fun onLongClick(v: View?): Boolean {

                val data: ClipData = ClipData.newPlainText("", "");
                val shadowBuilder: View.DragShadowBuilder = View.DragShadowBuilder(vi)
                vi.startDrag(data, shadowBuilder, vi, 0)
                vi.setVisibility(View.INVISIBLE)

                return true
            }
        }
    }
}
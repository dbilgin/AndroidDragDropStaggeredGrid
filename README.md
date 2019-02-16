# AndroidDragDropStaggeredGrid
A two column staggered grid library with drag and drop capabilities for android.

<img src="https://github.com/iatsu/AndroidDragDropStaggeredGrid/blob/master/DragDropStaggeredGrid.gif" width="450" />

Add it in your root build.gradle at the end of repositories:
    
    allprojects {
      repositories {
        ...
        maven { url 'https://jitpack.io' }
      }
    }
  
Add the dependency

    dependencies {
      implementation 'com.github.iatsu:AndroidDragDropStaggeredGrid:0.1.3'
    }

<h2>A simple guide</h2>

<b>First add it to your layout as below:</b>

    <?xml version="1.0" encoding="utf-8"?>
    <android.support.constraint.ConstraintLayout
            xmlns:android="http://schemas.android.com/apk/res/android"
            xmlns:tools="http://schemas.android.com/tools"
            xmlns:app="http://schemas.android.com/apk/res-auto"
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            tools:context=".MainActivity">

        <com.iatsu.dragdropstaggeredgridlibrary.DragDropStaggeredGrid
                android:id="@+id/dragDropStaggeredGrid"
                android:layout_width="match_parent"
                android:layout_height="match_parent"
                app:layout_constraintTop_toTopOf="parent"
                app:layout_constraintBottom_toBottomOf="parent"
                app:layout_constraintEnd_toEndOf="parent"
                app:layout_constraintStart_toStartOf="parent"/>

    </android.support.constraint.ConstraintLayout>
 
 In your activity, you can implement the <b>Draggable</b> interface, which lets you override <b>dragging()</b> and <b>dragEnded()</b>
 Below, you will see the basic usages of the methods available in DragDropStaggeredGrid
 
    lateinit var dragDropStaggeredGrid: DragDropStaggeredGrid

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        dragDropStaggeredGrid = findViewById(R.id.dragDropStaggeredGrid)
        //this callback lets you override dragging() and dragEnded()
        dragDropStaggeredGrid.callback = this

        //region add view list to layout
        val textViewList = mutableListOf<View>()
        for (i in 0 until 10) {
            val textView = TextView(this)
            textView.setBackgroundColor(Color.BLACK)
            textView.setTextColor(Color.WHITE)
            textView.text = "Lorem ipsum " + i * 6847987465468486854
            textViewList.add(textView)
        }

        dragDropStaggeredGrid.setViews(textViewList, 5)
        //endregion
        
        //get a list of all the views
        val viewList = dragDropStaggeredGrid.getViews()

        //region update item with tag
        val textView = TextView(this)
        textView.setBackgroundColor(Color.BLACK)
        textView.setTextColor(Color.WHITE)
        textView.text = "Lorem ipsum"
        dragDropStaggeredGrid.setViewByTag(textView, "oldViewsTag")
        //endregion
        
        //get the view you want from the list with the tag you give it
        val v: View = getViewByTag("tagString")
    }
    override fun dragging() {
        Log.e("e", "dragging")
    }

    override fun dragEnded() {
        Log.e("e", "drag ended")
        Log.e("View Changed Places", dragDropStaggeredGrid.hasChangedPlaces().toString())
    }

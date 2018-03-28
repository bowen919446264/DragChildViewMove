# DragChildViewMove
drag view move.

Creating a child relativeLayout view, Called DragChildMoveView.

Use:

<com.bowen.dragchildview.DragChildMoveView
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:background="@color/colorAccent"
        android:layout_margin="15dp">
        
        <ImageView
            android:layout_width="60dp"
            android:layout_height="60dp"
            android:layout_margin="20dp"
            android:clickable="true"
            android:background="@drawable/shape_circle"
            android:src="@mipmap/dragonfly"
            android:layout_alignParentRight="true"
            android:layout_alignParentBottom="true"
            android:scaleType="centerInside"
            />

        <ImageView
            android:layout_width="60dp"
            android:layout_height="60dp"
            android:layout_margin="100dp"
            android:clickable="true"
            android:background="@drawable/shape_circle"
            android:src="@mipmap/dragonfly"
            android:layout_alignParentRight="true"
            android:layout_alignParentBottom="true"
            android:scaleType="centerInside"
            />
    </com.bowen.dragchildview.DragChildMoveView>

effect as:

![image](https://github.com/bowen919446264/DragChildViewMove/blob/master/drag_move_view.gif)

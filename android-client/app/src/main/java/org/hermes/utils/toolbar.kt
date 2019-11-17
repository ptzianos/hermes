package org.hermes.utils

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.core.content.res.ResourcesCompat

import org.hermes.R

/**
 * Gets a toolbar and adds a button to it.
 */
fun addBackButton(toolbar: Toolbar, resources: Resources, callback: (View) -> Unit) {
    val drawable: Drawable = ResourcesCompat.getDrawable(resources, R.drawable.arrow_back_white, null)!!
    val bitmap: Bitmap = (drawable as BitmapDrawable).bitmap
    val newDrawable: Drawable = BitmapDrawable(resources, Bitmap.createScaledBitmap(bitmap, 50, 50, true))
    newDrawable.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP)
    toolbar.navigationIcon = newDrawable
    toolbar.setNavigationOnClickListener(callback)
}

package org.hermes.fragments


import android.content.Context
import androidx.fragment.app.Fragment
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.widget.Toolbar
import androidx.core.content.res.ResourcesCompat.getDrawable
import dagger.Module
import javax.inject.Inject

import org.hermes.R


/**
 * Fragment used for managing interactions for and presentation of a navigation drawer.
 * See the [design guidelines](https://developer.android.com/design/patterns/navigation-drawer.html#Interaction)
 * for a complete explanation of the behaviors implemented here.
 */
class NavigationDrawerFragment @Inject constructor() : Fragment() {

    @Module
    abstract class DaggerModule

    companion object {

        /**
         * Remember the position of the selected item.
         */
        private val STATE_SELECTED_POSITION = "selected_navigation_drawer_position"

        /**
         * Per the design guidelines, you should show the drawer on launch until the user manually
         * expands it. This shared preference tracks this.
         */
        private val PREF_USER_LEARNED_DRAWER = "navigation_drawer_learned"
    }

    /**
     * A pointer to the current callbacks instance (the Activity).
     */
    private var mCallbacks: NavigationDrawerCallbacks? = null

    /**
     * Helper component that ties the action bar to the navigation drawer.
     */
    private lateinit var mDrawerToggle: DrawerLayout.DrawerListener

    private var mDrawerLayout: DrawerLayout? = null
    private lateinit var mDrawerListView: ListView
    private lateinit var mFragmentContainerView: View

    private var mCurrentSelectedPosition = 0
    private var mFromSavedInstanceState: Boolean = false
    private var mUserLearnedDrawer: Boolean = false
    private lateinit var toolbar: Toolbar

    val isDrawerOpen: Boolean
        get() = mDrawerLayout != null && mDrawerLayout!!.isDrawerOpen(mFragmentContainerView)

    private var setupCalled = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sp = PreferenceManager.getDefaultSharedPreferences(activity!!.applicationContext)
        mUserLearnedDrawer = sp.getBoolean(PREF_USER_LEARNED_DRAWER, false)

        if (savedInstanceState != null) {
            mCurrentSelectedPosition = savedInstanceState.getInt(STATE_SELECTED_POSITION)
            mFromSavedInstanceState = true
        }

        // Select either the default item (0) or the last selected item.
        selectItem(mCurrentSelectedPosition)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mDrawerListView = inflater.inflate(
            R.layout.drawer_drawer, container, false
        ) as ListView

        mDrawerListView.onItemClickListener =
            AdapterView.OnItemClickListener { _, view, position, _ ->
                selectItem(position)
                view.isSelected = true
            }
        mDrawerListView.adapter = ArrayAdapter(
            context!!,
            R.layout.drawer_list_item,
            android.R.id.text1,
            arrayOf(
                getString(R.string.title_activity_dashboard),
                getString(R.string.title_activity_event_log),
                getString(R.string.title_activity_sensor_list)
            )
        )
        mDrawerListView.setItemChecked(mCurrentSelectedPosition, true)
        return mDrawerListView
    }

    /**
     * Users of this fragment must call this method to set up the navigation drawer interactions.
     *
     * @param fragmentId   The android:id of this fragment in its activity's layout.
     * @param drawerLayout The DrawerLayout containing this fragment's UI.
     */
    fun setUp(fragmentId: Int, drawerLayout: DrawerLayout) {
        mFragmentContainerView = activity!!.findViewById(fragmentId)
        mDrawerLayout = drawerLayout
        // set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout!!.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START)
        toolbar = activity!!.findViewById(R.id.toolbar)
        setDrawerIconToToolbar()
        setDrawerIconClickListeners()
        setupCalled = true
        selectItem(mCurrentSelectedPosition)
        // If the user hasn't 'learned' about the drawer, open it to introduce them to the drawer,
        // per the navigation drawer design guidelines.
        if (!mUserLearnedDrawer && !mFromSavedInstanceState) mDrawerLayout!!.openDrawer(mFragmentContainerView)
        else mDrawerLayout!!.closeDrawer(mFragmentContainerView)
    }

    /**
     * Setup the the hamburger icon in the toolbar and ensure that it has the correct dimension and color.
     */
    private fun setDrawerIconToToolbar() {
        val drawable: Drawable = getDrawable(resources, R.drawable.ic_drawer, null)!!
        val bitmap: Bitmap = (drawable as BitmapDrawable).bitmap
        val newDrawable: Drawable = BitmapDrawable(resources, Bitmap.createScaledBitmap(bitmap, 130, 50, true))
        newDrawable.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP)
        toolbar.navigationIcon = newDrawable
    }

    /**
     * Setup the listeners for the hamburger icon to open, close the drawer, set the highlighted entry every time,
     * set the fragment and the title of the screen.
     */
    private fun setDrawerIconClickListeners() {
        toolbar.setNavigationOnClickListener { mDrawerLayout!!.openDrawer(mFragmentContainerView) }

        mDrawerToggle = object : DrawerLayout.DrawerListener {
            override fun onDrawerClosed(drawerView: View) {
                if (!isAdded) {
                    return
                }
                mDrawerListView.getChildAt(mCurrentSelectedPosition).isSelected = true
                activity!!.invalidateOptionsMenu()
            }

            override fun onDrawerOpened(drawerView: View) {
                if (!isAdded) {
                    return
                }
                if (!mUserLearnedDrawer) {
                    // The user manually opened the drawer; store this flag to prevent auto-showing
                    // the navigation drawer automatically in the future.
                    mUserLearnedDrawer = true
                    val sp = PreferenceManager
                        .getDefaultSharedPreferences(activity)
                    sp.edit().putBoolean(PREF_USER_LEARNED_DRAWER, true).apply()
                }
                activity!!.invalidateOptionsMenu()
            }

            override fun onDrawerStateChanged(newState: Int) {}

            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {}
        }
        mDrawerLayout!!.addDrawerListener(mDrawerToggle)
    }

    /**
     * Highlight the selected item, change the title of the screen, display the new fragment
     * and close the drawer.
     */
    private fun selectItem(position: Int) {
        mCurrentSelectedPosition = position
        if (!setupCalled) return
        mDrawerListView.setItemChecked(position, true)
        mDrawerLayout!!.closeDrawer(mFragmentContainerView)
        mCallbacks!!.onNavigationDrawerItemSelected(position)
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        try {
            mCallbacks = activity as NavigationDrawerCallbacks?
        } catch (e: ClassCastException) {
            throw ClassCastException("Activity must implement NavigationDrawerCallbacks.")
        }

    }

    override fun onDetach() {
        super.onDetach()
        mCallbacks = null
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(STATE_SELECTED_POSITION, mCurrentSelectedPosition)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        // If the drawer is open, show the global app actions in the action bar. See also
        // showGlobalContextActionBar, which controls the top-left area of the action bar.
        if (mDrawerLayout != null && isDrawerOpen) {
            inflater!!.inflate(R.menu.global, menu)
        }
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item!!.itemId == R.id.action_example) {
            Toast.makeText(activity, "Example action.", Toast.LENGTH_SHORT).show()
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    /**
     * Callbacks interface that all activities using this fragment must implement.
     */
    interface NavigationDrawerCallbacks {
        fun onNavigationDrawerItemSelected(position: Int)
    }
}

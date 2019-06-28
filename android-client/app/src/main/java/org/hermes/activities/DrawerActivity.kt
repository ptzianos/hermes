package org.hermes.activities

import androidx.fragment.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import dagger.Module
import dagger.android.AndroidInjection
import javax.inject.Inject

import org.hermes.BaseActivity
import org.hermes.R
import org.hermes.fragments.DashboardFragment
import org.hermes.fragments.EventLogFragment
import org.hermes.fragments.NavigationDrawerFragment
import org.hermes.fragments.SensorListFragment


class DrawerActivity : BaseActivity(), NavigationDrawerFragment.NavigationDrawerCallbacks {

    enum class Tile(val i: Int) {
        DASHBOARD(0),
        EVENT_LOG(1),
        SENSOR_LIST(2)
    }

    @Module
    abstract class DaggerModule

    private lateinit var mNavigationDrawerFragment: NavigationDrawerFragment

    @Inject
    lateinit var dashboardFragment: DashboardFragment

    @Inject
    lateinit var eventLogFragment: EventLogFragment

    @Inject
    lateinit var sensorListFragment: SensorListFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_drawer)
        mNavigationDrawerFragment = supportFragmentManager.findFragmentById(R.id.navigation_drawer) as NavigationDrawerFragment

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
            R.id.navigation_drawer,
            findViewById<View>(R.id.drawer_layout) as DrawerLayout
        )
    }

    override fun onResume() {
        super.onResume()
        val extras = intent.extras
        onNavigationDrawerItemSelected(
            if (extras != null && extras.containsKey("tile")) extras.getInt("tile") else 0)
    }

    override fun onNavigationDrawerItemSelected(position: Int) {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        val fragment: Fragment = when (position) {
            DrawerActivity.Tile.DASHBOARD.i -> {
                toolbar.title = getString(R.string.title_activity_dashboard)
                dashboardFragment
            }
            DrawerActivity.Tile.EVENT_LOG.i -> {
                toolbar.title = getString(R.string.title_activity_event_log)
                eventLogFragment
            }
            DrawerActivity.Tile.SENSOR_LIST.i -> {
                toolbar.title = getString(R.string.title_activity_sensor_list)
                sensorListFragment
            }
            else -> throw Exception("WTF?")
        }
        supportFragmentManager.beginTransaction()
                .replace(R.id.container, fragment)
                .commit()
    }

    /**
     * A placeholder fragment containing a simple view. To be used for fragments that have not been
     * implemented yet.
     */
    class PlaceholderFragment : Fragment() {

        override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            return inflater.inflate(R.layout.fragment_drawer, container, false)
        }

        companion object {
            /**
             * The fragment argument representing the section number for this
             * fragment.
             */
            private val ARG_SECTION_NUMBER = "section_number"

            /**
             * Returns a new instance of this fragment for the given section
             * number.
             */
            fun newInstance(sectionNumber: Int): PlaceholderFragment {
                val fragment = PlaceholderFragment()
                val args = Bundle()
                args.putInt(ARG_SECTION_NUMBER, sectionNumber)
                fragment.arguments = args
                return fragment
            }
        }
    }

}

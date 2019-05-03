package org.hermes.activities

import androidx.fragment.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.paging.PagedList
import androidx.recyclerview.widget.RecyclerView
import dagger.Module
import dagger.android.AndroidInjection
import javax.inject.Inject

import org.hermes.BaseActivity
import org.hermes.LedgerService
import org.hermes.R
import org.hermes.adapters.PagedEventViewAdapter
import org.hermes.adapters.SensorListViewAdapter
import org.hermes.entities.Event
import org.hermes.fragments.EventLogFragment
import org.hermes.fragments.NavigationDrawerFragment
import org.hermes.fragments.SensorListFragment
import org.hermes.viewmodels.EventLogViewModel
import org.hermes.viewmodels.SensorListViewModel


class DrawerActivity : BaseActivity(), NavigationDrawerFragment.NavigationDrawerCallbacks {

    @Module
    abstract class DaggerModule

    private lateinit var mNavigationDrawerFragment: NavigationDrawerFragment

    var dashboardFragment: Fragment = PlaceholderFragment.newInstance(0)

    @Inject
    lateinit var eventLogFragment: EventLogFragment

    @Inject
    lateinit var sensorListFragment: SensorListFragment

    @Inject
    lateinit var sensorListViewModel: SensorListViewModel

    @Inject
    lateinit var eventLogViewModel: EventLogViewModel

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

        eventLogFragment.addCreateViewCallback {
            eventLogViewModel.allEvents.observe(this, Observer<PagedList<Event>?> {
                (findViewById<RecyclerView>(R.id.eventRecyclerView).adapter as PagedEventViewAdapter).submitList(it)
            })
        }
        sensorListFragment.addCreateViewCallback {
            sensorListViewModel.sensors.observe(this, Observer<List<LedgerService.Sensor>?> {
                (findViewById<RecyclerView>(R.id.sensorRecyclerView).adapter as SensorListViewAdapter).submitList(it)
            })
        }
    }

    override fun onNavigationDrawerItemSelected(position: Int) {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        val fragment: Fragment = when (position) {
            1 -> {
                toolbar.title = getString(R.string.title_activity_event_log)
                eventLogFragment
            }
            2 -> {
                toolbar.title = getString(R.string.title_activity_sensor_list)
                sensorListFragment
            }
            else -> {
                toolbar.title = getString(R.string.title_activity_dashboard)
                dashboardFragment
            }
        }
        supportFragmentManager.beginTransaction()
                .replace(R.id.container, fragment)
                .commitAllowingStateLoss()
    }

    /**
     * A placeholder fragment containing a simple view.
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

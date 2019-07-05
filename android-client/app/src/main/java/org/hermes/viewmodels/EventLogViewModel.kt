package org.hermes.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.paging.Config
import androidx.paging.toLiveData
import javax.inject.Inject

import org.hermes.MetadataRepository


class EventLogViewModel @Inject constructor(application: Application,
                                            metadataRepository: MetadataRepository): AndroidViewModel(application) {

    val allEvents = metadataRepository.eventDao.getAllLivePaged().toLiveData(Config(
        /**
         * A good page size is a value that fills at least a screen worth of content on a large
         * device so the MarketUser is unlikely to see a null item.
         * You can play with this constant to observe the paging behavior.
         * <p>
         * It's possible to vary this with list device size, but often unnecessary, unless a
         * user scrolling on a large device is expected to scroll through items more quickly
         * than a small device, such as when the large device uses a grid layout of items.
         */
        pageSize = 30,

        /**
         * If placeholders are enabled, PagedList will report the full size but some items might
         * be null in onBind method (PagedListAdapter triggers a rebind when data is loaded).
         * <p>
         * If placeholders are disabled, onBind will never receive null but as more pages are
         * loaded, the scrollbars will jitter as new pages are loaded. You should probably
         * disable scrollbars if you disable placeholders.
         */
        enablePlaceholders = false,

        /**
         * Maximum number of items a PagedList should hold in memory at once.
         * <p>
         * This number triggers the PagedList to start dropping distant pages as more are loaded.
         */
        maxSize = 200))
}

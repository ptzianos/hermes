package org.hermes.viewmodels

//import android.arch.lifecycle.ViewModel
//import android.arch.lifecycle.ViewModelProvider
//import javax.inject.Inject
//
///**
// * Awesome code from:
// * https://proandroiddev.com/dagger-2-on-android-the-simple-way-f706a2c597e9
// * https://gist.github.com/tfcporciuncula/3b7da90bd42bd61154ff6c536cc5a873#file-viewmodelfactory-kt
// * https://gist.github.com/tfcporciuncula/0218fc7b9c52d67d50709b68a8507b2f#file-bestpostactivity-kt
// */
//class ViewModelFactory<VM : ViewModel> @Inject constructor(
//        private val viewModel: Lazy<VM>
//) : ViewModelProvider.Factory {
//
//    @Suppress("UNCHECKED_CAST")
//    override fun <T : ViewModel> create(modelClass: Class<T>) = viewModel.get() as T
//}
package org.hermes.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import dagger.Module
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

import org.hermes.HermesClientApp
import org.hermes.R


class AboutFragment @Inject constructor() : Fragment() {

    @Module
    abstract class DaggerModule

    @Inject
    lateinit var application: HermesClientApp

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.about_fragment, container, false)
    }
}
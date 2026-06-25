package com.mattiamularoni.saveeat.features.profile.presentation.di

import com.mattiamularoni.saveeat.features.profile.presentation.viewmodel.ProfileViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val profileModule =
    module {
        viewModelOf(::ProfileViewModel)
    }

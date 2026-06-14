package com.mattiamularoni.saveeat.core.di.modules

import com.mattiamularoni.saveeat.core.data.remote.TranslationRemoteDataSource
import com.mattiamularoni.saveeat.core.data.remote.TranslationRemoteDataSourceImpl
import com.mattiamularoni.saveeat.core.data.remote.UnsplashRemoteDataSource
import com.mattiamularoni.saveeat.core.data.remote.UnsplashRemoteDataSourceImpl
import com.mattiamularoni.saveeat.core.data.repository.ImageRepositoryImpl
import com.mattiamularoni.saveeat.core.data.repository.TranslationRepositoryImpl
import com.mattiamularoni.saveeat.core.domain.repository.ImageRepository
import com.mattiamularoni.saveeat.core.domain.repository.TranslationRepository
import com.mattiamularoni.saveeat.core.domain.usecase.ResolveItemImageUseCase
import org.koin.dsl.module

val imageModule = module {
    factory<TranslationRemoteDataSource> { TranslationRemoteDataSourceImpl() }
    factory<UnsplashRemoteDataSource> { UnsplashRemoteDataSourceImpl() }
    factory<TranslationRepository> { TranslationRepositoryImpl(get()) }
    factory<ImageRepository> { ImageRepositoryImpl(get()) }
    factory { ResolveItemImageUseCase(get(), get()) }
}

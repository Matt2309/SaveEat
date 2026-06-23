package com.mattiamularoni.saveeat.features.shopping_list.presentation.di

import com.mattiamularoni.saveeat.features.shopping_list.data.repository.ShoppingListRepositoryImpl
import com.mattiamularoni.saveeat.features.shopping_list.domain.repository.ShoppingListRepository
import com.mattiamularoni.saveeat.features.shopping_list.domain.usecase.AddToShoppingListUseCase
import com.mattiamularoni.saveeat.features.shopping_list.domain.usecase.ClearShoppingListUseCase
import com.mattiamularoni.saveeat.features.shopping_list.domain.usecase.GetShoppingListUseCase
import com.mattiamularoni.saveeat.features.shopping_list.domain.usecase.RemoveFromShoppingListUseCase
import com.mattiamularoni.saveeat.features.shopping_list.presentation.viewmodel.ShoppingListViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val shoppingListModule = module {
    factory<ShoppingListRepository> {
        ShoppingListRepositoryImpl(dao = get())
    }

    factory { AddToShoppingListUseCase(repository = get()) }

    factory { GetShoppingListUseCase(repository = get()) }

    factory { RemoveFromShoppingListUseCase(repository = get()) }

    factory { ClearShoppingListUseCase(repository = get()) }

    viewModelOf(::ShoppingListViewModel)
}

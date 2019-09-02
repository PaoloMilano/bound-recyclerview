package com.magicbluepenguin.testapp.bindings

import androidx.databinding.BindingAdapter

@BindingAdapter("items")
fun <T> bindList(view: BoundRecyclerView<T>, list: List<T>?) {
    list?.let {
        view.boundAdapter?.setItems(it)
    }
}

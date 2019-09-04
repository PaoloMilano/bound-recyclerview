package com.magicbluepenguin.testapp.bindings

import androidx.databinding.BindingAdapter
import com.magicbluepenguin.boundrecyclerview.DifferentiableObject

@BindingAdapter("items")
fun <T : DifferentiableObject> bindList(view: BoundRecyclerView<T>, list: List<T>?) {
    list?.let {
        view.boundAdapter?.setItems(it)
    }
}

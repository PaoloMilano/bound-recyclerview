package com.magicbluepenguin.testapp.bindings

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.BindingAdapter
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.paging.PagedList
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * View bindings for RecyclerView
 */
@BindingAdapter("items")
fun <T : BoundPagedRecyclerViewAdapter.DifferentiableObject> bindList(
    view: BoundRecyclerView<T>,
    list: LiveData<PagedList<T>>?
) {
    list?.observe(view.context as AppCompatActivity,
        Observer<PagedList<T>> { t -> view.boundAdapter?.submitList(t) })
}

@BindingAdapter("isFetchingFromTop")
fun onFetchingFromTop(view: BoundRecyclerView<*>, isFetchingFromTop: LiveData<Boolean>) {
    isFetchingFromTop.observe(view.context as AppCompatActivity,
        Observer<Boolean> { t ->
            view.boundAdapter?.run {
                if (t != null && t != topProgressVisibility) {
                    topProgressVisibility = t
                    if (topProgressVisibility) {
                        notifyItemInserted(0)
                    } else {
                        notifyItemRemoved(0)
                    }
                }
            }
        })
}

@BindingAdapter("isFetchingFromBottom")
fun onFetchingFromBottom(view: BoundRecyclerView<*>, isFetchingFromBottom: LiveData<Boolean>) {
    isFetchingFromBottom.observe(view.context as AppCompatActivity,
        Observer<Boolean> { t ->
            view.boundAdapter?.run {
                if (t != null && t != bottomProgressVisibility) {
                    bottomProgressVisibility = t
                    if (bottomProgressVisibility) {
                        notifyItemInserted(itemCount)
                    } else {
                        notifyItemRemoved(itemCount)
                    }
                }
            }
        })
}

@BindingAdapter("onBottomReached")
fun onLastItemShown(view: BoundRecyclerView<*>, onBottomReached: (Boolean) -> Unit) {
    (view.layoutManager as? LinearLayoutManager)?.let {
        view.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                if (dy < 0) {
                    return
                }

                view.boundAdapter?.run {
                    if ((recyclerView.layoutManager as LinearLayoutManager).findLastVisibleItemPosition() == itemCount - fetchFromBottomThreshold) {
                        onBottomReached.invoke(true)
                    }
                }
            }
        })
    }
}

@BindingAdapter("onTopReached")
fun onFirstItemShown(view: BoundRecyclerView<*>, onTopReached: (Boolean) -> Unit) {
    (view.layoutManager as? LinearLayoutManager)?.let {

        var isScrolling = false

        view.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    isScrolling = true
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                if (!isScrolling || dy > 0) {
                    return
                }

                view.boundAdapter?.run {
                    if ((recyclerView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition() == 0) {
                        onTopReached.invoke(true)
                        isScrolling = false
                    }
                }
            }
        })
    }
}

/**
 * Generic RecyclerView that uses generics to enable reuse with data bindings
 */
class BoundRecyclerView<T : BoundPagedRecyclerViewAdapter.DifferentiableObject>(
    context: Context,
    attrs: AttributeSet
) :
    RecyclerView(context, attrs) {

    init {
        layoutManager = LinearLayoutManager(context)
    }

    var boundAdapter: BoundPagedRecyclerViewAdapter<T, *>?
        set(value) {
            value?.let {
                adapter = it
            }
        }
        @Suppress("UNCHECKED_CAST")
        get() {
            if (adapter is BoundPagedRecyclerViewAdapter<*, *>) {
                return adapter as? BoundPagedRecyclerViewAdapter<T, *>
            }
            return null
        }
}

/**
 * Custom Adapter that uses generics to enable reuse with data bindings. This adapter is low on functionality, limiting
 * itself to providing fields for data to be updated while letting subclasses decide how to answer update events
 */
abstract class BoundPagedRecyclerViewAdapter<T : BoundPagedRecyclerViewAdapter.DifferentiableObject, I : RecyclerView.ViewHolder>(
    val fetchFromBottomThreshold: Int = 3
) :
    PagedListAdapter<T, I>(object : DiffUtil.ItemCallback<T>() {

        override fun areItemsTheSame(oldItem: T, newItem: T): Boolean {
            return oldItem.hasSameId(newItem)
        }

        override fun areContentsTheSame(oldItem: T, newItem: T): Boolean {
            return oldItem.hasSameContents(newItem)
        }
    }) {

    var topProgressVisibility = false
    var bottomProgressVisibility = false

    protected val VIEW_TYPE_PROGRESS_BAR_TOP = 1
    protected val VIEW_TYPE_PROGRESS_BAR_BOTTOM = 2

    override fun getItemViewType(position: Int): Int {
        if (topProgressVisibility && position == 0) {
            return VIEW_TYPE_PROGRESS_BAR_TOP
        }
        if (bottomProgressVisibility && position == itemCount - 1) {
            return VIEW_TYPE_PROGRESS_BAR_BOTTOM
        }
        return super.getItemViewType(position)
    }

    override fun getItemCount(): Int {
        var itemCount = super.getItemCount()
        if (topProgressVisibility) {
            itemCount += 1
        }
        if (bottomProgressVisibility) {
            itemCount += 1
        }
        return itemCount
    }

    override fun getItem(position: Int): T? {
        var adjustedPosition = position
        if (topProgressVisibility) {
            adjustedPosition -= 1
        }
        if (bottomProgressVisibility) {
            adjustedPosition -= 1
        }
        return super.getItem(position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): I {
        return if (viewType == VIEW_TYPE_PROGRESS_BAR_TOP || viewType == VIEW_TYPE_PROGRESS_BAR_BOTTOM) {
            getProgressViewHolder(parent, viewType)
        } else {
            getViewHolder(parent, viewType)
        }
    }

    abstract fun getViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): I

    abstract fun getProgressViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): I

    interface DifferentiableObject {
        fun hasSameId(other: DifferentiableObject): Boolean
        fun hasSameContents(other: DifferentiableObject): Boolean
    }
}

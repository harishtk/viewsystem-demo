package com.example.viewsystem.commons.util.recyclerview

import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.RecyclerView

/**
 * Identifies adapter items that it will be used in [ConcatAdapter]
 * This class functionality creates adapter itemViewType unique for all adapters.
 * This is useful when used in [ConcatAdapter] +
 * [ConcatAdapter.Config.Builder.setIsolateViewType(false)]
 *
 * CAUTION: **Be sure to provide [globalViewItemType] when identifying an item
 * in child adapter, and restore itemViewType back when used internally**
 *
 * Usage:
 *
 *  class RecyclerAdapterSingleHeader(
 *      private val context: Context,
 *      override val concatAdapterIndex: Int,
 *      private val gridSpanSize: Int,
 *  ) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), ConcatenableAdapter
 *
 * ============================================================
 *
 *  Single view type:
 *  override fun getItemViewType(position: Int): Int {
 *      return globalViewItemType()
 *  }
 *
 *  Multiple view type:
 *  override fun getItemViewType(position: Int): Int {
 *      val itemViewType = AdapterViewItemTypeEnum
 *          .toItemType(productItem = items[position])
 *          .itemTypeValue
 *      return globalViewItemType(itemViewType)
 *  }
 *
 *  ============================================================
 *
 *  ViewHolder by item type
 *  override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ProductGridViewHolder {
 *      val localViewType = resolveGlobalViewItemType(viewType)
 *      return when (AdapterViewItemTypeEnum.fromItemTypeValue(localViewType)) {
 *          <...>
 *      }
 *  }
 *
 *  ============================================================
 *
 *  Span size override
 *  override fun spanSizeByType(globalItemViewType: Int): Int {
 *      return gridSpanSize
 *  }
 *
 *  ============================================================
 *
 *  Span size lookup
 *  gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
 *      override fun getSpanSize(position: Int): Int {
 *          val globalItemViewType = parentAdapter.getItemViewType(position)
 *          val spanSize: Int = parentAdapter
 *              .adapters
 *              .filterIsInstance<ConcatenableAdapter>()
 *              .firstOrNull { it.hasGlobalViewItemType(globalItemViewType) }
 *              ?.spanSizeByType(globalItemViewType) ?: layoutManager.spanCount
 *          return spanSize
 *      }
 *  }
 *
 *  ============================================================
 *
 *  Finally
 *  adapterSingleHeader1 = AdapterSingleHeader(
 *      context = context,
 *      concatAdapterIndex = 0,
 *      gridSpanSize = GRID_SPAN_SIZE,
 *  )
 *  adapterSingleNestedItems1 = AdapterSingleNestItems(
 *      context = context,
 *      concatAdapterIndex = 1,
 *      gridSpanSize = GRID_SPAN_SIZE,
 *  )
 *  adapterPagingItems = PagingItemAdapter(
 *      context = context,
 *      concatAdapterIndex = 2
 *  )
 *  val layoutManager = GridLayoutManager(context, GRID_SPAN_SIZE)
 *  val concatAdapterConfig = ConcatAdapter.Config.Builder()
 *      .setIsolateViewTypes(false)
 *      .build()
 *
 *  binding.productGridRecycler.layoutManager = layoutManager
 *  binding.productGridRecycler.adapter = concatAdapter
 *
 *  ============================================================
 */
interface ConcatenableAdapter {
    val concatAdapterIndex: Int

    /**
     * Returns span size when used in Grid Span size is resolved in numbers.
     * - Grid spanCount=2
     *      - When takes 1 column out of 2, spanSize = 1
     *      - When takes 2 column out of 2, spanSize = 1
     *      - When takes both columns 2, spanSize = 2
     * By default this does not change span size
     * @param globalItemViewType global item view type (calculated with [resolveGlobalViewItemType])
     * @return span size
     */
    fun spanSizeByType(globalItemViewType: Int) = 1

    /**
     * @return true if item type belongs to adapter
     */
    fun hasGlobalViewItemType(globalItemViewType: Int): Boolean {
        val minItemIndex = VIEW_ITEM_TYPE_MULTIPLIER *
                (concatAdapterIndex + 1)
        val maxItemIndex = VIEW_ITEM_TYPE_MULTIPLIER *
                (concatAdapterIndex + 2)
        return globalItemViewType in minItemIndex until maxItemIndex
    }

    /**
     * @return [RecyclerView.Adapter.getItemViewType] when used in
     * [ConcatAdapter] to provide a unique item type
     */
    fun globalViewItemType(localItemViewType: Int = 0): Int {
        return VIEW_ITEM_TYPE_MULTIPLIER * (concatAdapterIndex + 1) +
                localItemViewType
    }

    /**
     * Returns the original view item type for internal use of the adapter
     * @param globalItemViewType is calculated type with [globalItemViewType]
     * @return resolved local itemViewType
     */
    fun resolveGlobalViewItemType(globalItemViewType: Int): Int {
        return globalItemViewType - (VIEW_ITEM_TYPE_MULTIPLIER *
                (concatAdapterIndex + 1))
    }

    companion object {
        private const val VIEW_ITEM_TYPE_MULTIPLIER = 100
    }
}
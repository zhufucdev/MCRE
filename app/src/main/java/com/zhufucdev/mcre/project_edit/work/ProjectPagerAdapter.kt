package com.zhufucdev.mcre.project_edit.work

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.DiffUtil
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.zhufucdev.mcre.R
import com.zhufucdev.mcre.activity.ProjectActivity
import com.zhufucdev.mcre.project_edit.fragment.AssetFragment
import com.zhufucdev.mcre.project_edit.fragment.DataFragment
import kotlinx.android.synthetic.main.content_project.*

class ProjectPagerAdapter(
    manager: FragmentManager,
    private val attachTo: Pair<TabLayout, ViewPager2>,
    private val parent: ProjectActivity
) :
    FragmentStateAdapter(manager, parent.lifecycle) {
    val dataFragment =
        DataFragment(this, parent.project)
    val assetFragment = AssetFragment(parent.project)

    private var mTabs = arrayListOf(
        Tab(
            dataFragment,
            R.string.title_data,
            this,
            false
        ),
        Tab(
            assetFragment,
            R.string.title_assets,
            this,
            false
        )
    )
    private var tabsClone: ArrayList<Tab>? = null
    val tabs: ArrayList<Tab>
        get() {
            return if (!isUpdating) mTabs
            else {
                if (tabsClone == null)
                    tabsClone = mTabs.clone() as ArrayList<Tab>
                tabsClone!!
            }
        }

    init {
        attachTo.second.adapter = this
        TabLayoutMediator(attachTo.first, attachTo.second) { tab, position ->
            tab.text = mTabs[position].let {
                if (it.title is Int)
                    parent.getString(it.title)
                else
                    it.title.toString()
            }
        }.attach()
    }

    fun containsTab(tab: Tab) = tabs.contains(tab)

    fun forEach(action: (Tab) -> Unit) = mTabs.forEach(action)

    private var isUpdating = false

    fun beginUpdate() {
        isUpdating = true
    }

    fun endUpdate() {
        if (isUpdating && tabsClone != null) {
            //DiffUtil.calculateDiff(DiffCallback(mTabs, tabs)).dispatchUpdatesTo(this)
            mTabs = tabsClone!!
            tabsClone = null
        }
        notifyDataSetChanged()
        isUpdating = false
        forEach { it.updateIcon() }
        attachTo.first.tabMode = if (tabs.all { !it.closeable }) TabLayout.MODE_FIXED else TabLayout.MODE_SCROLLABLE
    }

    fun addTab(fragment: Fragment, nameID: Int): Tab {
        val r = Tab(fragment, nameID, this)
        if (!containsTab(r)) {
            mTabs.add(r)
            notifyDataSetChanged()
            endUpdate()
        }
        attachTo.first.tabMode = TabLayout.MODE_SCROLLABLE
        return r
    }

    fun getTab(pos: Int) = tabs[pos]

    fun addTab(tab: Tab) {
        if (!containsTab(tab)) {
            tabs.add(tab)
            if (!isUpdating) {
                notifyDataSetChanged()
                if (tab.closeable) {
                    attachTo.first.tabMode = TabLayout.MODE_SCROLLABLE
                }
            }
            endUpdate()
        }
    }

    override fun getItemCount(): Int = mTabs.size

    override fun createFragment(position: Int): Fragment = mTabs[position].fragment

    override fun getItemId(position: Int): Long = mTabs[position].hashCode().toLong()

    override fun containsItem(itemId: Long): Boolean = mTabs.any { it.hashCode().toLong() == itemId }

    open class Tab(
        val fragment: Fragment,
        val title: Any,
        private val parent: ProjectPagerAdapter,
        val closeable: Boolean = true
    ) {
        val position: Int get() = parent.mTabs.indexOf(this)
        private var mPinned = false

        var isPinned: Boolean
            get() = mPinned || !closeable
            set(value) {
                if (closeable) {
                    mPinned = value
                    updateIcon()
                }
            }

        fun updateIcon() {
            parent.attachTo.first.getTabAt(position)!!.apply {
                if (mPinned) {
                    setIcon(R.drawable.ic_pin_on_surface)
                } else {
                    icon = null
                }
            }
        }

        fun close() {
            if (!closeable) throw UnsupportedOperationException("Closing an uncloseable tab $fragment.")
            parent.apply {
                tabs.remove(this@Tab)
                if (!isUpdating) {
                    endUpdate()
                }
            }
        }

        fun head() {
            parent.parent.view_pager.setCurrentItem(position, true)
        }

        override fun equals(other: Any?): Boolean =
            other is Tab && other.fragment == fragment && other.title == title && other.closeable == closeable

        override fun hashCode(): Int {
            var result = fragment.hashCode()
            result = 31 * result + title.hashCode()
            return result
        }
    }

    private inner class DiffCallback(val old: List<Tab>, val new: List<Tab>) : DiffUtil.Callback() {
        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
            old[oldItemPosition] == new[newItemPosition]

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
            old[oldItemPosition].title == new[newItemPosition].title

        override fun getOldListSize(): Int = old.size

        override fun getNewListSize(): Int = new.size

    }
}


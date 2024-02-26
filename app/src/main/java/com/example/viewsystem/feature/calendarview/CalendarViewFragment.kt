package com.example.viewsystem.feature.calendarview

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.example.viewsystem.R
import com.example.viewsystem.databinding.FragmentCalendarViewBinding
import com.example.viewsystem.extensions.autoCleared
import com.example.viewsystem.extensions.themeColor
import com.google.android.material.transition.MaterialContainerTransform
import com.haibin.calendarview.Calendar
import com.haibin.calendarview.CalendarView
import timber.log.Timber
import java.util.Date
import kotlin.properties.Delegates

class CalendarViewFragment
    : Fragment(R.layout.fragment_calendar_view), CalendarView.OnCalendarSelectListener,
    CalendarView.OnYearChangeListener {

    private var binding: FragmentCalendarViewBinding by autoCleared()

    private var year: Int by Delegates.notNull<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedElementEnterTransition = MaterialContainerTransform().apply {
            drawingViewId = R.id.fragment_container
            scrimColor = Color.TRANSPARENT
            setAllContainerColors(requireContext().themeColor(com.google.android.material.R.attr.colorSurface))

            /*transitionListener(
                onTransitionStart = { binding.icCamera.isVisible = false; },
                onTransitionEnd = {
                    binding.icCamera.apply { scaleX = 0.8f; scaleY = 0.8f; }
                    TransitionManager.beginDelayedTransition(
                        binding.profileImageContainer as ViewGroup,
                        ScaleTransition().apply {
                            duration = 150L
                            interpolator = OvershootInterpolator(2f)
                        }
                    )
                    binding.icCamera.apply { scaleX = 1f; scaleY = 1f; }
                    binding.icCamera.isVisible = true
                }
            )*/
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentCalendarViewBinding.bind(view)
        binding.bindToolbar()

        val from = arguments?.getString("from", "unknown")
        if (from == "routing") {
            postponeEnterTransition()

            val titleTransition = arguments?.getString("titleTransitionName") ?: ""
            binding.toolbarIncluded.toolbarTitle.transitionName = titleTransition

            startPostponedEnterTransition()
        }

        binding.bindState()
    }

    private fun FragmentCalendarViewBinding.bindState() {
        // bindCalendarScheme()

        tvMonthDay.setOnClickListener {
            if (!calendarLayout.isExpand) {
                calendarLayout.expand()
                return@setOnClickListener
            }
            calendarView.showYearSelectLayout(year)
            tvLunar.isVisible = false
            tvYear.isVisible = false
            tvMonthDay.setText(year.toString())
        }
        flCurrent.setOnClickListener {
            calendarView.scrollToCurrent()
        }

        calendarView.setOnCalendarSelectListener(this@CalendarViewFragment)
        calendarView.setOnYearChangeListener(this@CalendarViewFragment)
        calendarView.setWeekStarWithMon()
        tvYear.setText(calendarView.curYear.toString())
        year = calendarView.curYear
        tvMonthDay.setText("${calendarView.curMonth} ${calendarView.curDay}")
        tvLunar.setText("Today")
        tvCurrentDay.setText(calendarView.curDay.toString())

        bindAndroidCalendar()
    }

    private fun FragmentCalendarViewBinding.bindCalendarScheme() {
        val year = calendarView.curYear
        val month = calendarView.curMonth

        val map: MutableMap<String, Calendar> = HashMap()
        map[getSchemeCalendar(year, month, 3, -0xbf24db, "Scheme 1").toString()] =
            getSchemeCalendar(year, month, 3, -0xbf24db, "Scheme 1")
        map[getSchemeCalendar(year, month, 6, -0x196ec8, "Scheme 2").toString()] =
            getSchemeCalendar(year, month, 6, -0x196ec8, "Scheme 2")
        map[getSchemeCalendar(year, month, 9, -0x20ecaa, "Scheme 3").toString()] =
            getSchemeCalendar(year, month, 9, -0x20ecaa, "Scheme 3")
        map[getSchemeCalendar(year, month, 13, -0x123a93, "Scheme 4").toString()] =
            getSchemeCalendar(year, month, 13, -0x123a93, "Scheme 4")
        map[getSchemeCalendar(year, month, 14, -0x123a93, "Scheme 4").toString()] =
            getSchemeCalendar(year, month, 14, -0x123a93, "Scheme 4")
        map[getSchemeCalendar(year, month, 15, -0x5533bc, "Scheme 1").toString()] =
            getSchemeCalendar(year, month, 15, -0x5533bc, "Scheme 1")
        map[getSchemeCalendar(year, month, 18, -0x43ec10, "Scheme 4").toString()] =
            getSchemeCalendar(year, month, 18, -0x43ec10, "Scheme 4")
        map[getSchemeCalendar(year, month, 25, -0xec5310, "Scheme 1").toString()] =
            getSchemeCalendar(year, month, 25, -0xec5310, "Scheme 1")
        map[getSchemeCalendar(year, month, 27, -0xec5310, "Scheme 5").toString()] =
            getSchemeCalendar(year, month, 27, -0xec5310, "Scheme 5")
        //此方法在巨大的数据量上不影响遍历性能，推荐使用
        //此方法在巨大的数据量上不影响遍历性能，推荐使用
        calendarView.setSchemeDate(map)
    }

    private fun FragmentCalendarViewBinding.bindAndroidCalendar() {
        // androidCalendarView.setDate(Date().time, true, true)
    }

    private fun FragmentCalendarViewBinding.bindToolbar() {
        toolbarIncluded.toolbarTitle.text =
            getString(R.string.title_calendar_view)
    }

    private fun getSchemeCalendar(
        year: Int,
        month: Int,
        day: Int,
        color: Int,
        text: String
    ): Calendar {
        val calendar = Calendar()
        calendar.year = year
        calendar.month = month
        calendar.day = day
        calendar.schemeColor = color //如果单独标记颜色、则会使用这个颜色
        calendar.scheme = text
        calendar.addScheme(Calendar.Scheme())
        calendar.addScheme(-0xff7800, "Scheme 1")
        calendar.addScheme(-0xff7800, "Scheme 2")
        return calendar
    }

    override fun onYearChange(year: Int) {
        binding.tvMonthDay.setText(year.toString())
    }

    override fun onCalendarOutOfRange(calendar: Calendar?) {
        TODO("Not yet implemented")
    }

    override fun onCalendarSelect(calendar: Calendar, isClick: Boolean) {
        with(binding) {
            tvLunar.isVisible = false
            tvYear.isVisible = true
            tvMonthDay.setText("${calendar.month} ${calendar.day}")
            tvYear.setText(calendar.year.toString())
            tvLunar.setText(calendar.lunar)
            year = calendar.year

            Timber.e("  -- " + calendar.year + "  --  " + calendar.month + "  -- " + calendar.day + "  --  " + isClick + "  --   " + calendar.scheme)
        }
    }
}
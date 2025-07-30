package com.example.itunesapi

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import android.content.Context
import java.text.SimpleDateFormat
import java.util.*

class FocusStatsFragment : Fragment() {

    private lateinit var barChart: BarChart
    private lateinit var topicSpinner: Spinner       // 주제 선택 Spinner
    private lateinit var rangeSpinner: Spinner       // 기간 선택 Spinner ("주간", "월간", "연간")

    private val prefs by lazy {
        requireContext().getSharedPreferences("FocusTimerPrefs", Context.MODE_PRIVATE)
    }

    private var selectedTopic: String = ""
    private var selectedRange: String = "주간"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_focus_stats, container, false)
        barChart = view.findViewById(R.id.barChart)
        topicSpinner = view.findViewById(R.id.topicSpinner)
        rangeSpinner = view.findViewById(R.id.spinnerRange)

        setupTopicSpinner()
        setupRangeSpinner()

        return view
    }

    private fun setupTopicSpinner() {
        val allKeys = prefs.all.keys
        val topicSet = mutableSetOf<String>()

        // 저장된 키들에서 주제만 추출 (예: "공부-2025-07-29" -> "공부")
        for (key in allKeys) {
            val parts = key.split("-")
            if (parts.size >= 4) {
                val topic = parts.dropLast(3).joinToString("-")
                topicSet.add(topic)
            }
        }

        val topicList = topicSet.toList().sorted()
        if (topicList.isEmpty()) {
            // 저장된 주제가 없으면 기본값 세팅
            selectedTopic = "공부"
        } else {
            selectedTopic = topicList[0]
        }

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, topicList)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        topicSpinner.adapter = adapter

        topicSpinner.setSelection(topicList.indexOf(selectedTopic))

        topicSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedTopic = topicList[position]
                updateChart()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupRangeSpinner() {
        val ranges = listOf("주간", "월간", "연간")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, ranges)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        rangeSpinner.adapter = adapter
        rangeSpinner.setSelection(ranges.indexOf(selectedRange))

        rangeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedRange = ranges[position]
                updateChart()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun updateChart() {
        val dataMap = when(selectedRange) {
            "주간" -> getWeeklyData(selectedTopic)
            "월간" -> getMonthlyData(selectedTopic)
            "연간" -> getYearlyData(selectedTopic)
            else -> emptyMap()
        }
        updateBarChart(dataMap)
    }

    private fun updateBarChart(dataMap: Map<String, Int>) {
        val entries = mutableListOf<BarEntry>()
        val labels = mutableListOf<String>()
        var index = 0f

        for ((label, time) in dataMap) {
            entries.add(BarEntry(index, time.toFloat()))
            labels.add(label)
            index += 1f
        }

        val dataSet = BarDataSet(entries, "$selectedTopic 집중 시간 (초)").apply {
            color = resources.getColor(R.color.teal_700, null)
        }
        val data = BarData(dataSet)
        data.barWidth = 0.9f

        barChart.data = data
        barChart.setFitBars(true)
        barChart.description.isEnabled = false
        barChart.setDrawGridBackground(false)

        val xAxis = barChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        xAxis.setDrawGridLines(false)
        xAxis.granularity = 1f
        xAxis.labelRotationAngle = -45f
        xAxis.textColor = android.graphics.Color.WHITE

        barChart.axisLeft.axisMinimum = 0f
        barChart.axisLeft.textColor = android.graphics.Color.WHITE
        barChart.axisRight.isEnabled = false
        barChart.description.textColor = android.graphics.Color.WHITE

        barChart.invalidate()
    }

    // 아래 함수들은 기존에 작성한 주간/월간/연간 데이터 생성 함수 사용

    private fun loadTime(topic: String, date: String): Int {
        val key = "$topic-$date"
        return prefs.getInt(key, 0)
    }

    private fun loadTimeRange(topic: String, startDate: String, endDate: String): Map<String, Int> {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val map = mutableMapOf<String, Int>()

        val startCal = Calendar.getInstance().apply { time = sdf.parse(startDate)!! }
        val endCal = Calendar.getInstance().apply { time = sdf.parse(endDate)!! }

        while (!startCal.after(endCal)) {
            val dateStr = sdf.format(startCal.time)
            val time = loadTime(topic, dateStr)
            map[dateStr] = time
            startCal.add(Calendar.DATE, 1)
        }
        return map
    }

    private fun getWeeklyData(topic: String, weeksBack: Int = 4): Map<String, Int> {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val weeklyMap = mutableMapOf<String, Int>()
        val calendar = Calendar.getInstance()

        for (i in 0 until weeksBack) {
            calendar.time = Date()

            // 오늘 기준으로 몇 주 전인지 이동
            calendar.add(Calendar.WEEK_OF_YEAR, -i)

            // 이번 주 월요일 계산
            val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
            val diffToMonday = if (dayOfWeek == Calendar.SUNDAY) 6 else dayOfWeek - Calendar.MONDAY
            calendar.add(Calendar.DATE, -diffToMonday)
            val startOfWeek = calendar.time

            // 이번 주 일요일 계산
            calendar.add(Calendar.DATE, 6)
            val endOfWeek = calendar.time

            val startStr = sdf.format(startOfWeek)
            val endStr = sdf.format(endOfWeek)

            val dailyData = loadTimeRange(topic, startStr, endStr)
            val sum = dailyData.values.sum()

            val weekOfMonth = Calendar.getInstance().apply {
                time = startOfWeek
            }.get(Calendar.WEEK_OF_MONTH)

            val label = "${startOfWeek.month + 1}월 ${weekOfMonth}주차"
            weeklyMap[label] = sum
        }
        return weeklyMap.toSortedMap()
    }

    private fun getMonthlyData(topic: String, monthsBack: Int = 6): Map<String, Int> {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val calendar = Calendar.getInstance()
        val monthlyMap = mutableMapOf<String, Int>()

        for (i in 0 until monthsBack) {
            calendar.time = Date()
            calendar.add(Calendar.MONTH, -i)
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            val startOfMonth = calendar.time

            calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
            val endOfMonth = calendar.time

            val startStr = sdf.format(startOfMonth)
            val endStr = sdf.format(endOfMonth)

            val dailyData = loadTimeRange(topic, startStr, endStr)
            val sum = dailyData.values.sum()

            val label = "${startOfMonth.month + 1}월"
            monthlyMap[label] = sum
        }

        return monthlyMap.toSortedMap()
    }

    private fun getYearlyData(topic: String, yearsBack: Int = 3): Map<String, Int> {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val calendar = Calendar.getInstance()
        val yearlyMap = mutableMapOf<String, Int>()

        for (i in 0 until yearsBack) {
            calendar.time = Date()
            calendar.add(Calendar.YEAR, -i)
            calendar.set(Calendar.DAY_OF_YEAR, 1)
            val startOfYear = calendar.time

            calendar.set(Calendar.DAY_OF_YEAR, calendar.getActualMaximum(Calendar.DAY_OF_YEAR))
            val endOfYear = calendar.time

            val startStr = sdf.format(startOfYear)
            val endStr = sdf.format(endOfYear)

            val dailyData = loadTimeRange(topic, startStr, endStr)
            val sum = dailyData.values.sum()

            val label = "${startOfYear.year + 1900}년"
            yearlyMap[label] = sum
        }

        return yearlyMap.toSortedMap()
    }

}

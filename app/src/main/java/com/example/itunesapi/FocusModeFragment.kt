package com.example.itunesapi

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import java.text.SimpleDateFormat
import java.util.*

class FocusModeFragment : Fragment() {

    private lateinit var addTopicButton: Button
    private lateinit var viewStatsButton: Button
    private lateinit var topicContainer: LinearLayout
    private lateinit var barChart: BarChart
    private lateinit var viewModel: TimerViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_focus_mode, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        addTopicButton = view.findViewById(R.id.btnAddTopic)
        viewStatsButton = view.findViewById(R.id.btnViewStats)
        topicContainer = view.findViewById(R.id.topicContainer)
        barChart = view.findViewById(R.id.barChart)

        viewModel = ViewModelProvider(requireActivity())[TimerViewModel::class.java]

        addTopicButton.setOnClickListener { showAddTopicDialog() }

        viewStatsButton.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, FocusStatsFragment())
                .addToBackStack(null)
                .commit()
        }

        viewModel.focusTimers.observe(viewLifecycleOwner) { topicMap ->
            updateTopicButtons(topicMap)
            updateBarChart(topicMap)
        }
    }

    private fun updateTopicButtons(topicMap: Map<String, Int>) {
        topicContainer.removeAllViews()
        for ((topic, time) in topicMap) {
            val button = Button(requireContext()).apply {
                text = "$topic (${formatTime(time)})"
                setOnClickListener {
                    parentFragmentManager.beginTransaction()
                        .replace(
                            R.id.fragment_container,
                            FocusTimerFragment.newInstance(topic, "", arrayListOf(), 0)
                        )
                        .addToBackStack(null)
                        .commit()
                }
            }
            topicContainer.addView(button)
        }
    }

    private fun updateBarChart(topicMap: Map<String, Int>) {
        val entries = ArrayList<BarEntry>()
        val labels = ArrayList<String>()
        var index = 0f
        for ((topic, time) in topicMap) {
            entries.add(BarEntry(index, time.toFloat()))
            labels.add(topic)
            index += 1f
        }

        val dataSet = BarDataSet(entries, "공부 시간 (분)").apply {
            color = ContextCompat.getColor(requireContext(), R.color.teal_700)
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

    private fun showAddTopicDialog() {
        val input = EditText(requireContext())
        AlertDialog.Builder(requireContext())
            .setTitle("주제 추가")
            .setView(input)
            .setPositiveButton("확인") { _, _ ->
                val topic = input.text.toString()
                viewModel.addTopic(topic)
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun formatTime(seconds: Int): String {
        val minutes = seconds / 60
        val secs = seconds % 60
        return "${minutes}분 ${secs}초"
    }

    private val prefs by lazy {
        requireContext().getSharedPreferences("FocusTimerPrefs", Context.MODE_PRIVATE)
    }

    private fun getTodayKey(topic: String): String {
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        return "$topic-$date"
    }

    private fun saveTime(topic: String, seconds: Int) {
        val key = getTodayKey(topic)
        val current = prefs.getInt(key, 0)
        prefs.edit().putInt(key, current + seconds).apply()
    }

    private fun loadTime(topic: String, date: String): Int {
        val key = "$topic-$date"
        return prefs.getInt(key, 0)
    }

    private fun loadTimeRange(topic: String, daysBack: Int): Map<String, Int> {
        val map = mutableMapOf<String, Int>()
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val cal = Calendar.getInstance()
        for (i in 0 until daysBack) {
            val date = sdf.format(cal.time)
            val key = "$topic-$date"
            val value = prefs.getInt(key, 0)
            map[date] = value
            cal.add(Calendar.DATE, -1)
        }
        return map
    }
}



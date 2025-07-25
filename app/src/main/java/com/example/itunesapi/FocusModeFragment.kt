package com.example.itunesapi

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import androidx.lifecycle.ViewModelProvider
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter

class FocusModeFragment : Fragment() {

    private lateinit var addTopicButton: Button
    private lateinit var topicContainer: LinearLayout
    private lateinit var viewModel: TimerViewModel
    private lateinit var barChart: BarChart

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_focus_mode, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        addTopicButton = view.findViewById(R.id.btnAddTopic)
        topicContainer = view.findViewById(R.id.topicContainer)
        barChart = view.findViewById(R.id.barChart)

        viewModel = ViewModelProvider(requireActivity())[TimerViewModel::class.java]

        addTopicButton.setOnClickListener {
            showAddTopicDialog()
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
                        .replace(R.id.fragment_container, FocusTimerFragment.newInstance(topic))
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

        barChart.axisLeft.axisMinimum = 0f
        barChart.axisRight.isEnabled = false
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

    private fun formatTime(minutes: Int): String {
        val hours = minutes / 60
        val mins = minutes % 60
        return "${hours}시간 ${mins}분"
    }
}




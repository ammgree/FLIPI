package com.example.itunesapi

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [EditPlaylistFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class EditPlaylistFragment : Fragment() {

    private lateinit var titleEditText: EditText
    private lateinit var imageView: ImageView
    private lateinit var saveButton: Button

    private var title: String? = null
    private var imageUrl: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            title = it.getString("title")
            imageUrl = it.getString("imageUrl")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_edit_playlist, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        titleEditText = view.findViewById(R.id.titleEditText)
        imageView = view.findViewById(R.id.imageView)
        saveButton = view.findViewById(R.id.saveButton)

        titleEditText.setText(title)
        Glide.with(requireContext()).load(imageUrl).into(imageView)

        saveButton.setOnClickListener {
            val newTitle = titleEditText.text.toString()
            Toast.makeText(requireContext(), "플리 제목을 '$newTitle'로 변경했습니다!", Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
        }
    }
}

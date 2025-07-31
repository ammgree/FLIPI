package com.example.itunesapi

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.auth.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class SettingFragment : Fragment(R.layout.fragment_setting) {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private lateinit var storageRef: StorageReference

    private lateinit var profileImage: ImageView
    private lateinit var usernameEditText: EditText
    private lateinit var currentPwEditText: EditText
    private lateinit var checkPwButton: Button
    private lateinit var newPwEditText: EditText
    private lateinit var confirmPwEditText: EditText
    private lateinit var saveButton: Button
    private lateinit var logoutText: TextView
    private lateinit var backBtn: ImageView

    private val PICK_IMAGE_REQUEST = 1
    private var imageUri: Uri? = null
    private var passwordConfirmed = false

    private var usernameFromDB: String? = null
    private var moodFromDB: String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        profileImage = view.findViewById(R.id.profileImage)
        checkPwButton = view.findViewById(R.id.confirmPwBtn)
        usernameEditText = view.findViewById(R.id.regName)
        currentPwEditText = view.findViewById(R.id.regPwConfirm)
        newPwEditText = view.findViewById(R.id.regPw)
        confirmPwEditText = view.findViewById(R.id.prePwConfirm)
        saveButton = view.findViewById(R.id.saveBtn)
        logoutText = view.findViewById(R.id.logoutText)
        backBtn = view.findViewById(R.id.backBtn)

        storageRef = storage.reference

        newPwEditText.visibility = View.GONE
        confirmPwEditText.visibility = View.GONE

        loadUserInfo()

        profileImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }

        checkPwButton.setOnClickListener {
            val password = currentPwEditText.text.toString()
            if (password.isEmpty()) {
                Toast.makeText(context, "비밀번호를 입력해주세요",Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val email = auth.currentUser?.email ?: return@setOnClickListener
            val credential = EmailAuthProvider.getCredential(email, password)

            auth.currentUser?.reauthenticate(credential)
                ?.addOnSuccessListener {
                    Toast.makeText(context, "비밀번호 확인 완료", Toast.LENGTH_SHORT).show()
                    passwordConfirmed = true
                    newPwEditText.visibility = View.VISIBLE
                    confirmPwEditText.visibility = View.VISIBLE
                }
                ?.addOnFailureListener {
                    Toast.makeText(context, "비밀번호가 틀렸습니다", Toast.LENGTH_SHORT).show()
                }
        }

        saveButton.setOnClickListener {
            if (passwordConfirmed) {
                val newPw = newPwEditText.text.toString()
                val confirmPw = confirmPwEditText.text.toString()

                if (newPw.length < 6) {
                    Toast.makeText(context, "비밀번호는 최소 6자 이상이어야 합니다", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                if (newPw != confirmPw) {
                    Toast.makeText(context, "비밀번호가 일치하지 않습니다", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                auth.currentUser?.updatePassword(newPw)
                    ?.addOnSuccessListener {
                        Toast.makeText(context, "비밀번호 저장 완료", Toast.LENGTH_SHORT).show()
                        uploadProfileAndUsername()
                    }
            } else {
                uploadProfileAndUsername()
            }
        }

        logoutText.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setMessage("로그아웃 하시겠습니까?")
                .setPositiveButton("네") { _, _ ->
                    auth.signOut()
                    Toast.makeText(context, "로그아웃 되었습니다", Toast.LENGTH_SHORT).show()
                    val intent = Intent(requireContext(), Login::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    requireActivity().finish()
                }
                .setNegativeButton("아니오", null)
                .show()
        }

        backBtn.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setMessage("설정에서 나가시겠습니까?")
                .setPositiveButton("네") { _, _ ->
                    goToHomeWithBundle()
                }
                .setNegativeButton("아니오", null)
                .show()
        }
    }

    private fun uploadProfileAndUsername() {
        val uid = auth.currentUser?.uid ?: return
        val newName = usernameEditText.text.toString()

        if (imageUri != null) {
            val imageRef = storageRef.child("profileImages/$uid.jpg")
            imageRef.putFile(imageUri!!)
                .addOnSuccessListener {
                    imageRef.downloadUrl.addOnSuccessListener { uri ->
                        updateUserInfo(uid, newName, uri.toString())
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(context, "이미지 업로드 실패", Toast.LENGTH_SHORT).show()
                }
        } else {
            updateUserInfo(uid, newName, null)
        }
    }

    private fun updateUserInfo(uid: String, newName: String, newProfileImageUrl: String?) {
        val updates = mutableMapOf<String, Any>("username" to newName)
        if (newProfileImageUrl != null) {
            updates["profileImageUrl"] = newProfileImageUrl
        }

        db.collection("users").document(uid)
            .update(updates)
            .addOnSuccessListener {
                Toast.makeText(context, "사용자 정보 저장 완료", Toast.LENGTH_SHORT).show()
                goToHomeWithBundle()
            }
    }

    private fun goToHomeWithBundle() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid)
            .get()
            .addOnSuccessListener { doc ->
                val username = doc.getString("username")
                val mood = doc.getString("mood")

                val bundle = Bundle().apply {
                    putString("username", username)
                    putString("mood", mood)
                }

                val homeFragment = HomeFragment()
                homeFragment.arguments = bundle

                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, homeFragment)
                    .commit()
            }
    }

    private fun loadUserInfo() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid)
            .get()
            .addOnSuccessListener { doc ->
                val username = doc.getString("username") ?: ""
                val profileImageUrl = doc.getString("profileImageUrl")
                usernameFromDB = username
                moodFromDB = doc.getString("mood")

                usernameEditText.setText(username)

                if (!profileImageUrl.isNullOrEmpty()) {
                    Glide.with(this)
                        .load(profileImageUrl)
                        .circleCrop()
                        .into(profileImage)
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "사용자 정보를 불러오지 못했습니다", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == AppCompatActivity.RESULT_OK && data != null && data.data != null) {
            imageUri = data.data
            profileImage.setImageURI(imageUri)
        }
    }
}

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

// 설정 화면을 담당하는 Fragment
class SettingFragment : Fragment(R.layout.fragment_setting) {

    // Firebase 인스턴스 초기화
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private lateinit var storageRef: StorageReference

    // UI 요소들 선언
    private lateinit var profileImage: ImageView
    private lateinit var usernameEditText: EditText
    private lateinit var currentPwEditText: EditText
    private lateinit var checkPwButton: Button
    private lateinit var newPwEditText: EditText
    private lateinit var confirmPwEditText: EditText
    private lateinit var saveButton: Button
    private lateinit var logoutText: TextView
    private lateinit var backBtn: ImageView

    // 이미지 선택 관련 상수 및 변수
    private val PICK_IMAGE_REQUEST = 1
    private var imageUri: Uri? = null
    private var passwordConfirmed = false

    // 사용자 정보 변수
    private var usernameFromDB: String? = null
    private var moodFromDB: String? = null

    // Fragment의 View가 생성된 이후 호출되는 함수
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // UI 요소 바인딩
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

        // 비밀번호 확인 전까지 새 비밀번호 입력창 숨김
        newPwEditText.visibility = View.GONE
        confirmPwEditText.visibility = View.GONE

        // Firestore에서 사용자 정보 불러오기
        loadUserInfo()

        // 프로필 이미지 클릭 시 갤러리에서 이미지 선택
        profileImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }

        // 현재 비밀번호 확인 버튼 클릭 시
        checkPwButton.setOnClickListener {
            val password = currentPwEditText.text.toString()
            if (password.isEmpty()) {
                Toast.makeText(context, "비밀번호를 입력해주세요",Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val email = auth.currentUser?.email ?: return@setOnClickListener
            val credential = EmailAuthProvider.getCredential(email, password)

            // 비밀번호 재인증 요청
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

        // 저장 버튼 클릭 시 사용자 정보 업데이트
        saveButton.setOnClickListener {
            val newName = usernameEditText.text.toString().trim()
            if (newName.isEmpty()) {
                Toast.makeText(context, "사용자 이름을 입력해주세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

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

                // 새 비밀번호 저장
                auth.currentUser?.updatePassword(newPw)
                    ?.addOnSuccessListener {
                        Toast.makeText(context, "비밀번호 저장 완료", Toast.LENGTH_SHORT).show()
                        uploadProfileAndUsername()
                    }
            } else {
                uploadProfileAndUsername()
            }
        }

        // 로그아웃 텍스트 클릭 시 확인 다이얼로그 → 로그아웃 및 로그인 화면 이동
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

        // 뒤로가기 버튼 클릭 시 확인 후 홈 화면으로 이동
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

    // 프로필 이미지와 사용자 이름을 업데이트하는 함수
    private fun uploadProfileAndUsername() {
        val uid = auth.currentUser?.uid ?: return
        val newName = usernameEditText.text.toString()

        // 이미지 선택된 경우 Storage에 업로드
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

    // Firestore에 사용자 이름 및 프로필 이미지 URL 업데이트
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

    // 홈 화면으로 이동하면서 사용자 정보 번들로 전달
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

    // Firestore에서 사용자 정보(이름, 프로필 이미지, 기분) 불러오기
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

                // 프로필 이미지가 있으면 Glide로 로드
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

    // 이미지 선택 후 호출되는 콜백 함수
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == AppCompatActivity.RESULT_OK && data != null && data.data != null) {
            imageUri = data.data
            profileImage.setImageURI(imageUri)
        }
    }
}

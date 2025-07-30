package com.example.itunesapi

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query


// 데이터 모델 클래스: 스토리 아이템을 나타냄
import android.os.Parcelable
import android.util.Log
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.parcelize.Parcelize
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import android.location.Geocoder
import java.util.Locale

@Parcelize
data class StoryItem(
    val title: String = "",
    val artist: String = "",
    val albumArtUrl: String = ""
) : Parcelable


class HomeFragment : Fragment(R.layout.fragment_home) {

    private lateinit var storyRecyclerView: RecyclerView
    private val db = FirebaseFirestore.getInstance()

    private val storyList = mutableListOf<StoryItem>()

    private lateinit var storyAdapter: StoryAdapter


    private var weatherApiKey: String = ""

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var weatherTextView: TextView





    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)

    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mood = arguments?.getString("mood") ?: "" //이걸 먼저 받을게욤
        val username = arguments?.getString("username") ?: ""

        KeyManager.init(requireContext())
        weatherApiKey = KeyManager.get("OWM_API_KEY")


        //MODD 기분기반노래추천 = $mood $usernme 님을 위한 노래 textveiw & recyclerView


        val moodRecyclerView = view.findViewById<RecyclerView>(R.id.rcmdSongRecyclerView)
        moodRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        val rcmdMent = view.findViewById<TextView>(R.id.rcmdMent)
        rcmdMent.text = "$mood $username 님을 위한 \n오늘의 노래추천 🎵"

        RecommendSong(mood, moodRecyclerView, "mood")

        //WEATHER 사용자 위치기반 날씨에 따른 노래추천
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext()) //위치클라이언트 초기화
        weatherTextView = view.findViewById(R.id.rcmdMentWeather)
        getLastLocation()



        // 1. 리사이클러뷰 설정: 스토리 목록 보여줌
        storyRecyclerView = view.findViewById(R.id.storyRecyclerView)
        storyRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

        storyAdapter = StoryAdapter(storyList) { storyItem ->
            val detailFragment = StoryDetailFragment().apply {
                arguments = Bundle().apply {
                    putParcelable("story", storyItem)
                }
            }
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, detailFragment)
                .addToBackStack(null)
                .commit()
        }

        storyRecyclerView.adapter = storyAdapter

        db.collection("stories")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener

                storyList.clear()
                for (doc in snapshot.documents) {
                    val item = doc.toObject(StoryItem::class.java)
                    if (item != null) storyList.add(item)
                }
                storyAdapter.notifyDataSetChanged()
            }

        // 2. + 스토리 추가 버튼 클릭 시, StoryAddFragment 다이얼로그 띄움
        val addStoryButton = view.findViewById<Button>(R.id.addStoryButton)
        addStoryButton.setOnClickListener {
            val dialog = StoryAddFragment()
            dialog.show(parentFragmentManager, "AddStoryDialog")
        }

        // 3. 파이어스토어에서 프로필 이미지 불러오기
        val profileImageView = view.findViewById<ImageView>(R.id.profileImageView)
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        //val db = FirebaseFirestore.getInstance()

        if (uid != null) {
            db.collection("users").document(uid).get().addOnSuccessListener { document ->
                val imageUrl = document.getString("profileImageUrl")
                if (!imageUrl.isNullOrEmpty()) {
                    Glide.with(view?.context ?: return@addOnSuccessListener)
                        .load(imageUrl)
                        .circleCrop()
                        .into(profileImageView)
                }
            }
        }

        // 4. 프로필 이미지 클릭 → 프로필 프래그먼트로 이동
        profileImageView.setOnClickListener {
            val bundle = Bundle().apply { //이때 걍 정보도 같이 보낼겜
                putString("username", username)
                putString("mood", mood)
            }
            val profileFragment = ProfileFragment()
            profileFragment.arguments = bundle

            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, profileFragment)
                .addToBackStack(null)
                .commit()
        }

        // 5. 하단 네비게이션바 보이도록 설정
        activity?.findViewById<View>(R.id.navigationBar)?.visibility = View.VISIBLE
    }

    fun RecommendSong(value : String, recyclerView: RecyclerView, field : String){
        Log.d("RecommendSong", "value: '$value', field: '$field'")
        Thread {
            db.collection("songs")
                .whereEqualTo(field, value)
                .get()
                .addOnSuccessListener { documents ->
                    Log.d("RecommendSong", "docs: ${documents.size()}")
                    lifecycleScope.launch(Dispatchers.IO) {
                        val searchKeywords = documents
                            .mapNotNull { doc ->
                                val title = doc.getString("title")
                                val artist = doc.getString("artist")
                                if (!title.isNullOrBlank() && !artist.isNullOrBlank()) {
                                    "$title $artist"
                                } else null
                            }
                            .shuffled()
                            .take(5)
                        val albumList = mutableListOf<Album>()
                        searchKeywords.forEach { keyword ->
                            val term = URLEncoder.encode(keyword, "UTF-8")
                            val url = "https://itunes.apple.com/search?media=musicTrack&entity=song&country=kr&term=$term"
                            val songMap = makeMap(url)
                            songMap.values.firstOrNull()?.let { albumList.add(it) }
                        }
                        withContext(Dispatchers.Main) {
                            val adapter = AlbumAdapter(
                                albumList,
                                onItemClick = { album ->
                                    MusicPlayerManager.play(album)
                                }
                            )
                            recyclerView.adapter = adapter
                        }
                    }
                }
        }.start()
    }

    fun makeMap(urls:String) : Map<String, Album>{
        //URL 객체로 만들기
        val url = URL(urls)

        //GET 요청하기
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "GET"
        //5초동안만 데이터 받기
        conn.connectTimeout = 5000
        conn.readTimeout = 5000

        val stream = conn.inputStream
        val result = stream.bufferedReader().use { it.readText() }
        stream.close()

        val jsonResponse = JSONObject(result)
        val jsonArray = jsonResponse.getJSONArray("results")

        val madeMap = mutableMapOf<String, Album>()
        for (i in 0 until jsonArray.length()) {
            val item = jsonArray.getJSONObject(i)
            var id = item.optString("collectionId")

            val title = item.optString("trackName")
            val artist = item.optString("artistName")
            val album = item.optString("collectionName")
            val albumArt = item.optString("artworkUrl100")
            val songUrl = item.optString("previewUrl")

            madeMap[id] = Album(title, artist, album, albumArt, songUrl)
        }
        return madeMap
    }

    private fun getLastLocation() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                100
            )
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location == null) {
                weatherTextView.text = "위치 정보를 받아올 수 없습니다 😱"
                return@addOnSuccessListener
            }
            fetchWeather(location.latitude, location.longitude)
        }
    }

    private fun fetchWeather(lat: Double, lon: Double) {
 Thread {
            try {
                val client = OkHttpClient()
                val url = "https://api.openweathermap.org/data/2.5/weather?lat=$lat&lon=$lon&appid=$weatherApiKey&lang=kr&units=metric"



                val request = Request.Builder().url(url).build()
                val response = client.newCall(request).execute()
                val responseData = response.body?.string()

                val locationName = getLocationName(lat,lon) //지역명얻기
                requireActivity().runOnUiThread {
                    if (!response.isSuccessful || responseData == null) {
                        weatherTextView.text = "날씨 API 호출 실패 (${response.code})"
                        return@runOnUiThread
                    }
                    try {
                        val json = JSONObject(responseData)
                        if (!json.has("weather")) {
                            weatherTextView.text = "날씨 정보를 불러올 수 없습니다."
                            return@runOnUiThread
                        }

                        val weatherArray = json.getJSONArray("weather")
                        val main = weatherArray.getJSONObject(0).getString("main")
                        val condition = WeatherUtil.classifyWeather(main)
                        weatherTextView.text = "현재 지역은 $locationName, 날씨는 $condition 입니다. \n이런 노래 어떠세요?"

                        val weatherRecyclerView = requireView().findViewById<RecyclerView>(R.id.rcmdSongWeatherRecyclerView)
                        weatherRecyclerView.layoutManager = LinearLayoutManager(requireContext())
                        RecommendSong(condition, weatherRecyclerView, "weather")

                    } catch (e: Exception) {
                        weatherTextView.text = "날씨 파싱 오류: ${e.message}"
                    }
                }
            } catch (e: Exception) {
                requireActivity().runOnUiThread {
                    weatherTextView.text = "네트워크 오류: ${e.message}"
                }
            }
        }.start()
    }

    // 권한 요청 결과 처리
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getLastLocation()
        } else {
            weatherTextView.text = "위치 권한이 필요합니다."
        }
    }

    private fun getLocationName(lat: Double, lon: Double): String {
        return try {
            val geocoder = Geocoder(requireContext(), Locale.KOREA)
            val addresses = geocoder.getFromLocation(lat, lon, 1)
            if (addresses != null && addresses.isNotEmpty()) {
                val address = addresses[0]
                // 시/구/동을 조합해서 지역명 만들기
                val adminArea = address.adminArea ?: ""    //시/도
                val subLocality = address.subLocality ?: "" // 구
                val thoroughfare = address.thoroughfare ?: "" // 동

                listOf(adminArea, subLocality, thoroughfare)
                    .filter { it.isNotBlank() }
                    .distinct()
                    .joinToString(" ")
            } else {
                "알 수 없음"
            }
        } catch (e: Exception) {
            Log.e("getLocationName", "Geocoding error: ${e.message}")
            "알 수 없음"
        }
    }
}


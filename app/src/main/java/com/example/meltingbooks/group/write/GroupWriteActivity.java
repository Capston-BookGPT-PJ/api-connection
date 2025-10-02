package com.example.meltingbooks.group.write;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.meltingbooks.feed.FeedActivity;
import com.example.meltingbooks.group.GroupFeedActivity;
import com.example.meltingbooks.network.ApiClient;
import com.example.meltingbooks.network.ApiResponse;
import com.example.meltingbooks.network.ApiService;
import com.example.meltingbooks.BuildConfig;
import com.example.meltingbooks.R;
import com.example.meltingbooks.network.group.CreatePostRequest;
import com.example.meltingbooks.network.group.GroupApi;
import com.example.meltingbooks.network.group.GroupFeedResponse;
import com.example.meltingbooks.network.group.PostSingleResponse;
import com.example.meltingbooks.network.group.UpdatePostRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

// Retrofit은 Retrofit 요청용
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// OkHttp는 OkHttp 요청용
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
//import okhttp3.ResponseBody;
//import okhttp3.Call;       // 여기서 okhttp3.Call
//import okhttp3.Callback;   // 여기서 okhttp3.Callback

public class GroupWriteActivity extends AppCompatActivity {

    private static final int REQUEST_PERMISSION_CODE = 1001;

    private EditText etInput, etInputTitle;
    private EditText postTitle,goalTitle, recommendTitle, groupNotiTitle, etPostContent, etGoalContent, etRecommendContent, etNotiContent;
    private ImageView micImageView, summarizingImageView;
    private ImageView imageView;
    private ImageButton btnRecord, btnAddFile, btnSummarize, btnUpload;

    private Spinner categorySpinner;
    private LinearLayout postLayout;
    private LinearLayout goalLayout;
    private LinearLayout recommendLayout;
    private LinearLayout groupNotiLayout;
    private TextView textHint;

    private SpeechRecognizer speechRecognizer;
    private Intent speechRecognizerIntent;

    private StorageReference storageReference;
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private OkHttpClient client;
    private Request request;
    private String apiKey;
    private Intent intent;

    private boolean isKeyboardVisible = false;

    private Uri selectedImageUri;

    //게시글 수정용 변수
    private boolean isEdit = false;
    private int postId = -1;
    private String token;
    //private GroupFeedResponse currentFeed; // 수정할 게시글 데이터
    // 게시글 수정용 변수
    private GroupFeedResponse.Post currentPost;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.group_write);

        //위 상단바색상
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.white));
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            View decor = getWindow().getDecorView();
            decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);  // 아이콘 색상 어둡게!
        }

        // API 키와 네트워크 클라이언트 초기화
        apiKey = BuildConfig.OPENAI_API_KEY;
        client = new OkHttpClient();



        // Firebase Storage 참조 초기화 (필요시 사용)
        storageReference = FirebaseStorage.getInstance().getReference("audio");

        // 뷰 초기화
        micImageView = findViewById(R.id.micON);
        summarizingImageView = findViewById(R.id.summarizing);
        imageView = findViewById(R.id.imageView);
        btnRecord = findViewById(R.id.btnRecord);
        btnAddFile = findViewById(R.id.btnAddFile);
        btnSummarize = findViewById(R.id.btnSummarize);
        btnUpload = findViewById(R.id.btnUpload);
        categorySpinner = findViewById(R.id.categorySpinner);

        postLayout = findViewById(R.id.postLayout);
        goalLayout = findViewById(R.id.goalLayout);
        recommendLayout = findViewById(R.id.recommendLayout);
        groupNotiLayout = findViewById(R.id.groupNotiLayout);

        textHint = findViewById(R.id.textHint);

        postTitle = findViewById(R.id.postTitle);
        etPostContent = findViewById(R.id.etPostContent);

        goalTitle = findViewById(R.id.goalTitle);
        etGoalContent = findViewById(R.id.etGoalContent);

        recommendTitle = findViewById(R.id.recommendTitle);
        etRecommendContent = findViewById(R.id.etRecommendContent);

        groupNotiTitle = findViewById(R.id.groupNotiTitle);
        etNotiContent = findViewById(R.id.etNotiContent);



        //게시글 수정 모드
        Intent feedIntent = getIntent();
        isEdit = feedIntent.getBooleanExtra("isEdit", false);
        postId = feedIntent.getIntExtra("postId", -1);

        if (isEdit && postId != -1) {
            loadReviewData(postId); // 서버에서 기존 리뷰 데이터 가져오기
        }


        // 권한 체크
        checkPermissions();

        // 이미지 선택기 설정
        setupImagePicker();

        // 스피너 설정 (카테고리별 레이아웃 토글)
        setupSpinner();

        // 요약하기 버튼 클릭 리스너
        btnSummarize.setOnClickListener(v -> {
            //요약하기 버튼 숨기기
            btnSummarize.setVisibility(View.GONE);
            // 요약 중 이미지를 보이게
            summarizingImageView.setVisibility(View.VISIBLE);

            // 텍스트를 요약하는 로직
            String inputText = etInput.getText().toString();
            callAPI(inputText);  // ChatGPT API 호출
        });


    }
    private void updateEtInput(String category) {
        switch (category) {
            case "감상문 공유":
                etInput = etPostContent;
                etInputTitle = postTitle;
                break;
            case "독서 목표 공유":
                etInput = etGoalContent;
                etInputTitle = goalTitle;
                break;
            case "책 추천":
                etInput = etRecommendContent;
                etInputTitle = recommendTitle;
                break;
            case "공지 사항":
                etInput = etNotiContent;
                etInputTitle = groupNotiTitle;
                break;
            default:
                etInput = null;
                etInputTitle = null;
                break;
        }
    }

    private boolean isSpinnerInitialized = false;

    private void setupSpinner() {

        // 문자열 배열 리소스를 가져와서 Spinner에 연결
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.groupPost_categories,
                R.layout.spinner_item  // 커스텀 일반 아이템 레이아웃
        );

        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);  // 커스텀 드롭다운 아이템 레이아웃

        categorySpinner.setAdapter(adapter);


        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedCategory = parent.getItemAtPosition(position).toString();
                /*
                // 선택된 카테고리에 맞게 레이아웃 보여주기
                switch (position) {
                    case 0:
                        postLayout.setVisibility(View.GONE);
                        goalLayout.setVisibility(View.GONE);
                        recommendLayout.setVisibility(View.GONE);
                        groupNotiLayout.setVisibility(View.GONE);
                        textHint.setVisibility(View.VISIBLE);
                        break;
                    case 1:
                        postLayout.setVisibility(View.VISIBLE);
                        goalLayout.setVisibility(View.GONE);
                        recommendLayout.setVisibility(View.GONE);
                        groupNotiLayout.setVisibility(View.GONE);
                        textHint.setVisibility(View.GONE);
                        break;
                    case 2:
                        postLayout.setVisibility(View.GONE);
                        goalLayout.setVisibility(View.VISIBLE);
                        recommendLayout.setVisibility(View.GONE);
                        groupNotiLayout.setVisibility(View.GONE);
                        textHint.setVisibility(View.GONE);
                        break;
                    case 3:
                        postLayout.setVisibility(View.GONE);
                        goalLayout.setVisibility(View.GONE);
                        recommendLayout.setVisibility(View.VISIBLE);
                        groupNotiLayout.setVisibility(View.GONE);
                        textHint.setVisibility(View.GONE);
                        break;
                    case 4:
                        postLayout.setVisibility(View.GONE);
                        goalLayout.setVisibility(View.GONE);
                        recommendLayout.setVisibility(View.GONE);
                        groupNotiLayout.setVisibility(View.VISIBLE);
                        textHint.setVisibility(View.GONE);
                        break;
                }*/
                // 선택된 카테고리에 따라 레이아웃 보이기
                postLayout.setVisibility(position == 1 ? View.VISIBLE : View.GONE);
                goalLayout.setVisibility(position == 2 ? View.VISIBLE : View.GONE);
                recommendLayout.setVisibility(position == 3 ? View.VISIBLE : View.GONE);
                groupNotiLayout.setVisibility(position == 4 ? View.VISIBLE : View.GONE);
                textHint.setVisibility(position == 0 ? View.VISIBLE : View.GONE);


                updateEtInput(selectedCategory);
                // EditText 클릭 및 텍스트 감지 리스너 설정
                setupEditTextListeners();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                postLayout.setVisibility(View.GONE);
                goalLayout.setVisibility(View.GONE);
                recommendLayout.setVisibility(View.GONE);
                groupNotiLayout.setVisibility(View.GONE);
            }

        });

    }

    private void setupEditTextListeners() {
        if (etInput == null) return;

        etInput.setOnClickListener(v -> {
            if (isKeyboardVisible) {
                hideKeyboard();
            } else {
                etInput.requestFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(etInput, InputMethodManager.SHOW_IMPLICIT);
            }
            isKeyboardVisible = !isKeyboardVisible;
        });

        etInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() >= 5) {
                    btnSummarize.setVisibility(View.VISIBLE);
                } else {
                    btnSummarize.setVisibility(View.GONE);
                }
            }

            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void setupImagePicker() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        imageView.setImageURI(selectedImageUri);
                    }
                });

        btnAddFile.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            imagePickerLauncher.launch(intent);
        });

        /// RecognizerIntent 생성
        intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        //intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,getPackageName()); // 여분의 키
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,"ko-KR"); // 언어 설정
        //intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault().toString()); // 기기의 기본 언어로 설정


        // btnRecord 클릭 리스너에서 micON 이미지 뷰를 표시
        btnRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnSummarize.setVisibility(View.GONE);
                initSpeechRecognizer();
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(GroupWriteActivity.this); // 새 SpeechRecognizer 를 만드는 팩토리 메서드
                speechRecognizer.setRecognitionListener(listener); // 리스너 설정
                // micON 뷰를 찾고 visibility를 VISIBLE로 변경
                if (micImageView != null) {
                    micImageView.setVisibility(View.VISIBLE);  // micON 이미지 뷰를 보이도록 설정
                }
                etInput.setHint("");  // hint를 빈 문자열로 설정
                speechRecognizer.startListening(intent); // 듣기 시작
            }
        });

        checkPermissions();

        btnUpload.setOnClickListener(v -> {
            String title = etInputTitle.getText().toString().trim();
            String content = etInput.getText().toString().trim();

            if (title.isEmpty()) {
                Toast.makeText(this, "제목을 입력하세요.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (content.isEmpty()) {
                Toast.makeText(this, "내용을 입력하세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            SharedPreferences prefs = getSharedPreferences("auth", MODE_PRIVATE);
            String token = prefs.getString("jwt", null);
            int userId = prefs.getInt("userId", -1);

            if (token == null || userId == -1) {
                Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
                return;
            }

            int groupId = getIntent().getIntExtra("groupId", -1);
            Log.d("UploadDebug", "받은 groupId = " + groupId);

            // 게시글 타입 결정
            String postType = "";
            switch (categorySpinner.getSelectedItemPosition()) {
                case 1: postType = "POST"; break;
                case 2: postType = "GOAL_SHARE"; break;
                case 3: postType = "RECOMMENDED"; break;
                case 4: postType = "NOTICE"; break;
                default: postType = "POST"; break;
            }
            GroupApi groupApi = ApiClient.getClient(token).create(GroupApi.class);

            Call<ApiResponse<PostSingleResponse>> call;

            if (isEdit) {
                // 수정
                UpdatePostRequest updateRequest = new UpdatePostRequest(postType, title, content,
                        selectedImageUri != null ? selectedImageUri.toString() : null);
                call = groupApi.updatePost("Bearer " + token, groupId, postId, updateRequest);
            } else {
                // 작성
                CreatePostRequest createRequest = new CreatePostRequest(postType, title, content,
                        selectedImageUri != null ? selectedImageUri.toString() : null);
                call = groupApi.createPost("Bearer " + token, groupId, createRequest);
            }

            call.enqueue(new retrofit2.Callback<ApiResponse<PostSingleResponse>>() {
                @Override
                public void onResponse(Call<ApiResponse<PostSingleResponse>> call, Response<ApiResponse<PostSingleResponse>> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        PostSingleResponse postData = response.body().getData();

                        // PostSingleResponse → GroupFeedResponse.Post 변환
                        GroupFeedResponse.Post post = new GroupFeedResponse.Post();
                        post.setPostId(postData.getId());
                        post.setType(postData.getPostType());
                        post.setTitle(postData.getTitle());
                        post.setContent(postData.getContent());
                        post.setImageUrl(postData.getImageUrl());
                        post.setAuthorId(postData.getUserId());
                        post.setCreatedAt(postData.getCreatedAt());


                        if (selectedImageUri != null) {
                            ApiService apiService = ApiClient.getClient(token).create(ApiService.class);
                            // 기존 uploadReviewImage 호출
                            uploadReviewImage(apiService, token, post.getPostId(), selectedImageUri);
                            // 이미지 업로드 후 fetchSinglePost는 uploadReviewImage 안의 finish() 이후에는 호출할 수 없음
                        } else {
                            // 이미지 없으면 바로 단일 게시글 가져오기
                            fetchSinglePost(groupId, post.getPostId(), token);
                        }

                    } else {
                        Toast.makeText(GroupWriteActivity.this, isEdit ? "게시글 수정 실패" : "게시글 작성 실패", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse<PostSingleResponse>> call, Throwable t) {
                    Toast.makeText(GroupWriteActivity.this, "네트워크 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

    }

    /// 전체 피드에서 postId 기준으로 찾아 화면 이동
    // ----------------------
// 단일 게시글 fetch
    private void fetchSinglePost(int groupId, int postId, String token) {
        GroupApi groupApi = ApiClient.getClient(token).create(GroupApi.class);
        int page = 0, size = 100; // 충분히 큰 수로 전체 게시글 가져오기

        groupApi.getGroupFeed(groupId, page, size)
                .enqueue(new Callback<GroupFeedResponse>() {
                    @Override
                    public void onResponse(Call<GroupFeedResponse> call, Response<GroupFeedResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            GroupFeedResponse.GroupFeedData feed = response.body().getData();
                            GroupFeedResponse.Post targetPost = null;

                    List<GroupFeedResponse.Post> allPosts = new ArrayList<>();
                    if (feed.getNotices() != null) allPosts.addAll(feed.getNotices());
                    if (feed.getRecommendedBooks() != null) allPosts.addAll(feed.getRecommendedBooks());
                    if (feed.getGoals() != null) allPosts.addAll(feed.getGoals());
                    if (feed.getPosts() != null && feed.getPosts().getContent() != null)
                        allPosts.addAll(feed.getPosts().getContent());


                    targetPost = null;
                    for (GroupFeedResponse.Post p : allPosts) {
                        if (p.getPostId() == postId) {
                            targetPost = p;
                            break;
                        }
                    }

                    if (targetPost != null) {
                        finishPostFlow(targetPost);
                    } else {
                        Toast.makeText(GroupWriteActivity.this, "게시글을 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(GroupWriteActivity.this, "게시글 가져오기 실패", Toast.LENGTH_SHORT).show();
                }
            }

                    @Override
                    public void onFailure(Call<GroupFeedResponse> call, Throwable t) {
                        Toast.makeText(GroupWriteActivity.this, "네트워크 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
        });
    }


    // 작성/수정 완료 후 화면 이동
    private void finishPostFlow(GroupFeedResponse.Post post) {
        if (isEdit) {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("updatedFeed", post); // Post는 Serializable
            setResult(RESULT_OK, resultIntent);
            finish();
        } else {
            Intent intent = new Intent(GroupWriteActivity.this, GroupFeedActivity.class);
            intent.putExtra("refreshFeed", true);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }
    }

    private RecognitionListener listener = new RecognitionListener() {
        @Override
        public void onReadyForSpeech(Bundle params) {
            showSpeechRecognitionUI();
            // 말하기 시작할 준비가되면 호출
        }

        @Override
        public void onBeginningOfSpeech() {
            // 말하기 시작했을 때 호출
        }

        @Override
        public void onRmsChanged(float rmsdB) {
            // 입력받는 소리의 크기를 알려줌
        }

        @Override
        public void onBufferReceived(byte[] buffer) {
            // 말을 시작하고 인식이 된 단어를 buffer에 담음
        }

        @Override
        public void onEndOfSpeech() {
            // 말하기를 중지하면 호출
        }

        @Override
        public void onError(int error) {
            Log.e("SpeechRecognizer", "오류 코드: " + error); // 오류 코드 확인
            String message;

            switch (error) {
                case SpeechRecognizer.ERROR_AUDIO:
                    message = "오디오 녹음 오류";
                    break;
                case SpeechRecognizer.ERROR_CLIENT:
                    message = "클라이언트 오류";
                    break;
                case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                    message = "권한 없음";
                    break;
                case SpeechRecognizer.ERROR_NETWORK:
                    message = "네트워크 오류";
                    break;
                case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                    message = "네트워크 타임아웃";
                    break;
                case SpeechRecognizer.ERROR_NO_MATCH:
                    message = "음성을 인식하지 못했습니다. 다시 시도해 주세요.";
                    break;
                case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                    message = "음성 인식기가 사용 중입니다. 잠시 후 다시 시도해 주세요.";
                    break;
                case SpeechRecognizer.ERROR_SERVER:
                    message = "서버 오류 발생";
                    break;
                case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                    message = "입력 시간이 초과되었습니다. 다시 시도해 주세요.";
                    break;
                default:
                    message = "알 수 없는 오류 발생 (코드: " + error + ")";
                    break;
            }

            Log.e("SpeechRecognizer", "에러 메시지: " + message); // 에러 메시지 출력 확인
            Toast.makeText(getApplicationContext(), "에러 발생 : " + message, Toast.LENGTH_SHORT).show();
            hideSpeechRecognitionUI();
            etInput.setHint("독서 후 느낌을 공유해 보세요!");  // 기본 hint로 설정
        }

        @Override
        public void onResults(Bundle results) {
            ArrayList<String> matches =
                    results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            if (matches != null && !matches.isEmpty()) {
                etInput.setText(matches.get(0)); // 인식된 첫 번째 텍스트를 etInput에 설정
                etInput.setSelection(etInput.getText().length()); // 🔥 커서를 맨 뒤로 이동
                // 음성 인식이 완료되면 요약하기 버튼을 보이게 설정
                btnSummarize.setVisibility(View.VISIBLE);
            }
            hideSpeechRecognitionUI();  // 음성 인식 후 UI 숨김
        }

        @Override
        public void onPartialResults(Bundle partialResults) {
            // 부분 인식 결과를 사용할 수 있을 때 호출
        }

        @Override
        public void onEvent(int eventType, Bundle params) {
            // 향후 이벤트를 추가하기 위해 예약
        }
    };

    //음성인식 초기화 한 번만
    private void initSpeechRecognizer() {
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
            speechRecognizer = null;
        }

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechRecognizer.setRecognitionListener(listener);
    }



    private void startSpeechRecognition() {
        micImageView.setVisibility(View.VISIBLE);
        etInput.setHint("");
        speechRecognizer.startListening(speechRecognizerIntent);
    }

    // ChatGPT API 호출
    private void callAPI(String question) {
        // 요약 중 이미지를 보이게
        summarizingImageView.setVisibility(View.VISIBLE);

        JSONObject object = new JSONObject();
        try {
            object.put("model", "gpt-3.5-turbo");
            JSONArray messagesArray = new JSONArray();

            // 시스템 역할 추가
            JSONObject systemMessage = new JSONObject();
            systemMessage.put("role", "system");
            systemMessage.put("content", "You are a helpful assistant that summarizes text.");
            messagesArray.put(systemMessage);

            // 사용자 입력 추가
            JSONObject messageObj = new JSONObject();
            messageObj.put("role", "user");
            messageObj.put("content", "다음 내용을 사용자의 감상을 중심으로 요약해줘:\\n" + question);  // 명확한 요청 추가
            messagesArray.put(messageObj);
            object.put("messages", messagesArray);
            object.put("temperature", 0.7); // 다양성을 조절하는 옵션
        } catch (JSONException e) {
            e.printStackTrace();
        }


        RequestBody body = RequestBody.create(object.toString(), MediaType.get("application/json; charset=utf-8"));
        request = new Request.Builder()
                .url("https://api.openai.com/v1/chat/completions")
                .header("Authorization", "Bearer " + apiKey)
                .post(body)
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() { // 반드시 okhttp3.Callback 사용
            @Override
            public void onFailure(@NonNull okhttp3.Call call, @NonNull IOException e) {
                // 요청 실패 처리
                runOnUiThread(() -> {
                    summarizingImageView.setVisibility(View.GONE);  // 요약 중 이미지 숨기기
                    Toast.makeText(GroupWriteActivity.this, "요약 실패", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(@NonNull okhttp3.Call call, @NonNull okhttp3.Response response) throws IOException {
                String responseBody = response.body().string();
                Log.d("API_RESPONSE", responseBody);  // 응답 로깅


                if (response.isSuccessful()) {
                    try {
                        JSONObject jsonResponse = new JSONObject(responseBody);
                        JSONArray choices = jsonResponse.getJSONArray("choices");
                        String summarizedText = choices.getJSONObject(0).getJSONObject("message").getString("content");

                        runOnUiThread(() -> {
                            etInput.setText(summarizedText);
                            summarizingImageView.setVisibility(View.GONE);
                            btnSummarize.setVisibility(View.VISIBLE);
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    runOnUiThread(() -> {
                        summarizingImageView.setVisibility(View.GONE);
                        Toast.makeText(GroupWriteActivity.this, "API 호출 오류: " + responseBody, Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_PERMISSION_CODE);
        }
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(etInput.getWindowToken(), 0);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "권한 허용 완료", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "권한을 허용해야 음성 인식이 가능합니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void showSpeechRecognitionUI() {
        // 음성 인식 중 이미지 보이기
        if (micImageView != null) {
            micImageView.setVisibility(View.VISIBLE);  // micON 이미지를 보이도록 설정
        }
    }

    // 음성 인식 종료 후 UI 숨김
    private void hideSpeechRecognitionUI() {
        // micON을 찾아서 숨김
        if (micImageView != null) {
            micImageView.setVisibility(View.GONE);  // micON 이미지 뷰를 숨김
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }
    }




    //그룹용 수정 필요
    private void uploadReviewImage(ApiService apiService, String token, int reviewId, Uri imageUri) {
        try {
            File file = new File(getCacheDir(), "temp_review.jpg");
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            OutputStream outputStream = new FileOutputStream(file);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
            outputStream.close();
            inputStream.close();

            RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), file);
            MultipartBody.Part body = MultipartBody.Part.createFormData("files", file.getName(), requestFile);

            Call<ApiResponse<List<String>>> call = apiService.uploadReviewImage("Bearer " + token, reviewId, body);
            call.enqueue(new Callback<ApiResponse<List<String>>>() {
                @Override
                public void onResponse(Call<ApiResponse<List<String>>> call, Response<ApiResponse<List<String>>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        Toast.makeText(GroupWriteActivity.this, "리뷰 & 이미지 업로드 완료!", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(GroupWriteActivity.this, "이미지 업로드 실패", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse<List<String>>> call, Throwable t) {
                    Toast.makeText(GroupWriteActivity.this, "네트워크 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // 수정 시 기존 게시글 가져오기
    private void loadReviewData(int postId) {
        SharedPreferences prefs = getSharedPreferences("auth", MODE_PRIVATE);
        String token = prefs.getString("jwt", null);

        if (token == null) {
            Log.e("GroupWrite", "토큰이 없습니다.");
            return;
        }

        int groupId = getIntent().getIntExtra("groupId", -1);
        if (groupId == -1) {
            Log.e("GroupWrite", "잘못된 그룹 정보입니다.");

            return;
        }

        GroupApi groupApi = ApiClient.getClient(token).create(GroupApi.class);

        // 전체 피드 가져오기 (page=0, size 충분히 크게)
        groupApi.getGroupFeed(groupId, 0, 10)
                .enqueue(new Callback<GroupFeedResponse>() {
                    @Override
                    public void onResponse(Call<GroupFeedResponse> call, Response<GroupFeedResponse> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            GroupFeedResponse.GroupFeedData data = response.body().getData();
                            if (data != null && data.getPosts() != null && data.getPosts().getContent() != null) {
                                List<GroupFeedResponse.Post> posts = data.getPosts().getContent();
                                for (GroupFeedResponse.Post post : posts) {
                                    if (post.getPostId() == postId) {
                                        bindDataToViews(post);
                                        return;
                                    }
                                }
                            }
                            Toast.makeText(GroupWriteActivity.this, "게시글을 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(GroupWriteActivity.this, "그룹 피드 로드 실패", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<GroupFeedResponse> call, Throwable t) {
                        Toast.makeText(GroupWriteActivity.this, "네트워크 오류", Toast.LENGTH_SHORT).show();
                    }
                });
    }



    private void bindDataToViews(GroupFeedResponse.Post post) {
        currentPost= post;  // 수정 모드용 저장

        // 제목/내용
        if (post.getTitle() != null) {
            postTitle.setText(post.getTitle());
            goalTitle.setText(post.getTitle());
            recommendTitle.setText(post.getTitle());
            groupNotiTitle.setText(post.getTitle());
        }
        if (post.getContent() != null) {
            etPostContent.setText(post.getContent());
            etGoalContent.setText(post.getContent());
            etRecommendContent.setText(post.getContent());
            etNotiContent.setText(post.getContent());
        }

        // Spinner 초기 선택
        switch (post.getType()) {
            case "POST": categorySpinner.setSelection(1); break;
            case "GOAL_SHARE": categorySpinner.setSelection(2); break;
            case "RECOMMENDED": categorySpinner.setSelection(3); break;
            case "NOTICE": categorySpinner.setSelection(4); break;
            default: categorySpinner.setSelection(0); break;
        }

    }



}

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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.meltingbooks.feed.FeedWriteActivity;
import com.example.meltingbooks.network.ApiClient;
import com.example.meltingbooks.network.ApiResponse;
import com.example.meltingbooks.network.ApiService;
import com.example.meltingbooks.network.book.Book;
import com.example.meltingbooks.network.book.BookController;
import com.example.meltingbooks.BuildConfig;
import com.example.meltingbooks.R;
import com.example.meltingbooks.network.feed.ReviewRequest;
import com.example.meltingbooks.network.feed.ReviewResponse;
import com.example.meltingbooks.search.SearchBookAdapter;
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

    private EditText etInput;
    private EditText etInputTitle, etDiscussionContent, etReviewContent, etGoalContent, etGroupPostContent, etNotiContent;
    private ImageView micImageView, summarizingImageView;
    private ImageView imageView;
    private ImageButton btnRecord, btnAddFile, btnSummarize, btnUpload;
    private Spinner categorySpinner;

    private LinearLayout discussionLayout;
    private LinearLayout reviewLayout;
    private LinearLayout goalLayout;
    private LinearLayout groupPostLayout;
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

    //책 정보
    private LinearLayout bookSearchInfoContainer;
    private EditText etBookTitle;
    private ImageView searchBook;

    // 🔹 책 관련 변수
    private SearchBookAdapter bookAdapter;
    private List<Book> filteredBookList;
    private BookController bookController;
    private RecyclerView rvSearchResults; //책 검색
    private LinearLayout bookInfoSelected; //선택한 책 표시
    private ImageView bookThumbnail; //책 이미지
    private TextView bookInfoTitle, bookInfoAuthor, bookInfoPublisher, bookInfoCategory; //제목 저자 출판사 카테고리


    private Uri selectedImageUri;

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
        discussionLayout = findViewById(R.id.discussionLayout);
        reviewLayout = findViewById(R.id.reviewLayout);
        goalLayout = findViewById(R.id.goalLayout);
        groupPostLayout = findViewById(R.id.groupPostLayout);
        groupNotiLayout = findViewById(R.id.groupNotiLayout);
        textHint = findViewById(R.id.textHint);
        etDiscussionContent = findViewById(R.id.etDiscussionContent);
        etReviewContent = findViewById(R.id.etReviewContent);
        etGoalContent = findViewById(R.id.etGoalContent);
        etGroupPostContent = findViewById(R.id.etGroupPostContent);
        etNotiContent = findViewById(R.id.etNotiContent);



        //토큰 받아오기
        SharedPreferences prefs = getSharedPreferences("auth", MODE_PRIVATE);
        String token = prefs.getString("jwt", "");

        // 책 정보 관련 뷰 초기화
        bookSearchInfoContainer = findViewById(R.id.bookSearchInfoContainer);
        etBookTitle = bookSearchInfoContainer.findViewById(R.id.etBookTitle);
        searchBook = bookSearchInfoContainer.findViewById(R.id.searchBook);
        rvSearchResults = bookSearchInfoContainer.findViewById(R.id.rvSearchResults);

        bookInfoSelected = bookSearchInfoContainer.findViewById(R.id.bookInfoSelected);
        bookInfoSelected.setVisibility(View.GONE); // 초기 숨김

        // bookInfoSelected 안의 뷰 초기화 (반드시 bookInfoSelected 이후)
        bookInfoTitle = bookInfoSelected.findViewById(R.id.bookInfoTitle);
        bookInfoAuthor = bookInfoSelected.findViewById(R.id.bookInfoAuthor);
        bookInfoPublisher = bookInfoSelected.findViewById(R.id.bookInfoPublisher);
        bookThumbnail = bookInfoSelected.findViewById(R.id.bookThumbnail);
        bookInfoCategory = bookInfoSelected.findViewById(R.id.bookInfoCategory);

        // RecyclerView 초기화
        bookController = new BookController(this); // context 전달
        filteredBookList = new ArrayList<>();
        bookAdapter = new SearchBookAdapter(this, filteredBookList);
        rvSearchResults.setLayoutManager(new LinearLayoutManager(this));
        rvSearchResults.setAdapter(bookAdapter);



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
            case "토론": etInput = etDiscussionContent; break;
            case "감상문": etInput = etReviewContent; break;
            case "목표": etInput = etGoalContent; break;
            case "자유주제": etInput = etGroupPostContent; break;
            case "공지": etInput = etNotiContent; break;
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
                if (!isSpinnerInitialized) {
                    isSpinnerInitialized = true;
                    return;  // 초기 호출 무시, UI는 모두 gone 상태 유지
                }
                // 선택된 카테고리에 맞게 레이아웃 보여주기
                switch (position) {
                    case 0:
                        discussionLayout.setVisibility(View.GONE);
                        reviewLayout.setVisibility(View.GONE);
                        goalLayout.setVisibility(View.GONE);
                        groupPostLayout.setVisibility(View.GONE);
                        groupNotiLayout.setVisibility(View.GONE);
                        textHint.setVisibility(View.VISIBLE);
                        break;
                    case 1:
                        discussionLayout.setVisibility(View.VISIBLE);
                        reviewLayout.setVisibility(View.GONE);
                        goalLayout.setVisibility(View.GONE);
                        groupPostLayout.setVisibility(View.GONE);
                        groupNotiLayout.setVisibility(View.GONE);
                        textHint.setVisibility(View.GONE);

                        // 선택 정보 영역 숨기기
                        bookInfoSelected.setVisibility(View.GONE);


                        searchBook.setOnClickListener(v -> {
                            String query = etBookTitle.getText().toString().trim();
                            rvSearchResults.setVisibility(View.VISIBLE); // 검색 결과 보여주기

                            if (!query.isEmpty()) {
                                // 서버에서 검색
                                bookController.searchBooks(query, new retrofit2.Callback<List<Book>>() {
                                    @Override
                                    public void onResponse(retrofit2.Call<List<Book>> call, retrofit2.Response<List<Book>> response) {
                                        Log.d("GroupWriteActivity", "서버 응답 성공: " + response.code());

                                        if (response.isSuccessful() && response.body() != null) {
                                            Log.d("GroupWriteActivity", "받은 책 개수: " + response.body().size());
                                            filteredBookList.clear();
                                            filteredBookList.addAll(response.body());
                                            bookAdapter.notifyDataSetChanged();
                                        } else {
                                            Log.d("GroupWriteActivity", "응답은 왔지만 body 없음");
                                            filteredBookList.clear();
                                            bookAdapter.notifyDataSetChanged();
                                        }
                                    }

                                    @Override
                                    public void onFailure(retrofit2.Call<List<Book>> call, Throwable t) {
                                        t.printStackTrace();
                                        Toast.makeText(GroupWriteActivity.this, "서버 연결 실패", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            } else {
                                // 검색어가 비었으면 전체 목록 표시
                                bookController.fetchBooks(new retrofit2.Callback<List<Book>>() {
                                    @Override
                                    public void onResponse(retrofit2.Call<List<Book>> call, retrofit2.Response<List<Book>> response) {
                                        if (response.isSuccessful() && response.body() != null) {
                                            filteredBookList.clear();
                                            filteredBookList.addAll(response.body());
                                            bookAdapter.notifyDataSetChanged();
                                        }
                                    }

                                    @Override
                                    public void onFailure(retrofit2.Call<List<Book>> call, Throwable t) {
                                        t.printStackTrace();
                                    }
                                });
                            }
                        });

                        // RecyclerView 아이템 클릭 시 선택한 책만 표시
                        bookAdapter.setOnItemClickListener(book -> {
                            // 클릭한 책 정보로 뷰 업데이트
                            bookInfoTitle.setText(book.getTitle());
                            bookInfoAuthor.setText(book.getAuthor());
                            bookInfoPublisher.setText(book.getPublisher());
                            bookInfoCategory.setText(book.getCategoryName());
                            Glide.with(GroupWriteActivity.this).load(book.getCover()).into(bookThumbnail);

                            // 검색 결과만 숨기고
                            rvSearchResults.setVisibility(View.GONE);

                            // 선택 정보 영역 보이기
                            bookInfoSelected.setVisibility(View.VISIBLE);

                        });

                        break;
                    case 2:
                        discussionLayout.setVisibility(View.GONE);
                        reviewLayout.setVisibility(View.VISIBLE);
                        goalLayout.setVisibility(View.GONE);
                        groupPostLayout.setVisibility(View.GONE);
                        groupNotiLayout.setVisibility(View.GONE);
                        textHint.setVisibility(View.GONE);

                        // 선택 정보 영역 숨기기
                        bookInfoSelected.setVisibility(View.GONE);


                        searchBook.setOnClickListener(v -> {
                            String query = etBookTitle.getText().toString().trim();
                            rvSearchResults.setVisibility(View.VISIBLE); // 검색 결과 보여주기

                            if (!query.isEmpty()) {
                                // 서버에서 검색
                                bookController.searchBooks(query, new retrofit2.Callback<List<Book>>() {
                                    @Override
                                    public void onResponse(retrofit2.Call<List<Book>> call, retrofit2.Response<List<Book>> response) {
                                        Log.d("GroupWriteActivity", "서버 응답 성공: " + response.code());

                                        if (response.isSuccessful() && response.body() != null) {
                                            Log.d("GroupWriteActivity", "받은 책 개수: " + response.body().size());
                                            filteredBookList.clear();
                                            filteredBookList.addAll(response.body());
                                            bookAdapter.notifyDataSetChanged();
                                        } else {
                                            Log.d("GroupWriteActivity", "응답은 왔지만 body 없음");
                                            filteredBookList.clear();
                                            bookAdapter.notifyDataSetChanged();
                                        }
                                    }

                                    @Override
                                    public void onFailure(retrofit2.Call<List<Book>> call, Throwable t) {
                                        t.printStackTrace();
                                        Toast.makeText(GroupWriteActivity.this, "서버 연결 실패", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            } else {
                                // 검색어가 비었으면 전체 목록 표시
                                bookController.fetchBooks(new retrofit2.Callback<List<Book>>() {
                                    @Override
                                    public void onResponse(retrofit2.Call<List<Book>> call, retrofit2.Response<List<Book>> response) {
                                        if (response.isSuccessful() && response.body() != null) {
                                            filteredBookList.clear();
                                            filteredBookList.addAll(response.body());
                                            bookAdapter.notifyDataSetChanged();
                                        }
                                    }

                                    @Override
                                    public void onFailure(retrofit2.Call<List<Book>> call, Throwable t) {
                                        t.printStackTrace();
                                    }
                                });
                            }
                        });

                        // RecyclerView 아이템 클릭 시 선택한 책만 표시
                        bookAdapter.setOnItemClickListener(book -> {
                            // 클릭한 책 정보로 뷰 업데이트
                            bookInfoTitle.setText(book.getTitle());
                            bookInfoAuthor.setText(book.getAuthor());
                            bookInfoPublisher.setText(book.getPublisher());
                            bookInfoCategory.setText(book.getCategoryName());
                            Glide.with(GroupWriteActivity.this).load(book.getCover()).into(bookThumbnail);

                            // 검색 결과만 숨기고
                            rvSearchResults.setVisibility(View.GONE);

                            // 선택 정보 영역 보이기
                            bookInfoSelected.setVisibility(View.VISIBLE);

                        });
                        break;
                    case 3:
                        discussionLayout.setVisibility(View.GONE);
                        reviewLayout.setVisibility(View.GONE);
                        goalLayout.setVisibility(View.VISIBLE);
                        groupPostLayout.setVisibility(View.GONE);
                        groupNotiLayout.setVisibility(View.GONE);
                        textHint.setVisibility(View.GONE);
                        break;
                    case 4:
                        discussionLayout.setVisibility(View.GONE);
                        reviewLayout.setVisibility(View.GONE);
                        goalLayout.setVisibility(View.GONE);
                        groupPostLayout.setVisibility(View.VISIBLE);
                        groupNotiLayout.setVisibility(View.GONE);
                        textHint.setVisibility(View.GONE);
                        break;
                    case 5:
                        discussionLayout.setVisibility(View.GONE);
                        reviewLayout.setVisibility(View.GONE);
                        goalLayout.setVisibility(View.GONE);
                        groupPostLayout.setVisibility(View.GONE);
                        groupNotiLayout.setVisibility(View.VISIBLE);
                        textHint.setVisibility(View.GONE);
                        break;
                }
                updateEtInput(selectedCategory);
                // EditText 클릭 및 텍스트 감지 리스너 설정
                setupEditTextListeners();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                discussionLayout.setVisibility(View.GONE);
                reviewLayout.setVisibility(View.GONE);
                goalLayout.setVisibility(View.GONE);
                groupPostLayout.setVisibility(View.GONE);
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
            SharedPreferences prefs = getSharedPreferences("auth", MODE_PRIVATE);
            String token = prefs.getString("jwt", null);
            int userId = prefs.getInt("userId", -1);

            if (token == null || userId == -1) {
                Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
                return;
            }

            String content = etInput.getText().toString().trim();
            if (content.isEmpty()) {
                Toast.makeText(this, "내용을 입력하세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            ReviewRequest request = new ReviewRequest(1, content, 5); // bookId=1, rating=5 예시
            ApiService apiService = ApiClient.getClient(token).create(ApiService.class);

            Call<ApiResponse<ReviewResponse>> call = apiService.createReview("Bearer " + token, userId, request);
            call.enqueue(new Callback<ApiResponse<ReviewResponse>>() {
                @Override
                public void onResponse(Call<ApiResponse<ReviewResponse>> call, Response<ApiResponse<ReviewResponse>> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        ReviewResponse review = response.body().getData();
                        int reviewId = review.getReviewId();

                        if (selectedImageUri != null) {
                            uploadReviewImage(apiService, token, reviewId, selectedImageUri);
                        } else {
                            Toast.makeText(GroupWriteActivity.this, "리뷰 작성 완료!", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    } else {
                        Toast.makeText(GroupWriteActivity.this, "리뷰 작성 실패", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse<ReviewResponse>> call, Throwable t) {
                    Toast.makeText(GroupWriteActivity.this, "네트워크 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });


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

}

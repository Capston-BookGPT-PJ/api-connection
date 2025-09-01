package com.example.meltingbooks.group;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
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

import com.example.meltingbooks.BuildConfig;
import com.example.meltingbooks.R;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class GroupWriteActivity extends AppCompatActivity {

    private static final int REQUEST_PERMISSION_CODE = 1001;

    private EditText etInput;
    private EditText etInputTitle, etDiscussionContent, etReviewContent, etGoalContent, etGroupPostContent, etNotiContent;
    private ImageView micImageView, summarizingImageView;
    private ImageView imageView;
    private ImageButton btnRecord, btnAddFile, btnSummarize;
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

                        break;
                    case 2:
                        discussionLayout.setVisibility(View.GONE);
                        reviewLayout.setVisibility(View.VISIBLE);
                        goalLayout.setVisibility(View.GONE);
                        groupPostLayout.setVisibility(View.GONE);
                        groupNotiLayout.setVisibility(View.GONE);
                        textHint.setVisibility(View.GONE);
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
                        Uri imageUri = result.getData().getData();
                        imageView.setImageURI(imageUri);
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

    private void callAPI(String text) {
        JSONObject object = new JSONObject();
        try {
            object.put("model", "gpt-3.5-turbo");
            JSONArray messagesArray = new JSONArray();
            JSONObject systemMsg = new JSONObject();
            systemMsg.put("role", "system");
            systemMsg.put("content", "You are a helpful assistant that summarizes text.");
            messagesArray.put(systemMsg);
            JSONObject userMsg = new JSONObject();
            userMsg.put("role", "user");
            userMsg.put("content", "다음 내용을 사용자의 감상을 중심으로 요약해줘:\n" + text);
            messagesArray.put(userMsg);
            object.put("messages", messagesArray);
            object.put("temperature", 0.7);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(object.toString(), MediaType.get("application/json; charset=utf-8"));
        request = new Request.Builder()
                .url("https://api.openai.com/v1/chat/completions")
                .header("Authorization", "Bearer " + apiKey)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> {
                    summarizingImageView.setVisibility(View.GONE);
                    Toast.makeText(GroupWriteActivity.this, "요약 실패", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String responseBody = response.body().string();
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
}

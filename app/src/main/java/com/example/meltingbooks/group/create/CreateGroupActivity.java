package com.example.meltingbooks.group.create;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.meltingbooks.R;

public class CreateGroupActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1001;

    private EditText groupNameEditText, groupIntroEditText, etCustomTopic;
    private Spinner spinnerMainCategory, spinnerSubCategory;
    private ImageButton groupProfileImage;
    private ImageView imageView;
    private ImageButton createButton;

    private Uri selectedImageUri = null;
    private boolean isSpinnerInitialized = false;
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.group_new_create);

        groupNameEditText = findViewById(R.id.groupNameEditText);
        groupIntroEditText = findViewById(R.id.groupIntroEditText);
        etCustomTopic = findViewById(R.id.etCustomTopic);
        spinnerMainCategory = findViewById(R.id.spinnerMainCategory);
        spinnerSubCategory = findViewById(R.id.spinnerSubCategory);
        groupProfileImage = findViewById(R.id.groupProfileImage);
        imageView = findViewById(R.id.imageView);
        createButton = findViewById(R.id.createButton);

        setupSpinners();

        createButton.setOnClickListener(v -> validateAndCreateGroup());
        groupProfileImage.setOnClickListener(v -> openImagePicker());

        //그룹 대표 이미지 불러오기
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        imageView.setImageURI(selectedImageUri);
                    }
                }
        );

        // 상태바 색상 조정
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.white));
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            View decor = getWindow().getDecorView();
            decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    private void setupSpinners() {
        ArrayAdapter<CharSequence> mainAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.group_category_main,
                R.layout.spinner_item
        );
        mainAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinnerMainCategory.setAdapter(mainAdapter);

        spinnerMainCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!isSpinnerInitialized) {
                    isSpinnerInitialized = true;
                    spinnerSubCategory.setVisibility(View.GONE);
                    etCustomTopic.setVisibility(View.VISIBLE);
                    return;
                }

                switch (position) {
                    case 0: // 선택 안 함
                        spinnerSubCategory.setVisibility(View.GONE);
                        etCustomTopic.setVisibility(View.GONE);
                        break;
                    case 1: // 장르
                        showSubCategory(R.array.group_category_genre);
                        break;
                    case 2: // 목적
                        showSubCategory(R.array.group_category_purpose);
                        break;
                    case 3: // 관심사
                        showSubCategory(R.array.group_category_interest);
                        break;
                    default:
                        spinnerSubCategory.setVisibility(View.GONE);
                        etCustomTopic.setVisibility(View.VISIBLE);
                        break;
                }
            }

            @Override public void onNothingSelected(AdapterView<?> parent) {
                spinnerSubCategory.setVisibility(View.GONE);
                etCustomTopic.setVisibility(View.GONE);
            }
        });
    }

    private void showSubCategory(int arrayResId) {
        ArrayAdapter<CharSequence> subAdapter = ArrayAdapter.createFromResource(
                this,
                arrayResId,
                R.layout.spinner_item
        );
        subAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinnerSubCategory.setAdapter(subAdapter);
        spinnerSubCategory.setVisibility(View.VISIBLE);
        etCustomTopic.setVisibility(View.GONE);
    }



    private void validateAndCreateGroup() {
        String name = groupNameEditText.getText().toString().trim();
        String intro = groupIntroEditText.getText().toString().trim();

        if (name.length() < 3) {
            Toast.makeText(this, "그룹 이름은 3자 이상 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (intro.length() > 500) {
            Toast.makeText(this, "그룹 소개글은 500자 이하로 작성해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        String category;
        if (etCustomTopic.getVisibility() == View.VISIBLE) {
            category = etCustomTopic.getText().toString().trim();
            if (TextUtils.isEmpty(category)) {
                Toast.makeText(this, "카테고리를 입력해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }
        } else {
            String mainCat = (String) spinnerMainCategory.getSelectedItem();
            String subCat = spinnerSubCategory.getVisibility() == View.VISIBLE
                    ? (String) spinnerSubCategory.getSelectedItem()
                    : "";
            category = mainCat + (subCat.isEmpty() ? "" : " > " + subCat);
        }

        // TODO: 서버 전송 또는 데이터 저장 처리
        Toast.makeText(this,
                "그룹 생성 완료\n이름: " + name + "\n카테고리: " + category + "\n소개: " + intro,
                Toast.LENGTH_LONG).show();

        finish(); // 완료 후 종료
    }
}

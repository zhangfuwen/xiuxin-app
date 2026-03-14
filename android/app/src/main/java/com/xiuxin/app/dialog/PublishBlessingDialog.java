package com.xiuxin.app.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.xiuxin.app.R;
import com.xiuxin.app.api.BlessingsApiClient;
import com.xiuxin.app.model.Blessing;

/**
 * 发布禅语对话框
 */
public class PublishBlessingDialog extends Dialog {
    
    private EditText editText;
    private EditText sourceEdit;
    private EditText practiceEdit;
    private Spinner categorySpinner;
    private EditText nameEdit;
    private Button publishBtn;
    private Button cancelBtn;
    
    private OnPublishListener publishListener;
    private final BlessingsApiClient apiClient;
    
    public interface OnPublishListener {
        void onPublishSuccess(Blessing blessing);
        void onPublishError(String error);
    }
    
    private final String[] categories = {"禅宗", "儒家", "道家", "佛经"};
    
    public PublishBlessingDialog(@NonNull Context context) {
        super(context, R.style.Theme_Design_Light_BottomSheetDialog);
        apiClient = BlessingsApiClient.getInstance();
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_publish_blessing);
        
        initViews();
        setupListeners();
    }
    
    private void initViews() {
        editText = findViewById(R.id.editText);
        sourceEdit = findViewById(R.id.sourceEdit);
        practiceEdit = findViewById(R.id.practiceEdit);
        categorySpinner = findViewById(R.id.categorySpinner);
        nameEdit = findViewById(R.id.nameEdit);
        publishBtn = findViewById(R.id.publishBtn);
        cancelBtn = findViewById(R.id.cancelBtn);
        
        // Setup category spinner
        android.widget.ArrayAdapter<String> adapter = new android.widget.ArrayAdapter<>(
            getContext(),
            android.R.layout.simple_spinner_item,
            categories
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);
    }
    
    private void setupListeners() {
        cancelBtn.setOnClickListener(v -> dismiss());
        
        publishBtn.setOnClickListener(v -> {
            String text = editText.getText().toString().trim();
            String source = sourceEdit.getText().toString().trim();
            String practice = practiceEdit.getText().toString().trim();
            String category = (String) categorySpinner.getSelectedItem();
            String userName = nameEdit.getText().toString().trim();
            
            if (text.isEmpty()) {
                editText.setError("请输入禅语内容");
                editText.requestFocus();
                return;
            }
            
            if (userName.isEmpty()) {
                userName = "匿名";
            }
            
            publishBlessing(text, source, practice, category, userName);
        });
    }
    
    private void publishBlessing(String text, String source, String practice, String category, String userName) {
        publishBtn.setEnabled(false);
        publishBtn.setText("发布中...");
        
        apiClient.publishBlessing(text, source, practice, category, 
            new BlessingsApiClient.ApiCallback<Blessing>() {
                @Override
                public void onSuccess(Blessing blessing) {
                    publishBtn.setEnabled(true);
                    publishBtn.setText("发布");
                    
                    if (publishListener != null) {
                        publishListener.onPublishSuccess(blessing);
                    }
                    
                    Toast.makeText(getContext(), "发布成功！", Toast.LENGTH_SHORT).show();
                    dismiss();
                }
                
                @Override
                public void onError(String error) {
                    publishBtn.setEnabled(true);
                    publishBtn.setText("发布");
                    
                    if (publishListener != null) {
                        publishListener.onPublishError(error);
                    }
                    
                    Toast.makeText(getContext(), "发布失败：" + error, Toast.LENGTH_LONG).show();
                }
            });
    }
    
    public void setOnPublishListener(OnPublishListener listener) {
        this.publishListener = listener;
    }
}

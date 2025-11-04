package com.anhbhn.rentcar.ui.car.addCar;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import com.anhbhn.rentcar.R;

import java.util.Arrays;
import java.util.List;

public class AddCarActivity extends AppCompatActivity {

    private AddCarViewModel addCarViewModel;

    // --- KHAI BÁO VIEWS VÀ HẰNG SỐ CHO STEP INDICATOR ---
    private List<TextView> stepNumbers;
    private List<TextView> stepTitles;
    private List<View> stepLines;

    // Tài nguyên cần phải tồn tại trong res/
    private final int COLOR_ACTIVE_INACTIVE = R.color.white;
    private final int COLOR_INACTIVE_TITLE = R.color.white_50_opacity;
    private final int DRAWABLE_ACTIVE = R.drawable.step_active_background;
    private final int DRAWABLE_INACTIVE = R.drawable.step_inactive_background;
    private final int DRAWABLE_COMPLETED = R.drawable.step_active_background;
    private final int COLOR_NUMBER_INACTIVE = R.color.colorPrimary;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. Set Content View (Activity phải dùng layout activity_add_car.xml)
        setContentView(R.layout.activity_add_car);

        // 2. Khởi tạo ViewModel
        addCarViewModel = new ViewModelProvider(this).get(AddCarViewModel.class);

        // 3. Thiết lập Toolbar (Sử dụng findViewById)
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // 4. Thiết lập tham chiếu Views cho Step Indicator
        setupStepIndicatorViews();

        // 5. Bắt đầu quan sát ViewModel để cập nhật Step Indicator
        addCarViewModel.getCurrentStep().observe(this, this::updateStepIndicator);

        // Khởi tạo dữ liệu Brand/Model
        addCarViewModel.initializeCarOptions(this);
    }

    private void setupStepIndicatorViews() {
        // Lấy tham chiếu đến Container chứa Step Indicator
        View stepIndicatorContainer = findViewById(R.id.step_indicator_container);

        // Lấy tham chiếu đến các Views con bên trong Container (dùng findViewById)
        stepNumbers = Arrays.asList(
                stepIndicatorContainer.findViewById(R.id.step_1_number),
                stepIndicatorContainer.findViewById(R.id.step_2_number),
                stepIndicatorContainer.findViewById(R.id.step_3_number),
                stepIndicatorContainer.findViewById(R.id.step_4_number)
        );
        stepTitles = Arrays.asList(
                stepIndicatorContainer.findViewById(R.id.step_1_title),
                stepIndicatorContainer.findViewById(R.id.step_2_title),
                stepIndicatorContainer.findViewById(R.id.step_3_title),
                stepIndicatorContainer.findViewById(R.id.step_4_title)
        );
        stepLines = Arrays.asList(
                stepIndicatorContainer.findViewById(R.id.line_1_2),
                stepIndicatorContainer.findViewById(R.id.line_2_3),
                stepIndicatorContainer.findViewById(R.id.line_3_4)
        );

        // Cập nhật lần đầu
        updateStepIndicator(addCarViewModel.getCurrentStep().getValue() != null ? addCarViewModel.getCurrentStep().getValue() : 1);
    }

    /**
     * Cập nhật trạng thái hiển thị của Step Indicator (Active, Completed, Inactive).
     */
    private void updateStepIndicator(int currentStep) {

        int colorActiveInactive = ContextCompat.getColor(this, COLOR_ACTIVE_INACTIVE);
        int colorInactiveTitle = ContextCompat.getColor(this, COLOR_INACTIVE_TITLE);
        int colorNumberInactive = ContextCompat.getColor(this, COLOR_NUMBER_INACTIVE);

        for (int i = 0; i < 4; i++) {
            TextView numberView = stepNumbers.get(i);
            TextView titleView = stepTitles.get(i);
            View lineView = (i < 3) ? stepLines.get(i) : null;
            int stepIndex = i + 1;

            if (stepIndex < currentStep) {
                // 1. COMPLETED
                numberView.setBackgroundResource(DRAWABLE_COMPLETED);
                numberView.setText("");
                titleView.setTextColor(colorActiveInactive);

                if (lineView != null) {
                    lineView.setBackgroundColor(colorActiveInactive);
                }
            } else if (stepIndex == currentStep) {
                // 2. ACTIVE
                numberView.setBackgroundResource(DRAWABLE_ACTIVE);
                numberView.setText(String.valueOf(stepIndex));
                numberView.setTextColor(colorActiveInactive);
                titleView.setTextColor(colorActiveInactive);

                if (lineView != null) {
                    lineView.setBackgroundColor(colorActiveInactive);
                }
            } else {
                // 3. INACTIVE
                numberView.setBackgroundResource(DRAWABLE_INACTIVE);
                numberView.setText(String.valueOf(stepIndex));
                numberView.setTextColor(colorNumberInactive);
                titleView.setTextColor(colorInactiveTitle);

                if (lineView != null) {
                    lineView.setBackgroundColor(colorInactiveTitle);
                }
            }
        }
    }
}
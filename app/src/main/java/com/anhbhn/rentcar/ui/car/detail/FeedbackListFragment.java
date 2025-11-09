package com.anhbhn.rentcar.ui.car.detail;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.anhbhn.rentcar.R;
import com.anhbhn.rentcar.data.dto.response.car.CarDetailResponse;

import java.util.ArrayList;
import java.util.List;

public class FeedbackListFragment extends Fragment implements CarDetailActivity.CarDetailFragmentBase {

    private RecyclerView recyclerViewFeedback;
    private TextView tvNoFeedback;
    private CarDetailResponse carDetail;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_car_feedback, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerViewFeedback = view.findViewById(R.id.recyclerViewFeedback);
        tvNoFeedback = view.findViewById(R.id.tvNoFeedback);

        recyclerViewFeedback.setLayoutManager(new LinearLayoutManager(getContext()));

        // Try to get car detail from activity
        if (getActivity() != null && getActivity() instanceof CarDetailActivity) {
            CarDetailResponse detail = ((CarDetailActivity) getActivity()).getCarDetail();
            if (detail != null) {
                updateCarDetail(detail);
            }
        }

        // For now, show empty state
        showEmptyState();
    }

    @Override
    public void updateCarDetail(CarDetailResponse carDetail) {
        this.carDetail = carDetail;
        // TODO: Load feedback list from API when available
        showEmptyState();
    }

    private void showEmptyState() {
        recyclerViewFeedback.setVisibility(View.GONE);
        tvNoFeedback.setVisibility(View.VISIBLE);
    }
}


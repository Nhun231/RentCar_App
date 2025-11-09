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

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class TermOfUseFragment extends Fragment implements CarDetailActivity.CarDetailFragmentBase {

    private TextView tvBasePrice;
    private TextView tvDeposit;
    private RecyclerView recyclerViewTerms;
    private CarDetailResponse carDetail;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_car_term_of_use, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvBasePrice = view.findViewById(R.id.tvBasePrice);
        tvDeposit = view.findViewById(R.id.tvDeposit);
        recyclerViewTerms = view.findViewById(R.id.recyclerViewTerms);

        recyclerViewTerms.setLayoutManager(new LinearLayoutManager(getContext()));

        // Try to get car detail from activity
        updateFromActivity();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateFromActivity();
    }

    private void updateFromActivity() {
        if (getActivity() != null && getActivity() instanceof CarDetailActivity) {
            CarDetailResponse detail = ((CarDetailActivity) getActivity()).getCarDetail();
            if (detail != null) {
                updateCarDetail(detail);
            }
        }
    }

    @Override
    public void updateCarDetail(CarDetailResponse carDetail) {
        this.carDetail = carDetail;
        if (getView() == null) return;

        // Base Price
        long basePrice = carDetail.basePrice;
        String priceText;
        if (basePrice >= 1000000) {
            priceText = String.format(Locale.getDefault(), "%.1fM", basePrice / 1000000.0);
        } else if (basePrice >= 1000) {
            priceText = String.format(Locale.getDefault(), "%.0fK", basePrice / 1000.0);
        } else {
            priceText = NumberFormat.getNumberInstance(Locale.getDefault()).format(basePrice);
        }
        tvBasePrice.setText(priceText);

        // Deposit
        long deposit = carDetail.deposit;
        String depositText;
        if (deposit >= 1000000) {
            depositText = String.format(Locale.getDefault(), "%.1fM", deposit / 1000000.0);
        } else if (deposit >= 1000) {
            depositText = String.format(Locale.getDefault(), "%.0fK", deposit / 1000.0);
        } else {
            depositText = NumberFormat.getNumberInstance(Locale.getDefault()).format(deposit);
        }
        tvDeposit.setText(depositText);

        // Terms of Use
        setupTerms();
    }

    private void setupTerms() {
        if (carDetail == null || carDetail.termOfUse == null || carDetail.termOfUse.isEmpty()) {
            return;
        }

        // Parse terms (comma-separated)
        String[] terms = carDetail.termOfUse.split(",");
        List<TermItem> termList = new ArrayList<>();
        for (String term : terms) {
            termList.add(new TermItem(term.trim()));
        }

        TermAdapter adapter = new TermAdapter(termList);
        recyclerViewTerms.setAdapter(adapter);
    }

    private static class TermItem {
        String name;

        TermItem(String name) {
            this.name = name;
        }
    }

    private static class TermAdapter extends RecyclerView.Adapter<TermAdapter.TermViewHolder> {
        private List<TermItem> terms;

        TermAdapter(List<TermItem> terms) {
            this.terms = terms;
        }

        @NonNull
        @Override
        public TermViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_term, parent, false);
            return new TermViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull TermViewHolder holder, int position) {
            TermItem item = terms.get(position);
            holder.termName.setText(item.name);
            // Set appropriate icon based on term name
            setTermIcon(holder.termIcon, item.name);
            holder.termCheck.setVisibility(View.VISIBLE);
        }

        private void setTermIcon(android.widget.ImageView imageView, String termName) {
            String lowerName = termName.toLowerCase();
            if (lowerName.contains("smoking")) {
                imageView.setImageResource(android.R.drawable.ic_menu_delete);
            } else if (lowerName.contains("food")) {
                imageView.setImageResource(android.R.drawable.ic_menu_compass);
            } else if (lowerName.contains("pet")) {
                imageView.setImageResource(android.R.drawable.ic_menu_myplaces);
            } else {
                imageView.setImageResource(android.R.drawable.ic_menu_info_details);
            }
        }

        @Override
        public int getItemCount() {
            return terms.size();
        }

        class TermViewHolder extends RecyclerView.ViewHolder {
            android.widget.ImageView termIcon;
            TextView termName;
            android.widget.ImageView termCheck;

            TermViewHolder(View view) {
                super(view);
                termIcon = view.findViewById(R.id.ivTermIcon);
                termName = view.findViewById(R.id.tvTermName);
                termCheck = view.findViewById(R.id.ivTermCheck);
            }
        }
    }
}


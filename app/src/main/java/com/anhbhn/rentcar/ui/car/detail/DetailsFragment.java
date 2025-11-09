package com.anhbhn.rentcar.ui.car.detail;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.anhbhn.rentcar.R;
import com.anhbhn.rentcar.data.dto.response.car.CarDetailResponse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DetailsFragment extends Fragment implements CarDetailActivity.CarDetailFragmentBase {

    private TextView tvMileage;
    private TextView tvFuelConsumption;
    private TextView tvAddress;
    private TextView tvDescription;
    private RecyclerView recyclerViewAdditionalFunctions;
    private CarDetailResponse carDetail;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_car_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvMileage = view.findViewById(R.id.tvMileage);
        tvFuelConsumption = view.findViewById(R.id.tvFuelConsumption);
        tvAddress = view.findViewById(R.id.tvAddress);
        tvDescription = view.findViewById(R.id.tvDescription);
        recyclerViewAdditionalFunctions = view.findViewById(R.id.recyclerViewAdditionalFunctions);

        recyclerViewAdditionalFunctions.setLayoutManager(new GridLayoutManager(getContext(), 4));

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

        // Mileage
        tvMileage.setText(String.format("%.0f Miles", carDetail.mileage));

        // Fuel Consumption
        tvFuelConsumption.setText(String.format("%.1f L/100Km", carDetail.fuelConsumption));

        // Address
        if (carDetail.address != null && !carDetail.address.isEmpty()) {
            tvAddress.setText(carDetail.address);
        } else {
            tvAddress.setText("Address not available");
        }

        // Description
        if (carDetail.description != null && !carDetail.description.isEmpty()) {
            tvDescription.setText(carDetail.description);
        } else {
            tvDescription.setText("No description available");
        }

        // Additional Functions
        setupAdditionalFunctions();
    }

    private void setupAdditionalFunctions() {
        if (carDetail == null || carDetail.additionalFunction == null || carDetail.additionalFunction.isEmpty()) {
            recyclerViewAdditionalFunctions.setVisibility(View.GONE);
            return;
        }

        // Parse additional functions (comma-separated)
        String[] functions = carDetail.additionalFunction.split(",");
        List<FunctionItem> functionList = new ArrayList<>();
        for (String function : functions) {
            functionList.add(new FunctionItem(function.trim(), true));
        }

        FunctionAdapter adapter = new FunctionAdapter(functionList);
        recyclerViewAdditionalFunctions.setAdapter(adapter);
    }

    private static class FunctionItem {
        String name;
        boolean enabled;

        FunctionItem(String name, boolean enabled) {
            this.name = name;
            this.enabled = enabled;
        }
    }

    private static class FunctionAdapter extends RecyclerView.Adapter<FunctionAdapter.FunctionViewHolder> {
        private List<FunctionItem> functions;

        FunctionAdapter(List<FunctionItem> functions) {
            this.functions = functions;
        }

        @NonNull
        @Override
        public FunctionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_function, parent, false);
            return new FunctionViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull FunctionViewHolder holder, int position) {
            FunctionItem item = functions.get(position);
            holder.functionName.setText(item.name);
            holder.functionCheck.setVisibility(item.enabled ? View.VISIBLE : View.GONE);
            // Set appropriate icon based on function name
            setFunctionIcon(holder.functionIcon, item.name);
        }

        private void setFunctionIcon(android.widget.ImageView imageView, String functionName) {
            // Map function names to appropriate icons
            String lowerName = functionName.toLowerCase();
            if (lowerName.contains("bluetooth")) {
                imageView.setImageResource(android.R.drawable.ic_menu_share);
            } else if (lowerName.contains("sun") || lowerName.contains("roof")) {
                imageView.setImageResource(android.R.drawable.ic_menu_day);
            } else if (lowerName.contains("dvd")) {
                imageView.setImageResource(android.R.drawable.ic_menu_gallery);
            } else if (lowerName.contains("gps") || lowerName.contains("navigation")) {
                imageView.setImageResource(android.R.drawable.ic_menu_mylocation);
            } else if (lowerName.contains("child")) {
                imageView.setImageResource(android.R.drawable.ic_menu_myplaces);
            } else if (lowerName.contains("usb")) {
                imageView.setImageResource(android.R.drawable.ic_menu_send);
            } else if (lowerName.contains("camera")) {
                imageView.setImageResource(android.R.drawable.ic_menu_camera);
            } else {
                imageView.setImageResource(android.R.drawable.ic_menu_info_details);
            }
        }

        @Override
        public int getItemCount() {
            return functions.size();
        }

        class FunctionViewHolder extends RecyclerView.ViewHolder {
            android.widget.ImageView functionIcon;
            TextView functionName;
            android.widget.ImageView functionCheck;

            FunctionViewHolder(View view) {
                super(view);
                functionIcon = view.findViewById(R.id.ivFunctionIcon);
                functionName = view.findViewById(R.id.tvFunctionName);
                functionCheck = view.findViewById(R.id.ivFunctionCheck);
            }
        }
    }
}


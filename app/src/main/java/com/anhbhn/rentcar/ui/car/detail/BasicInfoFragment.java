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

public class BasicInfoFragment extends Fragment implements CarDetailActivity.CarDetailFragmentBase {

    private TextView tvLicensePlate;
    private TextView tvColor;
    private TextView tvBrand;
    private TextView tvModel;
    private TextView tvProductionYear;
    private TextView tvNumberOfSeats;
    private TextView tvTransmission;
    private TextView tvFuel;
    private RecyclerView recyclerViewDocuments;
    private CarDetailResponse carDetail;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_car_basic_info, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvLicensePlate = view.findViewById(R.id.tvLicensePlate);
        tvColor = view.findViewById(R.id.tvColor);
        tvBrand = view.findViewById(R.id.tvBrand);
        tvModel = view.findViewById(R.id.tvModel);
        tvProductionYear = view.findViewById(R.id.tvProductionYear);
        tvNumberOfSeats = view.findViewById(R.id.tvNumberOfSeats);
        tvTransmission = view.findViewById(R.id.tvTransmission);
        tvFuel = view.findViewById(R.id.tvFuel);
        recyclerViewDocuments = view.findViewById(R.id.recyclerViewDocuments);

        recyclerViewDocuments.setLayoutManager(new LinearLayoutManager(getContext()));

        // Try to get car detail from activity
        updateFromActivity();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Update when fragment becomes visible
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

        // License Plate
        if (carDetail.licensePlate != null) {
            tvLicensePlate.setText(carDetail.licensePlate);
        }

        // Color
        if (carDetail.color != null) {
            tvColor.setText(carDetail.color);
        }

        // Brand
        if (carDetail.brand != null) {
            tvBrand.setText(carDetail.brand);
        }

        // Model
        if (carDetail.model != null) {
            tvModel.setText(carDetail.model);
        }

        // Production Year
        tvProductionYear.setText(String.valueOf(carDetail.productionYear));

        // Number of Seats
        tvNumberOfSeats.setText(String.valueOf(carDetail.numberOfSeats));

        // Transmission
        boolean isAutomatic = carDetail.isAutomatic != null ? carDetail.isAutomatic : true;
        tvTransmission.setText(isAutomatic ? "Automatic" : "Manual");

        // Fuel
        boolean isGasoline = carDetail.isGasoline != null ? carDetail.isGasoline : true;
        tvFuel.setText(isGasoline ? "Gasoline" : "Diesel");

        // Documents
        setupDocuments();
    }

    private void setupDocuments() {
        if (carDetail == null) return;

        List<DocumentItem> documents = new ArrayList<>();
        documents.add(new DocumentItem("Registration Paper", carDetail.registrationPaperIsVerified != null && carDetail.registrationPaperIsVerified));
        documents.add(new DocumentItem("Certificate of Inspection", carDetail.certificateOfInspectionIsVerified != null && carDetail.certificateOfInspectionIsVerified));
        documents.add(new DocumentItem("Insurance", carDetail.insuranceIsVerified != null && carDetail.insuranceIsVerified));

        DocumentAdapter adapter = new DocumentAdapter(documents);
        recyclerViewDocuments.setAdapter(adapter);
    }

    private static class DocumentItem {
        String name;
        boolean verified;

        DocumentItem(String name, boolean verified) {
            this.name = name;
            this.verified = verified;
        }
    }

    private static class DocumentAdapter extends RecyclerView.Adapter<DocumentAdapter.DocumentViewHolder> {
        private List<DocumentItem> documents;

        DocumentAdapter(List<DocumentItem> documents) {
            this.documents = documents;
        }

        @NonNull
        @Override
        public DocumentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_document, parent, false);
            return new DocumentViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull DocumentViewHolder holder, int position) {
            DocumentItem item = documents.get(position);
            holder.documentNumber.setText(String.valueOf(position + 1));
            holder.documentName.setText(item.name);
            holder.documentNote.setText(item.verified ? "Verified" : "Not Verified");
            holder.documentNote.setTextColor(item.verified ? 
                holder.itemView.getContext().getResources().getColor(android.R.color.holo_green_dark, null) :
                holder.itemView.getContext().getResources().getColor(android.R.color.darker_gray, null));
        }

        @Override
        public int getItemCount() {
            return documents.size();
        }

        class DocumentViewHolder extends RecyclerView.ViewHolder {
            TextView documentNumber;
            TextView documentName;
            TextView documentNote;

            DocumentViewHolder(View view) {
                super(view);
                documentNumber = view.findViewById(R.id.tvDocumentNumber);
                documentName = view.findViewById(R.id.tvDocumentName);
                documentNote = view.findViewById(R.id.tvDocumentNote);
            }
        }
    }
}


package com.traveler.traveljournal.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.AsyncListDiffer;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.traveler.traveljournal.GpxHelper;
import com.traveler.traveljournal.R;
import com.traveler.traveljournal.databinding.TripItemBinding;
import com.traveler.traveljournal.model.Trip;
import com.traveler.traveljournal.model.TripLocation;

import java.util.List;
import java.util.Objects;

public class TripsAdapter extends RecyclerView.Adapter<TripsAdapter.TripsHolder> {

    private Context context;
    TripListener mListener;
    public TripsAdapter(Context context, TripListener mListener) {
        this.context = context;
        this.mListener = mListener;
    }

    private final DiffUtil.ItemCallback<Trip> differCallback = new DiffUtil.ItemCallback<Trip>() {
        @Override
        public boolean areItemsTheSame(@NonNull Trip oldItem, @NonNull Trip newItem) {
            return oldItem.getId().equals(newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Trip oldItem, @NonNull Trip newItem) {
            return oldItem.equals(newItem);
        }
    };

    private final AsyncListDiffer<Trip> differ = new AsyncListDiffer<>(this, differCallback);

    public void setData(List<Trip> trips) {
        differ.submitList(trips);
    }

    public List<Trip> getTrips() {
        return differ.getCurrentList();
    }


    @NonNull
    @Override
    public TripsHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new TripsHolder(
                TripItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull TripsHolder holder, int position) {
        holder.bindTrip(differ.getCurrentList().get(position));
    }

    @Override
    public int getItemCount() {
        return differ.getCurrentList().size();
    }

    public class TripsHolder extends RecyclerView.ViewHolder {

        private final TripItemBinding binding;

        public TripsHolder(TripItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            binding.getRoot().setOnClickListener(view -> {
                mListener.onTripClicked(differ.getCurrentList().get(getAdapterPosition()));
            });

            binding.tripItemShareBtn.setOnClickListener(view -> {
                Trip trip = differ.getCurrentList().get(getAdapterPosition());
                mListener.onTripShareClicked(trip);
            });
        }

        @SuppressLint("SetTextI18n")
        public void bindTrip(Trip trip) {
            Glide.with(binding.tripItemImageImv)
                    .load(trip.getThumbnail())
                    .error(R.drawable.place_holder)
                    .into(binding.tripItemImageImv);
            binding.tripItemPlaceNameTv.setText(trip.getName());
            binding.tripItemLocationTv.setText(trip.getCity() + ", " + trip.getCountry());
        }
    }
}

interface TripListener {
    void onTripClicked(Trip trip);
    void onTripShareClicked(Trip trip);
}

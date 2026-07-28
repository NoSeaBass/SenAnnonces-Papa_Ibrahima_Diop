package com.example.senannonces;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class AnnonceAdapter extends RecyclerView.Adapter<AnnonceViewHolder> {
    private Context context;
    private List<Annonce> listAnnonce;

    public AnnonceAdapter(Context context, List<Annonce> listAnnonce){
        this.context = context;
        this.listAnnonce = listAnnonce;
    }

    @NonNull
    @Override
    public AnnonceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.annonce_design, parent, false);
        return new AnnonceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AnnonceViewHolder holder, int position) {
        Annonce annonce = listAnnonce.get(position);

        holder.txtTitre.setText(annonce.getTitre());
        holder.txtPrix.setText(annonce.getPrix() + " FCFA");
        holder.txtQuartier.setText(annonce.getQuartier());
        holder.txtDate.setText(annonce.getDate());

        Glide.with(context)
                .load(annonce.getImageUrl())
                .into(holder.imgAnnonce);

        holder.btnDetails.setOnClickListener(v -> {
            Intent intent = new Intent(context, DetailAnnonce.class);
            intent.putExtra("ANNONCE_ID", annonce.getId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        int count = 0;

        if(listAnnonce != null){
            count = listAnnonce.size();
        }

        return count;
    }
}

package com.example.senannonces;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

public class AnnonceViewHolder extends RecyclerView.ViewHolder{
    public ImageView imgAnnonce;
    public TextView txtTitre, txtPrix, txtQuartier, txtDate;
    public Button btnDetails;

    public AnnonceViewHolder(View element){
        super(element);
        imgAnnonce = element.findViewById(R.id.img_annonce);
        txtTitre = element.findViewById(R.id.txt_titre);
        txtPrix = element.findViewById(R.id.txt_prix);
        txtQuartier = element.findViewById(R.id.txt_quartier);
        txtDate = element.findViewById(R.id.txt_date);
        btnDetails = element.findViewById(R.id.btn_details);
    }
}

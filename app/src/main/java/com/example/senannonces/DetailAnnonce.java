package com.example.senannonces;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.bumptech.glide.Glide;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import cz.msebera.android.httpclient.Header;
import org.json.JSONArray;
import org.json.JSONObject;

public class DetailAnnonce extends AppCompatActivity {

    private ImageView imgDetailAnnonce;
    private TextView txtDetailTitre, txtDetailPrix, txtDetailQuartier, txtDetailDescription, txtDetailVendeur;
    private Button btnAppel;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_detail_annonce);

        imgDetailAnnonce = findViewById(R.id.img_detail_annonce);
        txtDetailTitre = findViewById(R.id.txt_detail_titre);
        txtDetailPrix = findViewById(R.id.txt_detail_prix);
        txtDetailQuartier = findViewById(R.id.txt_detail_quartier);
        txtDetailDescription = findViewById(R.id.txt_detail_description);
        txtDetailVendeur = findViewById(R.id.txt_detail_vendeur);
        btnAppel = findViewById(R.id.btn_appel);
        progressBar = findViewById(R.id.progress_bar);

        String annonceId = getIntent().getStringExtra("ANNONCE_ID");

        if (annonceId != null) {
            recupererDetailAnnonce(annonceId);
        }
    }

    private void recupererDetailAnnonce(String id) {
        progressBar.setVisibility(View.VISIBLE);

        AsyncHttpClient client = new AsyncHttpClient();
        String url =
            "https://senannonces.89-167-122-158.sslip.io/api/annonces/" + id;

        client.get(
            url,
            new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    progressBar.setVisibility(View.GONE);
                    try {
                        String titre = response.getString("titre");
                        int prix = response.getInt("prix");
                        String quartier = response.getString("quartier");
                        String description = response.optString(
                            "description",
                            "Aucune description"
                        );
                        String vendeur = response.optString(
                            "vendeur",
                            "Inconnu"
                        );
                        String telephone = response.optString("telephone", "");
                        String imageUrl = response.getString("imageUrl");

                        txtDetailTitre.setText(titre);
                        txtDetailPrix.setText(prix + " FCFA");
                        txtDetailQuartier.setText(quartier);
                        txtDetailDescription.setText(description);
                        txtDetailVendeur.setText(vendeur);

                        Glide.with(DetailAnnonce.this)
                            .load(imageUrl)
                            .into(imgDetailAnnonce);

                        btnAppel.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (!telephone.isEmpty()) {
                                    Intent intent = new Intent(Intent.ACTION_DIAL);
                                    intent.setData(Uri.parse("tel:" + telephone));
                                    startActivity(intent);
                                } else {
                                    Toast.makeText(
                                            DetailAnnonce.this,
                                            "Numéro indisponible",
                                            Toast.LENGTH_SHORT
                                    ).show();
                                }
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(DetailAnnonce.this, "Erreur lors du chargement des détails", Toast.LENGTH_SHORT).show();
                }
            }
        );
    }
}

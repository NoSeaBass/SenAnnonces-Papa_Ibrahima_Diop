package com.example.senannonces;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;

public class Publication extends AppCompatActivity {

    private static final String URL_BASE = "https://senannonces.89-167-122-158.sslip.io/";

    private EditText inputPubTitre, inputPubPrix, inputPubQuartier, inputPubDescription;
    private Spinner dropdownPubCategorie;
    private Button btnPublier;
    private ProgressBar progressBar;

    private List<String> nomCategories = new ArrayList<>();
    private List<String> idCategories = new ArrayList<>();
    private ArrayAdapter<String> spinnerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_publication);

        if (!estConnecte()) {
            redirigerVersAuthentification();
            return;
        }

        inputPubTitre = findViewById(R.id.input_pub_titre);
        inputPubPrix = findViewById(R.id.input_pub_prix);
        dropdownPubCategorie = findViewById(R.id.dropdown_pub_categorie);
        inputPubQuartier = findViewById(R.id.input_pub_quartier);
        inputPubDescription = findViewById(R.id.input_pub_description);
        btnPublier = findViewById(R.id.btn_publier);
        progressBar = findViewById(R.id.progress_bar);

        spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, nomCategories);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dropdownPubCategorie.setAdapter(spinnerAdapter);

        chargerCategories();

        btnPublier.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!estConnecte()) {
                    Toast.makeText(Publication.this, "Veuillez vous connecter", Toast.LENGTH_SHORT).show();
                    redirigerVersAuthentification();
                } else {
                    publierAnnonce();
                }
            }
        });
    }

    private boolean estConnecte() {
        SharedPreferences prefs = getSharedPreferences("utilisateur", Context.MODE_PRIVATE);
        String token = prefs.getString("token", null);
        return token != null && !token.isEmpty();
    }

    private String getToken() {
        SharedPreferences prefs = getSharedPreferences("utilisateur", Context.MODE_PRIVATE);
        return prefs.getString("token", null);
    }

    private void deconnecter() {
        SharedPreferences prefs = getSharedPreferences("utilisateur", Context.MODE_PRIVATE);
        prefs.edit().remove("token").apply();
    }

    private void redirigerVersAuthentification() {
        Intent intent = new Intent(Publication.this, Authentification.class);
        startActivity(intent);
        finish();
    }

    private void chargerCategories() {
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(URL_BASE + "api/categories", new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                try {
                    nomCategories.clear();
                    idCategories.clear();

                    for (int i = 0; i < response.length(); i++) {
                        JSONObject cat = response.getJSONObject(i);
                        idCategories.add(cat.getString("id"));
                        nomCategories.add(cat.optString("emoji", "") + " " + cat.getString("nom"));
                    }
                    spinnerAdapter.notifyDataSetChanged();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {}
        });
    }

    private void publierAnnonce() {
        String titre = inputPubTitre.getText().toString().trim();
        String prixStr = inputPubPrix.getText().toString().trim();
        String quartier = inputPubQuartier.getText().toString().trim();
        String description = inputPubDescription.getText().toString().trim();

        int selectedCatPos = dropdownPubCategorie.getSelectedItemPosition();

        if (titre.isEmpty() || prixStr.isEmpty() || quartier.isEmpty() || selectedCatPos < 0) {
            Toast.makeText(this, "Veuillez remplir les champs obligatoires", Toast.LENGTH_SHORT).show();
            return;
        }

        int prix = Integer.parseInt(prixStr);
        String idCategorie = idCategories.get(selectedCatPos);

        progressBar.setVisibility(View.VISIBLE);

        AsyncHttpClient client = new AsyncHttpClient();
        client.addHeader("Authorization", "Bearer " + getToken());

        try {
            JSONObject body = new JSONObject();
            body.put("titre", titre);
            body.put("prix", prix);
            body.put("categorie", idCategorie);
            body.put("quartier", quartier);
            body.put("description", description);

            StringEntity entity = new StringEntity(body.toString(), "UTF-8");

            client.post(this, URL_BASE + "api/annonces", entity, "application/json", new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(Publication.this, "Annonce publiée !", Toast.LENGTH_SHORT).show();
                    finish();
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    progressBar.setVisibility(View.GONE);
                    if (statusCode == 401) {
                        Toast.makeText(Publication.this, "Session expirée", Toast.LENGTH_SHORT).show();
                        deconnecter();
                        redirigerVersAuthentification();
                    } else {
                        Toast.makeText(Publication.this, "Erreur lors de la publication", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } catch (Exception e) {
            progressBar.setVisibility(View.GONE);
            e.printStackTrace();
        }
    }
}
package com.example.senannonces;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.loopj.android.http.*;

import cz.msebera.android.httpclient.Header;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    private static final String categorie_selectionnee = "categorie_selectionnee";
    private static final String url = "https://senannonces.89-167-122-158.sslip.io/";

    private EditText inputSearch;
    private Button btnSearch;
    private Spinner dropdownCategorie;
    private ProgressBar progressBar;
    private RecyclerView recyclerAnnonces;
    private FloatingActionButton fabAdd;
    private List<Annonce> listeAnnonces;
    private AnnonceAdapter adapter;

    private List<String> nomCategories = new ArrayList<>();
    private List<String> idCategories = new ArrayList<>();
    private ArrayAdapter<String> spinnerAdapter;
    private int categoryPositionRestored = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        if (savedInstanceState != null) {
            categoryPositionRestored = savedInstanceState.getInt(categorie_selectionnee, 0);
        }

        inputSearch = findViewById(R.id.input_search);
        btnSearch = findViewById(R.id.btn_search);
        dropdownCategorie = findViewById(R.id.dropdown_categorie);
        progressBar = findViewById(R.id.progress_bar);
        recyclerAnnonces = findViewById(R.id.recycler_annonces);
        View zoneProfile = findViewById(R.id.zone_profile);
        fabAdd = findViewById(R.id.fab_add);

        spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, nomCategories);

        listeAnnonces = new ArrayList<>();

        recyclerAnnonces = findViewById(R.id.recycler_annonces);
        adapter = new AnnonceAdapter(this, listeAnnonces);

        zoneProfile.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(MainActivity.this, Authentification.class);
                        startActivity(intent);
                    }
                }
        );

        fabAdd.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(MainActivity.this, Publication.class);
                        startActivity(intent);
                    }
                }
        );

        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dropdownCategorie.setAdapter(spinnerAdapter);

        recyclerAnnonces.setLayoutManager(new LinearLayoutManager(this));
        recyclerAnnonces.setAdapter(adapter);

        recupererCategories();
        recupererAnnonces(null, null);

        dropdownCategorie.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        String recherche = inputSearch.getText().toString().trim();
                        String idCat = idCategories.get(position);
                        recupererAnnonces(recherche, idCat);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {}
                }
        );

        btnSearch.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String entree = inputSearch.getText().toString().trim();
                        int position = dropdownCategorie.getSelectedItemPosition();
                        String idCat = null;
                        if (position >= 0 && position < idCategories.size()) {
                            idCat = idCategories.get(position);
                        }
                        recupererAnnonces(entree, idCat);
                    }
                }
        );
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(categorie_selectionnee, dropdownCategorie.getSelectedItemPosition());
    }

    private void recupererCategories() {
        AsyncHttpClient client = new AsyncHttpClient();

        client.get(url + "api/categories", new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                        try {
                            nomCategories.clear();
                            idCategories.clear();

                            nomCategories.add("Toutes les catégories");
                            idCategories.add("");

                            for (int i = 0; i < response.length(); i++) {
                                JSONObject cat = response.getJSONObject(i);
                                String id = cat.getString("id");
                                String nom = cat.getString("nom");
                                String emoji = cat.optString("emoji", "");

                                idCategories.add(id);
                                nomCategories.add(emoji + " " + nom);
                            }
                            spinnerAdapter.notifyDataSetChanged();

                            if (categoryPositionRestored < nomCategories.size()) {
                                dropdownCategorie.setSelection(categoryPositionRestored);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {}
                }
        );
    }

    private void recupererAnnonces(String recherche, String idCategorie) {
        progressBar.setVisibility(View.VISIBLE);

        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();

        if (recherche != null && !recherche.isEmpty()) {
            params.put("search", recherche);
        }

        if (idCategorie != null && !idCategorie.isEmpty()) {
            params.put("categorie", idCategorie);
        }

        client.get(url + "api/annonces", params, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                        super.onSuccess(statusCode, headers, response);
                        progressBar.setVisibility(View.GONE);

                        try {
                            listeAnnonces.clear();
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject obj = response.getJSONObject(i);
                                Annonce annonce = new Annonce();

                                annonce.setId(obj.getString("id"));
                                annonce.setTitre(obj.getString("titre"));
                                annonce.setPrix(obj.getInt("prix"));
                                annonce.setQuartier(obj.getString("quartier"));
                                annonce.setImageUrl(obj.getString("imageUrl"));
                                annonce.setDate(obj.getString("date"));

                                listeAnnonces.add(annonce);
                            }
                            adapter.notifyDataSetChanged();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                        super.onFailure(statusCode, headers, responseString, throwable);
                        progressBar.setVisibility(View.GONE);
                        if(statusCode == 404){
                            Toast.makeText(MainActivity.this, "Ressource introuvable", Toast.LENGTH_SHORT).show();
                        }else {
                            Toast.makeText(MainActivity.this, "Erreur de connexion", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );
    }

    @Override
    protected void onResume() {
        super.onResume();
        mettreAJourProfilUI();
    }

    private void mettreAJourProfilUI() {
        TextView txtStatutConnexion = findViewById(R.id.txt_statut_connexion);
        SharedPreferences prefs = getSharedPreferences("utilisateur", Context.MODE_PRIVATE);

        String token = prefs.getString("token", null);
        String email = prefs.getString("email", null);

        if (token != null && !token.isEmpty() && email != null) {
            txtStatutConnexion.setText(email);
        } else {
            txtStatutConnexion.setText(R.string.statut_deconnecte);
        }
    }
}
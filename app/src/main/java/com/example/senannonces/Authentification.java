package com.example.senannonces;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;

public class Authentification extends AppCompatActivity {

    private static final String mode_selectionne = "mode_selectionne";
    private static final String URL_BASE = "https://senannonces.89-167-122-158.sslip.io/";

    private RadioGroup radioGroupAuth;
    private RadioButton radioConnexion, radioInscription;
    private EditText inputAuthNom, inputAuthEmail, inputAuthPassword, inputAuthTelephone;
    private Button btnAction;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_authentification);

        radioGroupAuth = findViewById(R.id.radio_group_auth);
        radioConnexion = findViewById(R.id.radio_connexion);
        radioInscription = findViewById(R.id.radio_inscription);

        inputAuthNom = findViewById(R.id.input_auth_nom);
        inputAuthEmail = findViewById(R.id.input_auth_email);
        inputAuthPassword = findViewById(R.id.input_auth_password);
        inputAuthTelephone = findViewById(R.id.input_auth_telephone);

        btnAction = findViewById(R.id.btn_action);
        progressBar = findViewById(R.id.progress_bar);

        radioGroupAuth.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.radio_connexion) {
                    passerEnModeConnexion();
                } else if (checkedId == R.id.radio_inscription) {
                    passerEnModeInscription();
                }
            }
        });

        btnAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (estConnecte()) {
                    effacerToken();
                    Toast.makeText(Authentification.this, "Vous êtes déconnecté(e)", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    if (radioConnexion.isChecked()) {
                        traiterConnexion();
                    } else {
                        traiterInscription();
                    }
                }
            }
        });

        if (savedInstanceState != null) {
            boolean isInscription = savedInstanceState.getBoolean(mode_selectionne, false);
            if (isInscription) {
                radioInscription.setChecked(true);
                passerEnModeInscription();
            } else {
                radioConnexion.setChecked(true);
                passerEnModeConnexion();
            }
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(mode_selectionne, radioInscription.isChecked());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (estConnecte()) {
            desactiverFormulairePourDeconnexion();
        } else {
            activerFormulaire();
        }
    }

    private boolean estConnecte() {
        SharedPreferences prefs = getSharedPreferences("utilisateur", Context.MODE_PRIVATE);
        String token = prefs.getString("token", null);
        return token != null && !token.isEmpty();
    }

    private void sauvegarderSession(String token, String email) {
        SharedPreferences prefs = getSharedPreferences("utilisateur", Context.MODE_PRIVATE);
        prefs.edit()
                .putString("token", token)
                .putString("email", email)
                .apply();
    }

    private void effacerToken() {
        SharedPreferences prefs = getSharedPreferences("utilisateur", Context.MODE_PRIVATE);
        prefs.edit()
                .remove("token")
                .remove("email")
                .apply();
    }

    private void desactiverFormulairePourDeconnexion() {
        radioConnexion.setEnabled(false);
        radioInscription.setEnabled(false);

        inputAuthNom.setVisibility(View.GONE);
        inputAuthEmail.setVisibility(View.GONE);
        inputAuthPassword.setVisibility(View.GONE);
        inputAuthTelephone.setVisibility(View.GONE);

        btnAction.setText("Se déconnecter");
    }

    private void activerFormulaire() {
        radioConnexion.setEnabled(true);
        radioInscription.setEnabled(true);

        inputAuthEmail.setVisibility(View.VISIBLE);
        inputAuthPassword.setVisibility(View.VISIBLE);
    }

    private void passerEnModeConnexion() {
        inputAuthNom.setVisibility(View.GONE);
        inputAuthTelephone.setVisibility(View.GONE);
        btnAction.setText("Se connecter");
    }

    private void passerEnModeInscription() {
        inputAuthNom.setVisibility(View.VISIBLE);
        inputAuthTelephone.setVisibility(View.VISIBLE);
        btnAction.setText("S'inscrire");
    }

    private void traiterConnexion() {
        String email = inputAuthEmail.getText().toString().trim();
        String password = inputAuthPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        AsyncHttpClient client = new AsyncHttpClient();

        try {
            JSONObject body = new JSONObject();
            body.put("email", email);
            body.put("password", password);

            StringEntity entity = new StringEntity(body.toString(), "UTF-8");

            client.post(this, URL_BASE + "api/auth/login", entity, "application/json", new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    progressBar.setVisibility(View.GONE);
                    try {
                        String token = response.getString("token");
                        sauvegarderSession(token, email);
                        Toast.makeText(Authentification.this, "Connexion réussie", Toast.LENGTH_SHORT).show();
                        finish();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    progressBar.setVisibility(View.GONE);
                    if (statusCode == 401) {
                        Toast.makeText(Authentification.this, "Identifiants incorrects", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(Authentification.this, "Une erreur est survenue", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } catch (Exception e) {
            progressBar.setVisibility(View.GONE);
            e.printStackTrace();
        }
    }

    private void traiterInscription() {
        String nom = inputAuthNom.getText().toString().trim();
        String email = inputAuthEmail.getText().toString().trim();
        String password = inputAuthPassword.getText().toString().trim();
        String telephone = inputAuthTelephone.getText().toString().trim();

        if (nom.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Veuillez remplir les champs obligatoires", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        AsyncHttpClient client = new AsyncHttpClient();

        try {
            JSONObject body = new JSONObject();
            body.put("nom", nom);
            body.put("email", email);
            body.put("password", password);
            if (!telephone.isEmpty()) {
                body.put("telephone", telephone);
            }

            StringEntity entity = new StringEntity(body.toString(), "UTF-8");

            client.post(this, URL_BASE + "api/auth/register", entity, "application/json", new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    progressBar.setVisibility(View.GONE);
                    try {
                        String token = response.getString("token");
                        sauvegarderSession(token, email);
                        Toast.makeText(Authentification.this, "Inscription réussie", Toast.LENGTH_SHORT).show();
                        finish();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    progressBar.setVisibility(View.GONE);
                    if (statusCode == 409) {
                        Toast.makeText(Authentification.this, "Compte déjà existant", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(Authentification.this, "Une erreur est survenue", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } catch (Exception e) {
            progressBar.setVisibility(View.GONE);
            e.printStackTrace();
        }
    }
}
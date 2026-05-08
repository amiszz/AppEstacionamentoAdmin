package com.paradastar.admin;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DashboardActivity extends AppCompatActivity {

    TextView tvDisponiveis, tvOcupadas, tvBloqueadas;
    RecyclerView recyclerVagas;
    Button btnAdicionarVaga;
    DatabaseReference db;
    VagaAdaptada adaptada;
    List<Map<String, Object>> listaVagas = new ArrayList<>();
    List<String> listaIds = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        db = FirebaseDatabase.getInstance().getReference();

        tvDisponiveis = findViewById(R.id.tvDisponiveis);
        tvOcupadas = findViewById(R.id.tvOcupadas);
        tvBloqueadas = findViewById(R.id.tvBloqueadas);
        recyclerVagas = findViewById(R.id.recyclerVagas);
        btnAdicionarVaga = findViewById(R.id.btnAdicionarVaga);

        adaptada = new VagaAdaptada(this, listaVagas, listaIds);
        recyclerVagas.setLayoutManager(new LinearLayoutManager(this));
        recyclerVagas.setAdapter(adaptada);

        btnAdicionarVaga.setOnClickListener(v -> adicionarVaga());

        carregarVagas();
    }

    void carregarVagas() {
        db.child("vagas").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                listaVagas.clear();
                listaIds.clear();
                int disponiveis = 0, ocupadas = 0, bloqueadas = 0;

                for (DataSnapshot vaga : snapshot.getChildren()) {
                    Map<String, Object> dados = new HashMap<>();
                    for (DataSnapshot campo : vaga.getChildren()) {
                        dados.put(campo.getKey(), campo.getValue());
                    }
                    listaVagas.add(dados);
                    listaIds.add(vaga.getKey());

                    String status = (String) dados.getOrDefault("status", "disponivel");
                    if ("ocupada".equals(status)) ocupadas++;
                    else if ("bloqueada".equals(status)) bloqueadas++;
                    else disponiveis++;
                }

                tvDisponiveis.setText(String.valueOf(disponiveis));
                tvOcupadas.setText(String.valueOf(ocupadas));
                tvBloqueadas.setText(String.valueOf(bloqueadas));
                adaptada.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError error) {}
        });
    }

    void adicionarVaga() {
        View dialogView = LayoutInflater.from(this).inflate(
                android.R.layout.simple_list_item_1, null);

        final EditText etId = new EditText(this);
        etId.setHint("ID da vaga (ex: A1, B2)");
        etId.setPadding(40, 20, 40, 20);

        new AlertDialog.Builder(this)
                .setTitle("Adicionar Nova Vaga")
                .setView(etId)
                .setPositiveButton("ADICIONAR", (d, w) -> {
                    String id = etId.getText().toString().trim().toUpperCase();
                    if (TextUtils.isEmpty(id)) {
                        Toast.makeText(this, "Digite o ID da vaga!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Map<String, Object> dados = new HashMap<>();
                    dados.put("status", "disponivel");
                    dados.put("placa", "");
                    dados.put("modelo", "");
                    dados.put("nomeCliente", "");
                    dados.put("uidCliente", "");
                    dados.put("horaEntrada", 0);

                    db.child("vagas").child(id).setValue(dados)
                            .addOnSuccessListener(aVoid ->
                                    Toast.makeText(this, "Vaga " + id + " criada!", Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
}
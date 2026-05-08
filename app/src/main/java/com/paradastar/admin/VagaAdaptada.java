package com.paradastar.admin;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VagaAdaptada extends RecyclerView.Adapter<VagaAdaptada.VagaViewHolder> {

    Context context;
    List<Map<String, Object>> vagas;
    List<String> ids;
    DatabaseReference db;

    public VagaAdaptada(Context context, List<Map<String, Object>> vagas, List<String> ids) {
        this.context = context;
        this.vagas = vagas;
        this.ids = ids;
        this.db = FirebaseDatabase.getInstance().getReference("vagas");
    }

    @NonNull
    @Override
    public VagaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_vaga, parent, false);
        return new VagaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VagaViewHolder holder, int position) {
        Map<String, Object> vaga = vagas.get(position);
        String id = ids.get(position);
        String status = (String) vaga.getOrDefault("status", "disponivel");

        holder.tvIdVaga.setText("Vaga " + id);

        switch (status) {
            case "ocupada":
                holder.tvStatus.setText("🔴 Ocupada");
                holder.tvStatus.setTextColor(Color.parseColor("#C62828"));
                String nome = (String) vaga.getOrDefault("nomeCliente", "");
                String placa = (String) vaga.getOrDefault("placa", "");
                String modelo = (String) vaga.getOrDefault("modelo", "");
                holder.tvDetalhes.setText("👤 " + nome + "\n🚗 " + placa + " - " + modelo);
                holder.btnAcao.setText("DESOCUPAR");
                holder.btnAcao.setTextColor(Color.WHITE);
                holder.btnAcao.setBackgroundTintList(
                        android.content.res.ColorStateList.valueOf(Color.parseColor("#C62828")));
                holder.btnAcao.setOnClickListener(v -> desocuparVaga(id));
                break;

            case "bloqueada":
                holder.tvStatus.setText("🟡 Bloqueada");
                holder.tvStatus.setTextColor(Color.parseColor("#E65100"));
                holder.tvDetalhes.setText("Vaga interditada");
                holder.btnAcao.setText("DESBLOQUEAR");
                holder.btnAcao.setTextColor(Color.WHITE);
                holder.btnAcao.setBackgroundTintList(
                        android.content.res.ColorStateList.valueOf(Color.parseColor("#2E7D32")));
                holder.btnAcao.setOnClickListener(v -> desbloquearVaga(id));
                break;

            default:
                holder.tvStatus.setText("🟢 Disponível");
                holder.tvStatus.setTextColor(Color.parseColor("#2E7D32"));
                holder.tvDetalhes.setText("Vaga livre");
                holder.btnAcao.setText("OCUPAR");
                holder.btnAcao.setTextColor(Color.WHITE);
                holder.btnAcao.setBackgroundTintList(
                        android.content.res.ColorStateList.valueOf(Color.parseColor("#2E7D32")));
                holder.btnAcao.setOnClickListener(v -> ocuparVaga(id));
                break;
        }

        holder.btnBloquear.setText(status.equals("bloqueada") ? "DESBLOQUEAR" : "BLOQUEAR");
        holder.btnBloquear.setTextColor(Color.WHITE);
        holder.btnBloquear.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(
                        status.equals("bloqueada") ?
                                Color.parseColor("#2E7D32") : Color.parseColor("#E65100")));
        holder.btnBloquear.setOnClickListener(v -> {
            if (status.equals("bloqueada")) {
                desbloquearVaga(id);
            } else {
                bloquearVaga(id);
            }
        });
    }

    void ocuparVaga(String idVaga) {
        View dialogView = LayoutInflater.from(context).inflate(R.layout.mensagem_ocupacao, null);
        EditText etPlaca = dialogView.findViewById(R.id.etPlaca);
        EditText etModelo = dialogView.findViewById(R.id.etModelo);
        EditText etNome = dialogView.findViewById(R.id.etNome);

        new AlertDialog.Builder(context)
                .setTitle("Ocupar Vaga " + idVaga)
                .setView(dialogView)
                .setPositiveButton("OCUPAR", (d, w) -> {
                    String placa = etPlaca.getText().toString().trim().toUpperCase();
                    String modelo = etModelo.getText().toString().trim();
                    String nome = etNome.getText().toString().trim();

                    if (placa.isEmpty() || modelo.isEmpty() || nome.isEmpty()) {
                        Toast.makeText(context, "Preencha todos os campos!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (!placa.matches("^[A-Z]{3}[0-9][A-Z0-9][0-9]{2}$")) {
                        Toast.makeText(context, "Placa inválida! Use o formato ABC1234 ou ABC1D23.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Map<String, Object> dados = new HashMap<>();
                    dados.put("status", "ocupada");
                    dados.put("placa", placa);
                    dados.put("modelo", modelo);
                    dados.put("nomeCliente", nome);
                    dados.put("uidCliente", "gestor");
                    dados.put("horaEntrada", System.currentTimeMillis());

                    db.child(idVaga).setValue(dados);
                    Toast.makeText(context, "Vaga " + idVaga + " ocupada!", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    void desocuparVaga(String idVaga) {
        new AlertDialog.Builder(context)
                .setTitle("Desocupar Vaga " + idVaga)
                .setMessage("Deseja desocupar esta vaga?\n\n📋 Encaminhe o cliente até a portaria para realizar o pagamento.")
                .setPositiveButton("DESOCUPAR", (d, w) -> {
                    Map<String, Object> dados = new HashMap<>();
                    dados.put("status", "disponivel");
                    dados.put("placa", "");
                    dados.put("modelo", "");
                    dados.put("nomeCliente", "");
                    dados.put("uidCliente", "");
                    dados.put("horaEntrada", 0);
                    db.child(idVaga).updateChildren(dados);
                    Toast.makeText(context, "Vaga liberada! Encaminhe o cliente à portaria.", Toast.LENGTH_LONG).show();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    void bloquearVaga(String idVaga) {
        new AlertDialog.Builder(context)
                .setTitle("Bloquear Vaga " + idVaga)
                .setMessage("Deseja bloquear/interditar esta vaga?")
                .setPositiveButton("BLOQUEAR", (d, w) -> {
                    Map<String, Object> dados = new HashMap<>();
                    dados.put("status", "bloqueada");
                    dados.put("placa", "");
                    dados.put("modelo", "");
                    dados.put("nomeCliente", "");
                    dados.put("uidCliente", "");
                    dados.put("horaEntrada", 0);
                    db.child(idVaga).setValue(dados);
                    Toast.makeText(context, "Vaga " + idVaga + " bloqueada!", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    void desbloquearVaga(String idVaga) {
        Map<String, Object> dados = new HashMap<>();
        dados.put("status", "disponivel");
        db.child(idVaga).updateChildren(dados);
        Toast.makeText(context, "Vaga " + idVaga + " desbloqueada!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public int getItemCount() { return vagas.size(); }

    static class VagaViewHolder extends RecyclerView.ViewHolder {
        TextView tvIdVaga, tvStatus, tvDetalhes;
        Button btnAcao, btnBloquear;

        VagaViewHolder(View view) {
            super(view);
            tvIdVaga = view.findViewById(R.id.tvIdVaga);
            tvStatus = view.findViewById(R.id.tvStatus);
            tvDetalhes = view.findViewById(R.id.tvDetalhes);
            btnAcao = view.findViewById(R.id.btnAcao);
            btnBloquear = view.findViewById(R.id.btnBloquear);
        }
    }
}
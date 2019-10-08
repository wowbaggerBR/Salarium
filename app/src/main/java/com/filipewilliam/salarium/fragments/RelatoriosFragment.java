package com.filipewilliam.salarium.fragments;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.filipewilliam.salarium.R;
import com.filipewilliam.salarium.adapter.RelatoriosAdapter;
import com.filipewilliam.salarium.config.ConfiguracaoFirebase;
import com.filipewilliam.salarium.helpers.Base64Custom;
import com.filipewilliam.salarium.helpers.DateCustom;
import com.filipewilliam.salarium.helpers.InvestimentosHelper;
import com.filipewilliam.salarium.model.Transacao;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class RelatoriosFragment extends Fragment {

    Spinner spinnerMesAno;
    public DatabaseReference referencia = ConfiguracaoFirebase.getFirebaseDatabase();
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private DateCustom dateCustom;
    private TextView textViewDespesasRelatorio;
    private TextView textViewSaldoRelatorio;
    private TextView textViewRecebidoRelatorio;
    private TextView textViewNadaAReportar;
    private InvestimentosHelper tratarValores;
    private RecyclerView recyclerViewRelatorio;
    private ProgressBar progressBarRelatorios;

    public RelatoriosFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final String idUsuario = Base64Custom.codificarBase64(autenticacao.getCurrentUser().getEmail());

        View view = inflater.inflate(R.layout.fragment_relatorios, container, false);

        textViewDespesasRelatorio = view.findViewById(R.id.textViewNegativoRelatorio);
        textViewSaldoRelatorio = view.findViewById(R.id.textViewSaldoRelatorio);
        textViewRecebidoRelatorio = view.findViewById(R.id.textViewPositivoRelatorio);
        textViewNadaAReportar = view.findViewById(R.id.textViewAvisoNadaARelatar);
        progressBarRelatorios = view.findViewById(R.id.progressBarRelatorios);
        spinnerMesAno = view.findViewById(R.id.spinnerMesAnoRelatorio);
        recyclerViewRelatorio = view.findViewById(R.id.recyclerViewHistoricoRelatorio);
        recyclerViewRelatorio.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerViewRelatorio.setHasFixedSize(true);

        final ArrayList<Transacao> listaTransacoes = new ArrayList<>();
        referencia.child("usuarios").child(idUsuario).child("transacao").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.getChildrenCount() > 0){
                    progressBarRelatorios.setVisibility(View.GONE);
                    List<String> listTransacoesMeses = new ArrayList<String>();
                    for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                        listTransacoesMeses.add(dateCustom.formatarMesAno(dataSnapshot1.getKey()));
                        Collections.reverse(listTransacoesMeses); //Não é muito elegante, mas o Firebase não conhece o conceito de ordenar dados de forma decrescente...

                        ArrayAdapter<String> transacoesAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, listTransacoesMeses);
                        transacoesAdapter.setDropDownViewResource(android.R.layout.simple_selectable_list_item);
                        spinnerMesAno.setAdapter(transacoesAdapter);

                    }
                }else{
                    progressBarRelatorios.setVisibility(View.GONE);
                    textViewNadaAReportar.setText("Você ainda não tem dados cadastrados!");

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }

        });

        spinnerMesAno.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String item = dateCustom.formataMesAnoFirebase((String) adapterView.getItemAtPosition(i));

                referencia.child("usuarios").child(idUsuario).child("transacao").child(item).orderByChild("data").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        listaTransacoes.clear();
                        double totalDespesaMes = 0;
                        double totalRecebidoMes = 0;
                        for(DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()){
                            Transacao transacao = dataSnapshot1.getValue(Transacao.class);
                            listaTransacoes.add(transacao);

                            if(dataSnapshot1.child("tipo").getValue().toString().equals("Gasto")){
                                totalDespesaMes = totalDespesaMes + Double.valueOf(transacao.getValor());

                            }else{
                                totalRecebidoMes = totalRecebidoMes + Double.valueOf(transacao.getValor());

                            }

                            atualizaDados(transacao, totalRecebidoMes, totalDespesaMes);
                        }
                        RelatoriosAdapter adapterTransacoes = new RelatoriosAdapter(getActivity(), listaTransacoes);
                        recyclerViewRelatorio.setAdapter(adapterTransacoes);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        return view;

    }

    public void atualizaDados(Transacao historico, Double saldoPositivo, Double saldoNegativo){

        Double saldoMes = saldoPositivo - saldoNegativo;

        System.out.println(historico.getValor());
        textViewRecebidoRelatorio.setText(tratarValores.tratarValores(saldoPositivo));
        textViewDespesasRelatorio.setText(tratarValores.tratarValores(saldoNegativo));

        if(saldoMes < 0){
            textViewSaldoRelatorio.setText(tratarValores.tratarValores(saldoMes));
            textViewSaldoRelatorio.setTextColor(ContextCompat.getColor(getContext(), R.color.corBotoesCancela));

        }else{
            textViewSaldoRelatorio.setText(tratarValores.tratarValores(saldoMes));
            textViewSaldoRelatorio.setTextColor(ContextCompat.getColor(getContext(), R.color.corBotoesConfirma));

        }

    }


}

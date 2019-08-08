package com.filipewilliam.salarium.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.filipewilliam.salarium.R;
import com.filipewilliam.salarium.adapter.AdapterTransacoes;
import com.filipewilliam.salarium.model.Transacao;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class ResumoFragment extends Fragment {

    private List<Transacao> listaTransacoes = new ArrayList<>();
    private RecyclerView recyclerViewTransacoes;
    private AdapterTransacoes adapterTransacoes;

    public ResumoFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        //return inflater.inflate(R.layout.fragment_resumo, container, false);
        View view = inflater.inflate(R.layout.fragment_resumo, container, false);

        recyclerViewTransacoes = (RecyclerView) view.findViewById(R.id.recyclerViewTransacoes);

        this.criarTransacoes();
        AdapterTransacoes adapterTransacoes = new AdapterTransacoes(listaTransacoes);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerViewTransacoes.setLayoutManager(layoutManager);
        recyclerViewTransacoes.setHasFixedSize(true);
        recyclerViewTransacoes.setAdapter(adapterTransacoes);

        return view;

    }

    public void criarTransacoes(){

        Transacao transacao = new Transacao("Gastei", "Mercado", "85.00", "7/8/2019");
        this.listaTransacoes.add(transacao);

        transacao = new Transacao("Gastei", "Combustível", "150.00", "7/8/2019");
        this.listaTransacoes.add(transacao);

        transacao = new Transacao("Gastei", "Água", "92.00", "7/8/2019");
        this.listaTransacoes.add(transacao);

        transacao = new Transacao("Recebi", "Salário", "2.000,00", "7/8/2019");
        this.listaTransacoes.add(transacao);

    }

}
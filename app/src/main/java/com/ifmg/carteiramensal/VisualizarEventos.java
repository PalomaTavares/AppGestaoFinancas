package com.ifmg.carteiramensal;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.VoiceInteractor;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import ferramentas.EventosDB;
import modelo.Evento;

public class VisualizarEventos extends AppCompatActivity {


    private TextView tituloTxt;
    private ListView listaEventos;
    private TextView totalTxt;
    private Button novoBtn;
    private Button cancelarBtn;

    private ArrayList<Evento> eventos;
    private ItemListaEventos adapter;

    // operacao = 0 indica entrada    operacao = 1 indica saida
    private int operacao = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visualizar_eventos);

        tituloTxt = (TextView) findViewById(R.id.tituloTxt);
        listaEventos = (ListView) findViewById(R.id.listaEventos);
        totalTxt = (TextView) findViewById(R.id.valorTotalTxt);
        novoBtn = (Button) findViewById(R.id.novoBtn);
        cancelarBtn = (Button) findViewById(R.id.cancelarBtn);

        Intent intencao = getIntent();
        operacao = intencao.getIntExtra("acao", -1);
        //0 - entrada 1 - saida

        ajusteOperacao();
        cadastrarEventos();
        cadastrarEventos();
        carregaEventosLista();
    }


    public void cadastrarEventos(){
        novoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(operacao != -1){

                    Intent trocaAct = new Intent(VisualizarEventos.this, CadastroEdicaoEventos.class);

                    if (operacao == 0){
                        trocaAct.putExtra("acao", 0);
                        startActivityForResult(trocaAct, 0);
                    }else{
                        trocaAct.putExtra("acao", 1);
                        startActivityForResult(trocaAct, 1);
                    }

                }
            }
        });

        cancelarBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }


    private void ajusteOperacao(){
        //realizar uma busca no banco a respeito das informacoes dos eventos a serem apresentados
        if (operacao == 0){
            tituloTxt.setText("Entradas");
        }else{
            if (operacao == 1){
                tituloTxt.setText("Saídas");
            }else{
                //erro na configuracao activity
                Toast.makeText(VisualizarEventos.this, "erro no parâmetro acao", Toast.LENGTH_LONG).show();
            }
        }
    }
    private void carregaEventosLista(){

        eventos = new ArrayList<>();

        RequestQueue pilha = Volley.newRequestQueue(this);

        String url = GlobalVar.urlServidor+"evento";

        StringRequest requisicao = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {
                    JSONObject resposta = new JSONObject(response);

                    if (resposta.getInt("cod") == 200) {

                        JSONArray eventosJSON = resposta.getJSONArray("informacao");

                        for (int i = 0; i < eventosJSON.length(); i++) {
                            JSONObject obj = eventosJSON.getJSONObject(i);

                            Evento temp = new Evento(obj.getInt("id"), obj.getString("nome"), obj.getDouble("valor"),
                                    new Date(obj.getLong("dataCadastro")), new Date(obj.getLong("dataValida")),
                                    new Date(obj.getLong("dataOcorreu")), obj.getString("urlImagem"));

                            if ((operacao == 0 && temp.getValor() > 0) || operacao == 1 && temp.getValor() < 0) {

                                if (temp.getValor() < 0) {
                                    temp.setValor(temp.getValor() * -1);
                                }
                                eventos.add(temp);

                            }
                        }

                        adapter = new ItemListaEventos(getApplicationContext(), eventos);
                        listaEventos.setAdapter(adapter);

                        listaEventos.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> adapterView, View view, int indice, long id) {
                                Evento eventoSelecionado = eventos.get(indice);

                                Intent novoFluxo = new Intent(VisualizarEventos.this, CadastroEdicaoEventos.class);

                                if (operacao == 0) {
                                    novoFluxo.putExtra("acao", 2);
                                } else {
                                    //editando um evento da saida
                                    novoFluxo.putExtra("acao", 3);
                                }

                                novoFluxo.putExtra("id", eventoSelecionado.getId() + "");

                                startActivityForResult(novoFluxo, operacao);
                            }
                        });
                        //somamos todos os valores para apresentar no total
                        double total = 0.0;

                        for (int i = 0; i < eventos.size(); i++) {
                            total += eventos.get(i).getValor();
                        }
                        totalTxt.setText(String.format("%.2f", total));

                    } else {
                        //um ploblema reportado pelo servidor
                        Toast.makeText(VisualizarEventos.this, resposta.getString("inforemacao"), Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException ex) {
                    Toast.makeText(VisualizarEventos.this, "Erro no padrão do retorno, contate a equpie de desenvolvimento", Toast.LENGTH_LONG).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(VisualizarEventos.this, "Verifique sua conexão e tente novamente...", Toast.LENGTH_LONG).show();
            }
        }){
            protected Map<String, String> getParams(){
                Map<String, String> parametros = new HashMap<>();
                parametros.put("idUsuario", GlobalVar.idUsuario+"");
                parametros.put("servico", "consulta");
                SimpleDateFormat formatador =  new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                parametros.put("dataConsulta", formatador.format(MainActivity.dataAPP.getTime()));
                return parametros;
            }
        };

        pilha.add(requisicao);
    }

    protected void onActivityResult(int codigoRequest, int codigoResultado, Intent data){
        super.onActivityResult(codigoRequest, codigoResultado, data);

       carregaEventosLista();

    }


}
package com.ifmg.carteiramensal;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
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
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import ferramentas.EventosDB;
import modelo.Evento;

public class MainActivity extends AppCompatActivity {

    private TextView titulo;
    private TextView entrada;
    private TextView saida;
    private TextView saldo;
    private ImageButton entradaBtn;
    private ImageButton saidaBtn;
    private Button anteriorBtn;
    private Button proximoBtn;
    private Button novoBtn;
    private Calendar hoje;
    static Calendar dataAPP;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //link entre os componentes java e xml

        titulo = (TextView) findViewById(R.id.tituloMain);
        entrada = (TextView) findViewById(R.id.entradaTxt);
        saida = (TextView) findViewById(R.id.saidaTxt);
        saldo = (TextView) findViewById(R.id.saldoTxt);

        entradaBtn = (ImageButton) findViewById(R.id.entradaBtn);
        saidaBtn = (ImageButton) findViewById(R.id.saidaBtn);

        anteriorBtn = (Button) findViewById(R.id.anteriorBtn);
        proximoBtn = (Button) findViewById(R.id.proximoBtn);
        novoBtn = (Button) findViewById(R.id.novoBtn);

        //responsavel por implementar todos os eventos de botoes
        cadastroEventos();

        // recupera data e hora atuais
        dataAPP = Calendar.getInstance();
        hoje = Calendar.getInstance();


        //responsavel por implementar todos os eventos de botoes

        mostraDataApp();
        atualizaValores();
        configuraPermissoes();
    }

    private void configuraPermissoes(){
        if (ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.CAMERA,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.INTERNET}, 0);
        }
    }

    private void cadastroEventos(){
        anteriorBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                atualizaMes(-1);
            }
        });
        proximoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                atualizaMes(1);
            }
        });

        novoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //EventosDB db = new EventosDB(MainActivity.this);
                //db.insereEvento();

                //Toast.makeText(MainActivity.this, db.getDatabaseName(), Toast.LENGTH_LONG).show();
            }
        });
        entradaBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent trocaAct = new Intent(MainActivity.this, VisualizarEventos.class);

                trocaAct.putExtra("acao", 0);

                startActivityForResult(trocaAct, 0);
            }
        });
        saidaBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent trocaAct = new Intent(MainActivity.this, VisualizarEventos.class);

                trocaAct.putExtra("acao", 1);

                //pedimos para visualizar a ativity passada como parametro
                startActivityForResult(trocaAct,1);
            }
        });
    }

    private void mostraDataApp(){
        //0-janeiro, 1-fevereiro ... 11-dezembro
        String nomeMes[] = {"Janeiro", "Fevereiro", "Março", "Abril", "Maio",
                "Junho", "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro"};
        int mes = dataAPP.get(Calendar.MONTH);
        int ano = dataAPP.get(Calendar.YEAR);

        titulo.setText(nomeMes[mes] + "/"+ano);

    }
    private void atualizaMes(int ajuste){
        dataAPP.add(Calendar.MONTH, ajuste);
         //proximo mes (nao pode passar do mes atual)
        if(ajuste > 0){
            if(dataAPP.after(hoje)){
                dataAPP.add(Calendar.MONTH, -1);
            }
        }else{
            //aqui temos que realizar uma busca de dados (avaliar se existem meses anteriores cadastrados

        }
       // dataAPP.add(Calendar.MONTH, ajuste);

        // aqui temos que realizar uma busca no banco de dados(avaliar se existem meses anteriores cadastrados)
        mostraDataApp();
        atualizaValores();
    }

    private void atualizaValores(){

        // buscando entradas e saidas cadastradas para este mes no banco de dados
        //EventosDB db = new EventosDB( MainActivity.this);

        RequestQueue pilha = Volley.newRequestQueue(this);

        String url = GlobalVar.urlServidor+"evento";

        StringRequest requisicao = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject resposta = new JSONObject(response);

                    if (resposta.getInt("cod") == 200) {

                        JSONArray eventosJson = resposta.getJSONArray("informacao");

                        ArrayList<Evento> saidasLista = new ArrayList<>();
                        ArrayList<Evento> entradasLista = new ArrayList<>();

                        //percorrendo a lista eventos JSON  e transformando-os para evento em JAVA, classificando os eventos em arrays especificos
                        for(int i = 0; i < eventosJson.length(); i++){
                            JSONObject obj = eventosJson.getJSONObject(i);
                            Evento temp = new Evento(obj.getInt("id"), obj.getString("nome"), obj.getDouble("valor"),
                                    new Date(obj.getLong("dataCadastro")), new Date(obj.getLong("dataValida")),
                                    new Date(obj.getLong("dataOcorreu")), obj.getString("urlImagem"));
                            if(temp.getValor() < 0){
                                temp.setValor(temp.getValor()*-1);
                                saidasLista.add(temp);
                            }else{
                                entradasLista.add(temp);
                            }
                        }

                        //somando todos os valors dos eventos recuperados em banco
                        double entradaTotal = 0.0;
                        double saidaTotal = 0.0;
                        double saldoTotal = 0.0;

                        for(int i = 0; i < entradasLista.size(); i++){
                            entradaTotal += entradasLista.get(i).getValor();
                        }

                        for(int i = 0; i < saidasLista.size(); i++){
                            saidaTotal += saidasLista.get(i).getValor();
                        }

                        //mostrando os valores para o usuario
                        saldoTotal = entradaTotal - saidaTotal;

                        entrada.setText(String.format("%.2f",entradaTotal));
                        saida.setText(String.format("%.2f",saidaTotal ));
                        saldo.setText(String.format("%.2f", saldoTotal ));;

                    } else {
                        //algum erro foi retornado pelo servidor
                        Toast.makeText(MainActivity.this, resposta.getString("informacao"), Toast.LENGTH_LONG).show();

                    }
                } catch (JSONException e) {
                    //problema com o formato json
                    Toast.makeText(MainActivity.this, "Erro no padrão do retorno, contate a equpie de desenvolvimento", Toast.LENGTH_LONG).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this, "Verifique a sua conexão e tente novamente...", Toast.LENGTH_LONG).show();
            }
        }){
            protected Map<String, String> getParams(){
                Map<String, String> parametros = new HashMap<>();
                SimpleDateFormat formatador = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                parametros.put("dataConsulta", formatador.format(dataAPP.getTime()));
                parametros.put("servico", "consulta");
                parametros.put("idUsuario", GlobalVar.idUsuario+"");

                return parametros;
            }
        };
        pilha.add(requisicao);

    }
    protected void  onActivityResult(int codigoRequest, int codigoResultado, Intent data){
        super.onActivityResult(codigoRequest, codigoResultado, data);

        atualizaValores();
    }
}
package com.ifmg.carteiramensal;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.VoiceInteractor;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;


import ferramentas.EventosDB;
import modelo.Evento;

public class CadastroEdicaoEventos extends AppCompatActivity {


    private TextView tituloTxt;
    private TextView nomeTxt;
    private TextView valorTxt;
    private TextView dataTxt;
    private CheckBox repeteBtn;
    private ImageView foto;
    private Button fotoBtn;
    private Button salvarBtn;
    private Button cancelarBtn;
    private Calendar calendarioTemp;
    private DatePickerDialog calendarioUsuario;
    private Spinner mesesRepeteSpi;


    //0 = cadastro entrada 1 = cadastro saida 2 = edicao entrada 3 = edicao saida
    private int acao = -1;
    private Evento eventoSelecionado;
    private String nomeFoto;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro_edicao_eventos);

        tituloTxt = (TextView) findViewById(R.id.tituloCadastroTxt);
        nomeTxt = (EditText) findViewById(R.id.nomeCadastroTxt);
        valorTxt = (EditText) findViewById(R.id.valorCadastroTxt);
        dataTxt = (TextView) findViewById(R.id.dataCadastroTxt);
        repeteBtn = (CheckBox) findViewById(R.id.repeteBtn);
        foto = (ImageView) findViewById(R.id.fotoCadastro);
        fotoBtn = (Button) findViewById(R.id.fotoBtn);
        salvarBtn = (Button) findViewById(R.id.salvarCadastroBtn);
        cancelarBtn = (Button) findViewById(R.id.cancelarCadastroBtn);
        mesesRepeteSpi = (Spinner) findViewById(R.id.mesesSpinner);

        Calendar hoje = Calendar.getInstance();
        SimpleDateFormat formatador = new SimpleDateFormat("dd/MM/yyyy");
        dataTxt.setText(formatador.format(hoje.getTime()));

        Intent intencao = getIntent();
         acao = intencao.getIntExtra("acao", -1);

         ajustaPorAcao();//requisicao criada aqui
         cadastraEventos();
         confSpinners();
    }
    private void confSpinners(){
        ArrayList<String> meses = new ArrayList<>();

        // vamos permitir nesta versão a repeticao de apenas 24 meses de um evento
        for(int i = 0; i <= 24; i++){
            meses.add(i + "");
        }
        ArrayAdapter<String> listaAdapter = new ArrayAdapter<String>(this,
                R.layout.support_simple_spinner_dropdown_item,
                meses);
        mesesRepeteSpi.setAdapter(listaAdapter);
        mesesRepeteSpi.setEnabled(false);
    }

    private void cadastraEventos(){

        //configurando o DatePicker
        calendarioTemp = Calendar.getInstance();
        /*if(acao >=2 ){
            calendarioTemp.setTime(eventoSelecionado.getOcorreu());
        }*/
        calendarioUsuario = new DatePickerDialog(CadastroEdicaoEventos.this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int ano, int mes, int dia) {
                calendarioTemp.set(ano, mes, dia);
                dataTxt.setText(dia + "/" + (mes + 1)+ "/" + ano);

            }
        },calendarioTemp.get(Calendar.YEAR), calendarioTemp.get(Calendar.MONTH), calendarioTemp.get(Calendar.DAY_OF_MONTH));

        dataTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                calendarioUsuario.show();
            }
        });

        salvarBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(acao < 2){
                   cadastrarNovoEvento();
                }else{
                    //update do evento
                    updateEvento();
                }

            }
        });

        //tratando a repeticao do evento
        repeteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(repeteBtn.isChecked()){
                    mesesRepeteSpi.setEnabled(true);
                }else{
                    mesesRepeteSpi.setEnabled(false);
                }
            }
        });

        cancelarBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(acao < 2){
                    //termina a execução de uma activity e retorna a anterior
                    finish();
                }else{
                    //aqui sera chamado o metodo de delete no bd
                   // EventosDB db = new EventosDB(CadastroEdicaoEventos.this);
                    //db.excluirEventoId(eventoSelecionado.getId());
                    //finish();
                    requestExcluirEvento(eventoSelecionado.getId());
                }

            }
        });

        fotoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent cameraActivity = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                startActivityForResult(cameraActivity, 100);
            }
        });
    }

    protected  void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK){
            Bitmap imagemUser = (Bitmap) data.getExtras().get("data");
            foto.setImageBitmap(imagemUser);
            foto.setBackground(null);

            salvarImagem(imagemUser);

        }
    }
    private void salvarImagem(Bitmap img){
        Random gerador = new Random();
        Date instante = new Date();

        //definindo o nome do arquivo (foto)
        String nome = gerador.nextInt() + "" + instante.getTime() + ".png";

        nomeFoto = nome;

        File sd = Environment.getExternalStorageDirectory();
        File fotoArquivo = new File(sd, nome);

        //gravacao em sistema de armazenamento do dispositivo
        try {
            FileOutputStream gravador = new FileOutputStream(fotoArquivo);
            img.compress(Bitmap.CompressFormat.PNG, 100, gravador);
            gravador.flush();
            gravador.close();

        }catch (Exception ex){
            System.err.println("erro ao armazenar a foto");
        }
    }
    //metodo chamado durante a edicao de evento
    private void carregarImagem(){
        if(nomeFoto != null){

            File sd = Environment.getExternalStorageDirectory();
            File arquivoLeitura = new File(sd, nomeFoto);

            try {

                FileInputStream leitor = new FileInputStream(arquivoLeitura);
                Bitmap img = BitmapFactory.decodeStream(leitor);

                foto.setImageBitmap(img);
                foto.setBackground(null);


            }catch (Exception ex){
                System.err.println("erro na leitura da foto");
            }
        }
    }

//metodo auxilia na reutilizacao da activity, altera os valores dos componentes reutilizaveis
    private  void ajustaPorAcao(){
        switch (acao){
            case 0:{
                tituloTxt.setText("Cadast. Entrada");
            }break;
            case 1:{
                tituloTxt.setText("Cadast. Saída");
            }break;
            case 2:{
                //edicao de entradas
                tituloTxt.setText("Edição. Entrada");
                ajusteEdicao();
            }break;
            case 3: {
                //dedicao de saidas
                tituloTxt.setText("Edição. Saída");
                ajusteEdicao();
            }break;
            default:{

            }
        }
    }

    private void ajusteEdicao(){
        cancelarBtn.setText("Excluir");
        salvarBtn.setText("Atualizar");

        //carregando a informação do banco de dados
        int id = Integer.parseInt(getIntent().getStringExtra("id"));

        if(id != 0){
            RequestQueue fila = Volley.newRequestQueue(this);

            String url = GlobalVar.urlServidor+"evento";

            StringRequest requisicao = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                        JSONObject resposta = new JSONObject(response);
                        if (resposta.getInt("cod") == 200) {
                            JSONObject obj = resposta.getJSONObject("informacao");
                            eventoSelecionado = new Evento(obj.getInt("id"), obj.getString("nome"), obj.getDouble("valor"),
                                    new Date(obj.getLong("dataCadastro")), new Date(obj.getLong("dataValida")),
                                    new Date(obj.getLong("dataOcorreu")), obj.getString("urlImagem"));
                            SimpleDateFormat formatar = new SimpleDateFormat("dd/MM/yyyy");

                            nomeTxt.setText(eventoSelecionado.getNome());
                            valorTxt.setText(eventoSelecionado.getValor() + "");
                            dataTxt.setText(formatar.format(eventoSelecionado.getOcorreu()));

                            nomeFoto = eventoSelecionado.getCaminhoFoto();
                            carregarImagem();

                            Calendar d1 = Calendar.getInstance();
                            d1.setTime(eventoSelecionado.getValida());

                            Calendar d2 = Calendar.getInstance();
                            d2.setTime(eventoSelecionado.getOcorreu());

                            repeteBtn.setChecked(d1.get(Calendar.MONTH) != d2.get(Calendar.MONTH) ? true : false);

                            if (repeteBtn.isChecked()) {
                                mesesRepeteSpi.setEnabled(true);

                                //diferença mes de cadastro e mes de validade
                                mesesRepeteSpi.setSelection(d1.get(Calendar.MONTH) - d2.get(Calendar.MONTH) - 1);
                            }
                            calendarioTemp = Calendar.getInstance();
                            calendarioTemp.setTime(eventoSelecionado.getOcorreu());
                        }else{
                            Toast.makeText(CadastroEdicaoEventos.this, resposta.getString("informacao"), Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException ex) {
                        Toast.makeText(CadastroEdicaoEventos.this, "Erro no padrão do retorno, contate a equpie de desenvolvimento", Toast.LENGTH_LONG).show();
                    }

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(CadastroEdicaoEventos.this, "Verifique a sua conexão e tente novamente...", Toast.LENGTH_LONG).show();
                }
            }) {
                protected Map<String, String> getParams() {
                    Map<String, String> parametros = new HashMap<>();
                    SimpleDateFormat formatador = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    parametros.put("servico", "consulta");
                    parametros.put("idUsuario", GlobalVar.idUsuario + "");
                    parametros.put("idEvento", id + "");

                    return parametros;
                }
            };

            fila.add(requisicao);

         }
    }
    private void  updateEvento(){
        eventoSelecionado.setNome(nomeTxt.getText().toString());
        eventoSelecionado.setValor(Double.parseDouble(valorTxt.getText().toString()));

        if(acao == 3){
            eventoSelecionado.setValor(eventoSelecionado.getValor() * -1);
        }
        eventoSelecionado.setOcorreu( calendarioTemp.getTime());

        // um novo calendario para calcular data limite(repeticao)
        Calendar dataLimite = Calendar.getInstance();
        dataLimite.setTime(calendarioTemp.getTime());
        dataLimite.set(Calendar.DAY_OF_MONTH, dataLimite.getActualMaximum(Calendar.DAY_OF_MONTH));

        //verificando se este evento ira repetir por alguns meses
        if(repeteBtn.isChecked()){
            String mesStr = (String)mesesRepeteSpi.getSelectedItem();

            dataLimite.add(Calendar.MONTH, Integer.parseInt(mesStr));

        }
        //setando para o ultimo dia do mes limite
        dataLimite.set(Calendar.DAY_OF_MONTH, dataLimite.getActualMaximum(Calendar.DAY_OF_MONTH));

        eventoSelecionado.setValida(dataLimite.getTime());

        eventoSelecionado.setCaminhoFoto(nomeFoto);

        requestUpdateEvento(eventoSelecionado);

    }

    private void requestUpdateEvento(Evento ev) {
        //finish();
        RequestQueue pilha = Volley.newRequestQueue(this);
        String url = GlobalVar.urlServidor + "evento";

        StringRequest jsonRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            //onResponse é executado assim que o servidor entraga o resultado do processamento
            @Override
            public void onResponse(String response) {
                //o parametro response é o resultado enviado do servidor para o app

                try {
                    JSONObject resposta = new JSONObject(response);

                    //200 indica sucesso
                    if (resposta.getInt("cod") == 200) {
                        Toast.makeText(CadastroEdicaoEventos.this, "Atualização feita com sucesso", Toast.LENGTH_LONG).show();
                        finish();
                    } else {
                        //erro...que foi relatado pelo servidor
                        Toast.makeText(CadastroEdicaoEventos.this, resposta.getString("informacao"), Toast.LENGTH_LONG).show();
                    }

                } catch (JSONException ex) {
                    //erro no formato json enviado pelo servidor
                    Toast.makeText(CadastroEdicaoEventos.this, "Erro no padrão do retorno, contate a equpie de desenvolvimento", Toast.LENGTH_LONG).show();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                Toast.makeText(CadastroEdicaoEventos.this, "verifique sua conexão e tente novamente...", Toast.LENGTH_LONG).show();
            }
        }) {
            protected Map<String, String> getParams() {
                Map<String, String> parametros = new HashMap<>();
                parametros.put("servico", "atualizacao");
                parametros.put("nome", eventoSelecionado.getNome());
                parametros.put("valor", eventoSelecionado.getValor()+"");
                parametros.put("idUsuario", GlobalVar.idUsuario+"");
                parametros.put("idEvento", eventoSelecionado.getId()+"");

                SimpleDateFormat formatador = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                parametros.put("dataOcorreu", formatador.format(eventoSelecionado.getOcorreu()));
                parametros.put("dataValida", formatador.format(eventoSelecionado.getValida()));

                return parametros;

            }
        };
        //coloca a requisiçao na pilha de execução
        pilha.add(jsonRequest);
    }

    private void requestExcluirEvento(long id){
        String url = GlobalVar.urlServidor+"evento";
        RequestQueue fila = Volley.newRequestQueue(this);
        StringRequest requisicao = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                try {
                    JSONObject resposta = new JSONObject(response);

                    //200 indiga sucesso
                    if (resposta.getInt("cod") == 200) {
                        Toast.makeText(CadastroEdicaoEventos.this, "Exclusão feita com sucesso", Toast.LENGTH_LONG).show();
                        finish();
                    }else{
                        Toast.makeText(CadastroEdicaoEventos.this, resposta.getString("informacao"), Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException ex) {
                    Toast.makeText(CadastroEdicaoEventos.this, "Erro no padrão do retorno, contate a equpie de desenvolvimento", Toast.LENGTH_LONG).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(CadastroEdicaoEventos.this, "Verifique a sua conexão e tente novamente...", Toast.LENGTH_LONG).show();
            }
        }){
            protected Map<String, String> getParams() {
                Map<String, String> parametros = new HashMap<>();
                parametros.put("servico", "exclusao");
                parametros.put("idUsuario", GlobalVar.idUsuario + "");
                parametros.put("idEvento", id + "");
                return parametros;
            }
        };
        fila.add(requisicao);
    }


    private void cadastrarNovoEvento() {

        String nome = nomeTxt.getText().toString();
        double valor = Double.parseDouble(valorTxt.getText().toString());

        if (acao == 1 || acao == 3) {
            valor *= -1;
        }

        Date diaEvento = calendarioTemp.getTime();

        // um novo calendario para calcular data limite(repeticao)
        Calendar dataLimite = Calendar.getInstance();
        dataLimite.setTime(calendarioTemp.getTime());

        //verificando se este evento ira repetir por alguns meses
        if (repeteBtn.isChecked()) {
            String mesStr = (String) mesesRepeteSpi.getSelectedItem();

            dataLimite.add(Calendar.MONTH, Integer.parseInt(mesStr));

        }
        //setando para o ultimo dia do mes limite
        dataLimite.set(Calendar.DAY_OF_MONTH, dataLimite.getActualMaximum(Calendar.DAY_OF_MONTH));
        dataLimite.set(Calendar.HOUR_OF_DAY, dataLimite.getActualMaximum(Calendar.HOUR_OF_DAY));
        dataLimite.set(Calendar.MINUTE, 59);

        Evento novoEvento = new Evento(nome, valor, new Date(), dataLimite.getTime(), diaEvento, nomeFoto);

        requestCadastroEvento(novoEvento);

        finish();
    }

    private void requestCadastroEvento(Evento novo){

        RequestQueue pilha = Volley.newRequestQueue(this);
        String url = GlobalVar.urlServidor + "evento";

        StringRequest requisicao = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {
                    JSONObject resposta = new JSONObject(response);

                    //200 indiga sucesso
                    if (resposta.getInt("cod") == 200) {
                        Toast.makeText(CadastroEdicaoEventos.this, resposta.getString("informacao"), Toast.LENGTH_LONG).show();

                    } else {
                        //erro...que foi relatado pelo servidor
                        Toast.makeText(CadastroEdicaoEventos.this, resposta.getString("informacao"), Toast.LENGTH_LONG).show();
                    }

                } catch (JSONException ex) {
                    //erro no formato json enviado pelo servidor
                    Toast.makeText(CadastroEdicaoEventos.this, "Erro no padrão do retorno, contate a equpie de desenvolvimento", Toast.LENGTH_LONG).show();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                Toast.makeText(CadastroEdicaoEventos.this, "verifique sua conexão e tente novamente...", Toast.LENGTH_LONG).show();
            }
        }){
            protected Map<String, String> getParams(){
                Map<String, String> parametros = new HashMap<>();
                parametros.put("nome", novo.getNome());
                parametros.put("valor", novo.getValor()+"");
                parametros.put("idUsuario",GlobalVar.idUsuario+"");
                parametros.put("urlImagem",novo.getCaminhoFoto() == null?"null": novo.getCaminhoFoto());

                SimpleDateFormat formatar = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                parametros.put("dataOcorreu", formatar.format(novo.getOcorreu()));
                parametros.put("dataValida", formatar.format(novo.getValida()));
                parametros.put("servico", "cadastro");

                return parametros;
            }
        };
        //coloca a requisiçao na pilha de execução
        pilha.add(requisicao);
    }

    /* private void cadastrarNovoEvento(){

        String nome = nomeTxt.getText().toString();
        double valor = Double.parseDouble(valorTxt.getText().toString());

        if(acao  == 1 || acao == 3){
            valor *= -1;
        }

        //SimpleDateFormat formatador = new SimpleDateFormat("dd/MM/yyyy");

       // String dataStr = dataTxt.getText().toString();

       // try {
            Date diaEvento = calendarioTemp.getTime();

            // um novo calendario para calcular data limite(repeticao)
            Calendar dataLimite = Calendar.getInstance();
            dataLimite.setTime(calendarioTemp.getTime());
            dataLimite.set(Calendar.DAY_OF_MONTH, dataLimite.getActualMaximum(Calendar.DAY_OF_MONTH));

            //verificando se este evento ira repetir por alguns meses
            if(repeteBtn.isChecked()){
              String mesStr = (String)mesesRepeteSpi.getSelectedItem();

              dataLimite.add(Calendar.MONTH, Integer.parseInt(mesStr));

            }
            //setando para o ultimo dia do mes limite
            dataLimite.set(Calendar.DAY_OF_MONTH, dataLimite.getActualMaximum(Calendar.DAY_OF_MONTH));

            Evento novoEvento = new Evento (nome, valor, new Date(), dataLimite.getTime(), diaEvento, nomeFoto);

            //inserir evento no bd
            EventosDB bd = new EventosDB(CadastroEdicaoEventos.this);
            bd.insereEvento(novoEvento);

            Toast.makeText(CadastroEdicaoEventos.this, "Cadastro feito com sucesso", Toast.LENGTH_LONG).show();

            finish();

        //}catch (ParseException ex){
          //  System.err.println("erro no formato da data");
        //}*/

   // }

}
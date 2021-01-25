package com.ifmg.carteiramensal;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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

         ajustaPorAcao();
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
                    EventosDB db = new EventosDB(CadastroEdicaoEventos.this);
                    //db.excluirEventoId(eventoSelecionado.getId());
                    finish();
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
            EventosDB db = new EventosDB(CadastroEdicaoEventos.this);
            eventoSelecionado = db.buscaEventoId(id);

            //carregar as informacoes do campos recuperados do banco
            SimpleDateFormat formatar = new SimpleDateFormat("dd/MM/yyyy");

            nomeTxt.setText(eventoSelecionado.getNome());
            valorTxt.setText(eventoSelecionado.getValor()+"");
            dataTxt.setText(formatar.format(eventoSelecionado.getOcorreu()));

            nomeFoto = eventoSelecionado.getCaminhoFoto();
            carregarImagem();

            Calendar d1 = Calendar.getInstance();
            d1.setTime(eventoSelecionado.getValida());

            Calendar d2 = Calendar.getInstance();
            d2.setTime(eventoSelecionado.getOcorreu());

            repeteBtn.setChecked(d1.get(Calendar.MONTH) != d2.get(Calendar.MONTH) ? true : false);

            if(repeteBtn.isChecked()){
                mesesRepeteSpi.setEnabled(true);

                //diferença mes de cadastro e mes de validade
               mesesRepeteSpi.setSelection(d1.get(Calendar.MONTH) - d2.get(Calendar.MONTH) - 1);
            }
             calendarioTemp = Calendar.getInstance();
            calendarioTemp.setTime(eventoSelecionado.getOcorreu());
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

        EventosDB db = new EventosDB(CadastroEdicaoEventos.this);
        db.updateEvento(eventoSelecionado);
        finish();
    }

    private void cadastrarNovoEvento(){

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
        //}

    }

}
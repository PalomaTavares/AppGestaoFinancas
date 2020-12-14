package ferramentas;
//Paloma Tavares e Rebeka Góes
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import modelo.Evento;

public class EventosDB extends SQLiteOpenHelper {
    private Context contexto;
    public EventosDB(Context cont){
        super(cont, "evento", null, 1);
        contexto = cont;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        final String criaTabela = "CREATE TABLE IF NOT EXISTS evento(id INTEGER PRIMARY KEY AUTOINCREMENT, nome TEXT," +
                "valor REAL, imagem TEXT, dataocorreu DATE, datacadastro DATE, datavalida DATE)";
        db.execSQL(criaTabela);
    }

    public void insereEvento(Evento novoEvento){

        try(SQLiteDatabase db = this.getWritableDatabase()){
           /* String sql = "INSERT into evento(nome, valor) VALUES('evento1', 89)";
            db.execSQL(sql);
            */
            ContentValues valores = new ContentValues();
            valores.put("nome", novoEvento.getNome());
            valores.put("valor", novoEvento.getValor());
            valores.put("imagem", novoEvento.getCaminhoFoto());
            valores.put("dataocorreu", novoEvento.getOcorreu().getTime());
            valores.put("datacadastro", new Date().getTime());
            valores.put("datavalida", novoEvento.getValida().getTime());


            db.insert("evento", null, valores);

        }catch(SQLiteException ex){

            ex.printStackTrace();
        }




    }
    public void atualizaEvento(){

    }
    public ArrayList<Evento> buscaEventos(int op, Calendar data){

        ArrayList<Evento> resultado = new ArrayList<>();

        //dia 1 do mes
        Calendar dia1 = Calendar.getInstance();
        dia1.setTime(data.getTime());
        dia1.set(Calendar.DAY_OF_MONTH,1);

        //ultimo dia do mes
        Calendar dia2 = Calendar.getInstance();
        dia2.setTime(data.getTime());
        dia2.set(Calendar.DAY_OF_MONTH, dia2.getActualMaximum(Calendar.DAY_OF_MONTH));

        String sql = "SELECT * FROM evento WHERE dataocorreu <= " + dia2.getTime().getTime() +
                " AND dataocorreu >= "+dia1.getTime().getTime();

        sql += " AND valor ";

        if(op == 0){
            //entradas
            sql += ">=0";
        }else{
            //saidas (indicada por valor negativo)
            sql += "<=0";
        }
        try(SQLiteDatabase db = this.getWritableDatabase()) {

            Cursor tuplas = db.rawQuery(sql, null);

            //efetuar a leitura das tupla
            if(tuplas.moveToFirst()){

                do{
                    long id = tuplas.getInt(0);
                    String nome = tuplas.getString(1);
                    double valor = tuplas.getDouble(2);
                    if(valor < 0){
                        valor += -1;
                    }
                    String urlFoto = tuplas.getString(3);
                    Date dataocorreu = new Date(tuplas.getLong(4));
                    Date datacadastro = new Date(tuplas.getLong(5));
                    Date datavalida = new Date(tuplas.getLong(6));

                    Evento temporario = new Evento((long) id, nome, valor, dataocorreu, datacadastro, datavalida, urlFoto);
                    resultado.add(temporario);

                }while(tuplas.moveToNext());
            }
        }catch (SQLiteException ex){
            System.err.println("ocorreu um bug na consulta do banco");
            ex.printStackTrace();
        }

        return resultado;
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        //ficara parado ate a atualizacao da Activity de update (funcionalidade)

    }
}

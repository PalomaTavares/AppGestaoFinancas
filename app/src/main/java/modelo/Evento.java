package modelo;

import java.util.Date;

public class Evento {
    private  long id;
    private String nome;
    private double valor;
    private Date cadastro;
    private Date valida;
    private Date ocorreu;
    private String caminhoFoto;


    public Evento(long id, String nome, double valor, Date cadastro, Date valida, Date ocorreu, String caminhoFoto) {
        this.id = id;
        this.nome = nome;
        this.valor = valor;
        this.cadastro = cadastro;
        this.valida = valida;
        this.ocorreu = ocorreu;
        this.caminhoFoto = caminhoFoto;
    }

    public Evento(String nome, double valor, Date cadastro, Date valida, Date ocorreu, String caminhoFoto) {
        this.nome = nome;
        this.valor = valor;
        this.cadastro = cadastro;
        this.valida = valida;
        this.ocorreu = ocorreu;
        this.caminhoFoto = caminhoFoto;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public double getValor() {
        return valor;
    }

    public void setValor(double valor) {
        this.valor = valor;
    }

    public Date getCadastro() {
        return cadastro;
    }

    public void setCadastro(Date cadastro) {
        this.cadastro = cadastro;
    }

    public Date getValida() {
        return valida;
    }

    public void setValida(Date valida) {
        this.valida = valida;
    }

    public Date getOcorreu() {
        return ocorreu;
    }

    public void setOcorreu(Date ocorreu) {
        this.ocorreu = ocorreu;
    }

    public String getCaminhoFoto() {
        return caminhoFoto;
    }

    public void setCaminhoFoto(String caminhoFoto) {
        this.caminhoFoto = caminhoFoto;
    }
}

// Book.java
public class Book {
    private int id;
    private String categoria;
    private String titulo;

    public Book(int id, String categoria, String titulo) {
        this.id = id;
        this.categoria = categoria;
        this.titulo = titulo;
    }

    public int getId() { return id; }
    public String getCategoria() { return categoria; }
    public String getTitulo() { return titulo; }

    public void setId(int id) { this.id = id; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    @Override
    public String toString() {
        return String.format("{\"id\":%d,\"categoria\":\"%s\",\"titulo\":\"%s\"}", id, categoria, titulo);
    }
}

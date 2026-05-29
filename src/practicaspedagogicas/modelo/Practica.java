package practicaspedagogicas.modelo;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Modelo que representa una práctica pedagógica.
 * Corresponde a la tabla 'practica'. Máximo 8 por programa.
 *
 * @version 1.0
 */
public class Practica {

    private int           id;
    private int           idPrograma;
    private int           numero;           // 1 a 8
    private String        nombre;
    private String        tipo;             // Observacion|Intervencion|Investigativa|Profundizacion|Otro
    private String        objetivos;
    private int           horasMinimas;
    private String        semestre;         // Ej: "2026-1"
    private LocalDate     fechaInicio;
    private LocalDate     fechaFin;
    private String        estado;           // Planificada|Activa|Finalizada|Cancelada
    private LocalDateTime fechaCreacion;
    private boolean       activo;

    // Atributo de conveniencia (no columna): nombre del programa
    private String nombrePrograma;

    // ── Constructores ─────────────────────────────────────────────────────────

    public Practica() {}

    public Practica(int idPrograma, int numero, String nombre, String tipo,
                    String objetivos, int horasMinimas, String semestre,
                    LocalDate fechaInicio, LocalDate fechaFin) {
        this.idPrograma  = idPrograma;
        this.numero      = numero;
        this.nombre      = nombre;
        this.tipo        = tipo;
        this.objetivos   = objetivos;
        this.horasMinimas = horasMinimas;
        this.semestre    = semestre;
        this.fechaInicio = fechaInicio;
        this.fechaFin    = fechaFin;
        this.estado      = "Planificada";
        this.activo      = true;
    }

    // ── Getters y Setters ─────────────────────────────────────────────────────

    public int           getId()              { return id; }
    public void          setId(int id)        { this.id = id; }

    public int           getIdPrograma()      { return idPrograma; }
    public void          setIdPrograma(int i) { this.idPrograma = i; }

    public int           getNumero()          { return numero; }
    public void          setNumero(int n)     { this.numero = n; }

    public String        getNombre()          { return nombre; }
    public void          setNombre(String n)  { this.nombre = n; }

    public String        getTipo()            { return tipo; }
    public void          setTipo(String t)    { this.tipo = t; }

    public String        getObjetivos()       { return objetivos; }
    public void          setObjetivos(String o){ this.objetivos = o; }

    public int           getHorasMinimas()    { return horasMinimas; }
    public void          setHorasMinimas(int h){ this.horasMinimas = h; }

    public String        getSemestre()        { return semestre; }
    public void          setSemestre(String s){ this.semestre = s; }

    public LocalDate     getFechaInicio()     { return fechaInicio; }
    public void          setFechaInicio(LocalDate f){ this.fechaInicio = f; }

    public LocalDate     getFechaFin()        { return fechaFin; }
    public void          setFechaFin(LocalDate f)  { this.fechaFin = f; }

    public String        getEstado()          { return estado; }
    public void          setEstado(String e)  { this.estado = e; }

    public LocalDateTime getFechaCreacion()   { return fechaCreacion; }
    public void          setFechaCreacion(LocalDateTime f){ this.fechaCreacion = f; }

    public boolean       isActivo()           { return activo; }
    public void          setActivo(boolean a) { this.activo = a; }

    public String        getNombrePrograma()  { return nombrePrograma; }
    public void          setNombrePrograma(String n){ this.nombrePrograma = n; }

    @Override
    public String toString() { return "Práctica " + numero + ": " + nombre; }
}

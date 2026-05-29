package practicaspedagogicas.modelo;

import java.time.LocalDateTime;

/**
 * Modelo que representa un programa de licenciatura.
 * Corresponde a la tabla 'programa' en la base de datos.
 *
 * @version 1.0
 */
public class Programa {

    private int           id;
    private String        nombre;
    private String        codigoSnies;
    private String        facultad;
    private String        modalidad;   // Presencial, Virtual, A distancia, Mixta
    private String        nivel;       // Pregrado, Posgrado
    private boolean       acreditado;
    private LocalDateTime fechaRegistro;
    private boolean       activo;

    // ── Constructores ─────────────────────────────────────────────────────────

    /** Constructor vacío requerido por los DAOs. */
    public Programa() {}

    /** Constructor completo para nuevos registros. */
    public Programa(String nombre, String codigoSnies, String facultad,
                    String modalidad, String nivel, boolean acreditado) {
        this.nombre      = nombre;
        this.codigoSnies = codigoSnies;
        this.facultad    = facultad;
        this.modalidad   = modalidad;
        this.nivel       = nivel;
        this.acreditado  = acreditado;
        this.activo      = true;
    }

    // ── Getters y Setters ─────────────────────────────────────────────────────

    public int            getId()            { return id; }
    public void           setId(int id)      { this.id = id; }

    public String         getNombre()        { return nombre; }
    public void           setNombre(String n){ this.nombre = n; }

    public String         getCodigoSnies()             { return codigoSnies; }
    public void           setCodigoSnies(String cs)    { this.codigoSnies = cs; }

    public String         getFacultad()               { return facultad; }
    public void           setFacultad(String f)        { this.facultad = f; }

    public String         getModalidad()              { return modalidad; }
    public void           setModalidad(String m)       { this.modalidad = m; }

    public String         getNivel()                  { return nivel; }
    public void           setNivel(String n)           { this.nivel = n; }

    public boolean        isAcreditado()              { return acreditado; }
    public void           setAcreditado(boolean a)    { this.acreditado = a; }

    public LocalDateTime  getFechaRegistro()          { return fechaRegistro; }
    public void           setFechaRegistro(LocalDateTime f) { this.fechaRegistro = f; }

    public boolean        isActivo()                  { return activo; }
    public void           setActivo(boolean a)        { this.activo = a; }

    @Override
    public String toString() { return nombre; }
}

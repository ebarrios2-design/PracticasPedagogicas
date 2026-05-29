package practicaspedagogicas.modelo;

import java.time.LocalDateTime;

/**
 * Modelo que representa un grupo de estudiantes en una práctica.
 * Corresponde a la tabla 'grupo'.
 *
 * @version 1.0
 */
public class Grupo {

    private int           id;
    private int           idPractica;
    private int           idInstitucion;
    private String        nombre;
    private int           cupoMaximo;
    private String        jornada;          // Manana|Tarde|Noche|Completa
    private String        observaciones;
    private LocalDateTime fechaCreacion;
    private boolean       activo;

    // Atributos de conveniencia (JOIN)
    private String nombrePractica;
    private String nombreInstitucion;
    private int    estudiantesInscritos;    // conteo en tiempo real

    // ── Constructores ─────────────────────────────────────────────────────────

    public Grupo() {}

    public Grupo(int idPractica, int idInstitucion, String nombre,
                 int cupoMaximo, String jornada) {
        this.idPractica   = idPractica;
        this.idInstitucion = idInstitucion;
        this.nombre       = nombre;
        this.cupoMaximo   = cupoMaximo;
        this.jornada      = jornada;
        this.activo       = true;
    }

    // ── Getters y Setters ─────────────────────────────────────────────────────

    public int           getId()                  { return id; }
    public void          setId(int id)            { this.id = id; }

    public int           getIdPractica()          { return idPractica; }
    public void          setIdPractica(int i)     { this.idPractica = i; }

    public int           getIdInstitucion()       { return idInstitucion; }
    public void          setIdInstitucion(int i)  { this.idInstitucion = i; }

    public String        getNombre()              { return nombre; }
    public void          setNombre(String n)      { this.nombre = n; }

    public int           getCupoMaximo()          { return cupoMaximo; }
    public void          setCupoMaximo(int c)     { this.cupoMaximo = c; }

    public String        getJornada()             { return jornada; }
    public void          setJornada(String j)     { this.jornada = j; }

    public String        getObservaciones()       { return observaciones; }
    public void          setObservaciones(String o){ this.observaciones = o; }

    public LocalDateTime getFechaCreacion()       { return fechaCreacion; }
    public void          setFechaCreacion(LocalDateTime f){ this.fechaCreacion = f; }

    public boolean       isActivo()               { return activo; }
    public void          setActivo(boolean a)     { this.activo = a; }

    public String        getNombrePractica()      { return nombrePractica; }
    public void          setNombrePractica(String n){ this.nombrePractica = n; }

    public String        getNombreInstitucion()   { return nombreInstitucion; }
    public void          setNombreInstitucion(String n){ this.nombreInstitucion = n; }

    public int           getEstudiantesInscritos()     { return estudiantesInscritos; }
    public void          setEstudiantesInscritos(int e){ this.estudiantesInscritos = e; }

    /** @return Cupos disponibles (cupoMaximo - estudiantesInscritos). */
    public int getCuposDisponibles() {
        return cupoMaximo - estudiantesInscritos;
    }

    @Override
    public String toString() { return nombre; }
}

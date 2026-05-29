package practicaspedagogicas.modelo;

import java.time.LocalDateTime;

/**
 * Modelo que representa un usuario del sistema.
 * Corresponde a la tabla 'usuario'.
 * Los roles posibles son: Director, Coordinador, Docente, Estudiante, Institucion.
 *
 * @version 1.0
 */
public class Usuario {

    private int           id;
    private Integer       idPrograma;       // nullable
    private String        nombres;
    private String        apellidos;
    private String        tipoDocumento;    // CC, CE, TI, Pasaporte
    private String        numeroDocumento;
    private String        correo;
    private String        contrasenaHash;   // SHA-256, nunca texto plano
    private String        rol;              // Director|Coordinador|Docente|Estudiante|Institucion
    private String        telefono;
    private LocalDateTime fechaCreacion;
    private LocalDateTime ultimoAcceso;
    private boolean       activo;

    // ── Constructores ─────────────────────────────────────────────────────────

    public Usuario() {}

    public Usuario(Integer idPrograma, String nombres, String apellidos,
                   String tipoDocumento, String numeroDocumento,
                   String correo, String contrasenaHash, String rol) {
        this.idPrograma      = idPrograma;
        this.nombres         = nombres;
        this.apellidos       = apellidos;
        this.tipoDocumento   = tipoDocumento;
        this.numeroDocumento = numeroDocumento;
        this.correo          = correo;
        this.contrasenaHash  = contrasenaHash;
        this.rol             = rol;
        this.activo          = true;
    }

    // ── Métodos de utilidad ───────────────────────────────────────────────────

    /** @return Nombre completo: nombres + apellidos. */
    public String getNombreCompleto() {
        return nombres + " " + apellidos;
    }

    // ── Getters y Setters ─────────────────────────────────────────────────────

    public int           getId()                    { return id; }
    public void          setId(int id)              { this.id = id; }

    public Integer       getIdPrograma()            { return idPrograma; }
    public void          setIdPrograma(Integer ip)  { this.idPrograma = ip; }

    public String        getNombres()               { return nombres; }
    public void          setNombres(String n)       { this.nombres = n; }

    public String        getApellidos()             { return apellidos; }
    public void          setApellidos(String a)     { this.apellidos = a; }

    public String        getTipoDocumento()         { return tipoDocumento; }
    public void          setTipoDocumento(String t) { this.tipoDocumento = t; }

    public String        getNumeroDocumento()       { return numeroDocumento; }
    public void          setNumeroDocumento(String n){ this.numeroDocumento = n; }

    public String        getCorreo()                { return correo; }
    public void          setCorreo(String c)        { this.correo = c; }

    public String        getContrasenaHash()        { return contrasenaHash; }
    public void          setContrasenaHash(String h){ this.contrasenaHash = h; }

    public String        getRol()                   { return rol; }
    public void          setRol(String r)           { this.rol = r; }

    public String        getTelefono()              { return telefono; }
    public void          setTelefono(String t)      { this.telefono = t; }

    public LocalDateTime getFechaCreacion()         { return fechaCreacion; }
    public void          setFechaCreacion(LocalDateTime f) { this.fechaCreacion = f; }

    public LocalDateTime getUltimoAcceso()          { return ultimoAcceso; }
    public void          setUltimoAcceso(LocalDateTime u)  { this.ultimoAcceso = u; }

    public boolean       isActivo()                 { return activo; }
    public void          setActivo(boolean a)       { this.activo = a; }

    @Override
    public String toString() { return getNombreCompleto() + " [" + rol + "]"; }
}

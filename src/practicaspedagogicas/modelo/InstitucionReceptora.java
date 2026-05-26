package practicaspedagogicas.modelo;

/**
 * Modelo que representa una institución educativa receptora.
 * Corresponde a la tabla 'institucion_receptora'.
 *
 * @version 1.0
 */
public class InstitucionReceptora {

    private int     id;
    private String  nombre;
    private String  nit;
    private String  dane;
    private String  direccion;
    private String  municipio;
    private String  departamento;
    private String  zona;             // Urbana | Rural
    private String  nivelEducativo;   // SET: Preescolar,Primaria,Secundaria,Media
    private String  nombreRector;
    private String  correoContacto;
    private String  telefono;
    private boolean activo;

    // ── Constructores ─────────────────────────────────────────────────────────

    public InstitucionReceptora() {}

    public InstitucionReceptora(String nombre, String nit, String direccion,
                                String municipio, String departamento,
                                String zona, String nivelEducativo) {
        this.nombre         = nombre;
        this.nit            = nit;
        this.direccion      = direccion;
        this.municipio      = municipio;
        this.departamento   = departamento;
        this.zona           = zona;
        this.nivelEducativo = nivelEducativo;
        this.activo         = true;
    }

    // ── Getters y Setters ─────────────────────────────────────────────────────

    public int     getId()                      { return id; }
    public void    setId(int id)                { this.id = id; }

    public String  getNombre()                  { return nombre; }
    public void    setNombre(String n)          { this.nombre = n; }

    public String  getNit()                     { return nit; }
    public void    setNit(String n)             { this.nit = n; }

    public String  getDane()                    { return dane; }
    public void    setDane(String d)            { this.dane = d; }

    public String  getDireccion()               { return direccion; }
    public void    setDireccion(String d)       { this.direccion = d; }

    public String  getMunicipio()               { return municipio; }
    public void    setMunicipio(String m)       { this.municipio = m; }

    public String  getDepartamento()            { return departamento; }
    public void    setDepartamento(String d)    { this.departamento = d; }

    public String  getZona()                    { return zona; }
    public void    setZona(String z)            { this.zona = z; }

    public String  getNivelEducativo()          { return nivelEducativo; }
    public void    setNivelEducativo(String n)  { this.nivelEducativo = n; }

    public String  getNombreRector()            { return nombreRector; }
    public void    setNombreRector(String n)    { this.nombreRector = n; }

    public String  getCorreoContacto()          { return correoContacto; }
    public void    setCorreoContacto(String c)  { this.correoContacto = c; }

    public String  getTelefono()                { return telefono; }
    public void    setTelefono(String t)        { this.telefono = t; }

    public boolean isActivo()                   { return activo; }
    public void    setActivo(boolean a)         { this.activo = a; }

    @Override
    public String toString() { return nombre + " (" + municipio + ")"; }
}

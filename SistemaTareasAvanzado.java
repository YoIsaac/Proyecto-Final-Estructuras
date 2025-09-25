import java.util.*;
import java.io.*;

/**
 * Proyecto Final - Sistema de Gestion de Tareas Avanzado
 * Empresa: "Tareas  (antes eran "Tareitas", ahora ya evolucionamos a corporativo jeje)
 *
 * Aqui se juntan: colas de prioridad, arboles binarios, grafos, hashmaps,
 * ordenamientos, busquedas, recursividad y un poco de magia
 *
 * Notas:
 * - Mantengo el diseño original pero le agrego funciones que faltaban:
 *   guardar/cargar, marcar completada, eliminar, listar todas, etc.
 * - Comentarios estilo cotorro (pa que no se haga tan denso).
 */
public class SistemaTareasAvanzado {

    // ====== Clase Tarea ======
    // Representa una tarea con id autoincremental, titulo, depto, urgencia, fecha y estado.
    static class Tarea implements Serializable {
        private static final long serialVersionUID = 1L;
        private static int NEXT_ID = 1;
        int id;
        String titulo;
        String departamento;
        int urgencia; // 1-5
        Date fechaEntrega;
        boolean completada;

        Tarea(String titulo, String depto, int urgencia, Date fecha) {
            this.id = NEXT_ID++;
            this.titulo = titulo;
            this.departamento = depto;
            this.urgencia = urgencia;
            this.fechaEntrega = fecha;
            this.completada = false;
        }

        void marcarCompletada() { this.completada = true; }

        @Override
        public String toString() {
            return "[" + id + "] " + titulo + " (Dept: " + departamento +
                   ", urg: " + urgencia + ", fecha: " + fechaEntrega +
                   ", estado: " + (completada ? "✔ done" : "pendiente") + ")";
        }
    }

    // Cola de prioridad (urgencia + fecha) 
    // Usada para seleccionar primero lo mas urgente y si hay empate, lo con fecha mas cercana.
    static class ColaPrioridad {
        PriorityQueue<Tarea> pq = new PriorityQueue<>((a, b) -> {
            int cmp = Integer.compare(b.urgencia, a.urgencia);
            if (cmp == 0) return a.fechaEntrega.compareTo(b.fechaEntrega);
            return cmp;
        });

        void add(Tarea t) { pq.offer(t); }
        Tarea poll() { return pq.poll(); }
        boolean isEmpty() { return pq.isEmpty(); }
        List<Tarea> toList() { return new ArrayList<>(pq); }
    }

    // ====== Árbol binario para empleados ======
    // Sirve para organizar empleados por nombre y buscarlos rapido (BST).
    static class NodoEmpleado {
        String nombre;
        String departamento;
        NodoEmpleado izq, der;

        NodoEmpleado(String n, String d) {
            nombre = n;
            departamento = d;
        }
    }

    static class ArbolEmpleados {
        NodoEmpleado raiz;

        void insertar(String nombre, String depto) {
            raiz = insertarRec(raiz, nombre, depto);
        }

        private NodoEmpleado insertarRec(NodoEmpleado actual, String nombre, String depto) {
            if (actual == null) return new NodoEmpleado(nombre, depto);
            if (nombre.compareToIgnoreCase(actual.nombre) < 0)
                actual.izq = insertarRec(actual.izq, nombre, depto);
            else
                actual.der = insertarRec(actual.der, nombre, depto);
            return actual;
        }

        boolean buscar(String nombre) {
            return buscarRec(raiz, nombre);
        }

        private boolean buscarRec(NodoEmpleado actual, String nombre) {
            if (actual == null) return false;
            if (actual.nombre.equalsIgnoreCase(nombre)) return true;
            return nombre.compareToIgnoreCase(actual.nombre) < 0
                    ? buscarRec(actual.izq, nombre)
                    : buscarRec(actual.der, nombre);
        }

        void mostrarInOrder() { mostrarRec(raiz); }
        private void mostrarRec(NodoEmpleado n) {
            if (n != null) {
                mostrarRec(n.izq);
                System.out.println("Empleado: " + n.nombre + " (Depto: " + n.departamento + ")");
                mostrarRec(n.der);
            }
        }
    }

    // ====== HashMap para tareas y empleados ======
    static class BaseDatos implements Serializable {
        private static final long serialVersionUID = 1L;
        HashMap<Integer, Tarea> tareas = new HashMap<>();
        HashMap<String, String> empleados = new HashMap<>();

        void addTarea(Tarea t) { tareas.put(t.id, t); }
        void addEmpleado(String nombre, String depto) { empleados.put(nombre, depto); }

        Tarea getTarea(int id) { return tareas.get(id); }
        String getEmpleado(String nombre) { return empleados.get(nombre); }

        void mostrarTareas() { 
            if (tareas.isEmpty()) {
                System.out.println("No hay tareas en la base de datos.");
                return;
            }
            tareas.values().forEach(System.out::println); 
        }
    }

    // ====== Gestor de ordenamiento y búsqueda ======
    // Aqui aplicamos "divide y vencerás" con búsqueda binaria y ordenamientos.
    static class GestorOrdenamiento {
        List<Tarea> lista;

        GestorOrdenamiento(Collection<Tarea> tareas) {
            lista = new ArrayList<>(tareas);
        }

        void ordenarPorUrgencia() {
            // Orden descendente por urgencia (5 -> 1)
            lista.sort((a, b) -> Integer.compare(b.urgencia, a.urgencia));
        }

        void ordenarPorFecha() {
            // Orden ascendente por fecha (antes primero)
            lista.sort(Comparator.comparing(t -> t.fechaEntrega));
        }

        // búsqueda binaria por id (divide y vencerás)
        Tarea buscarPorID(int id) {
            lista.sort(Comparator.comparingInt(t -> t.id));
            int izq = 0, der = lista.size() - 1;
            while (izq <= der) {
                int mid = (izq + der) / 2;
                if (lista.get(mid).id == id) return lista.get(mid);
                if (lista.get(mid).id < id) izq = mid + 1;
                else der = mid - 1;
            }
            return null;
        }

        void mostrar() { 
            if (lista.isEmpty()) {
                System.out.println("lista vacia.");
                return;
            }
            lista.forEach(System.out::println); 
        }
    }

    // ====== Grafo de dependencias ======
    // Representa dependencias: si A depende de B, entonces no hacer A hasta B.
    static class GrafoDependencias implements Serializable {
        private static final long serialVersionUID = 1L;
        Map<Integer, List<Integer>> adj = new HashMap<>();

        void addDep(int tareaA, int tareaB) {
            adj.computeIfAbsent(tareaA, k -> new ArrayList<>()).add(tareaB);
        }

        void mostrar(Map<Integer, Tarea> tareas) {
            if (adj.isEmpty()) {
                System.out.println("No hay dependencias registradas.");
                return;
            }
            for (Map.Entry<Integer, List<Integer>> entry : adj.entrySet()) {
                Tarea origenT = tareas.get(entry.getKey());
                String origen = origenT != null ? origenT.titulo : ("ID " + entry.getKey());
                for (int dep : entry.getValue()) {
                    Tarea destinoT = tareas.get(dep);
                    String destino = destinoT != null ? destinoT.titulo : ("ID " + dep);
                    System.out.println("La tarea [" + origen + "] depende de [" + destino + "]");
                }
            }
        }

        // util: obtener lista de dependencias de una tarea
        List<Integer> getDeps(int tareaId) {
            return adj.getOrDefault(tareaId, Collections.emptyList());
        }
    }

    // ====== Variables globales ======
    static ColaPrioridad cola = new ColaPrioridad();
    static ArbolEmpleados arbol = new ArbolEmpleados();
    static BaseDatos db = new BaseDatos();
    static GrafoDependencias grafo = new GrafoDependencias();
    static Scanner sc = new Scanner(System.in);

    // persistencia: archivo para guardar base de datos y grafo
    static final String ARCHIVO_DB = "tareas_db.ser";
    static final String ARCHIVO_GRAFO = "tareas_grafo.ser";

    // ====== MAIN ======
    public static void main(String[] args) {
        // al arrancar intentamos cargar estado previo si existe (persistencia)
        cargarEstado();

        // seed demo (si no hay nada cargado, esto mete ejemplos)
        if (db.tareas.isEmpty()) seedDemo();

        boolean salir = false;
        while (!salir) {
            showMenu();
            String opt = sc.nextLine().trim();
            switch (opt) {
                case "1": addTask(); break;                  // agregar tarea (varias estructuras)
                case "2": verTareas(); break;                // ver cola prioridad
                case "3": ordenarYBuscar(); break;           // ordenamiento / busqueda (divide y venceras)
                case "4": empleadosMenu(); break;            // arbol empleados (insert/buscar/mostrar)
                case "5": hashMenu(); break;                 // hashmap: mostrar todas las tareas, buscar
                case "6": dependenciasMenu(); break;         // grafo: ver/añadir dependencias
                case "7": marcarTareaCompletada(); break;    // marcar completada (nueva)
                case "8": eliminarTarea(); break;            // eliminar tarea (nueva)
                case "9": listarTodasLasTareas(); break;     // listar todo (nueva)
                case "10": guardarEstado(); break;           // guardar manual (nueva)
                case "0":                                      // salir (guarda antes)
                    guardarEstado();
                    salir = true;
                    break;
                default: System.out.println("Opcion invalida\n");
            }
        }
        System.out.println("Gracias por usar Tareas SA de CV... vuelve pronto a sufrir con mas chambas jeje");
    }

    // ====== Menús ======
    static void showMenu() {
        System.out.println("\n--- Sistema Avanzado de Tareas ---");
        System.out.println("1) Agregar tarea");
        System.out.println("2) Ver tareas (cola prioridad)");
        System.out.println("3) Ordenar y buscar tareas");
        System.out.println("4) Gestion de empleados (arbol)");
        System.out.println("5) HashMap (acceso rapido)");
        System.out.println("6) Dependencias de tareas (grafo)");
        System.out.println("7) Marcar tarea como completada");
        System.out.println("8) Eliminar tarea");
        System.out.println("9) Listar todas las tareas (base de datos)");
        System.out.println("10) Guardar manualmente (persistencia)");
        System.out.println("0) Salir");
        System.out.print("Opcion: ");
    }

    // ====== Funciones de gestion de tareas
    static void addTask() {
        System.out.print("Titulo: ");
        String tit = sc.nextLine();
        System.out.print("Depto: ");
        String depto = sc.nextLine();
        int urg = readInt("Urgencia (1-5): ", 1, 5);
        // generamos fecha aproximada sumando dias segun urgencia (demo)
        Date fecha = new Date(System.currentTimeMillis() + urg * 86400000L);

        Tarea t = new Tarea(tit, depto, urg, fecha);
        // lo metemos en cola prioridad y en la base de datos hash
        cola.add(t);
        db.addTarea(t);

        System.out.println("Tarea agregada: " + t);

        // opcional: preguntar dependencias al crear
        System.out.print("¿Tiene dependencias? (s/n): ");
        String ans = sc.nextLine().trim();
        if (ans.equalsIgnoreCase("s")) {
            System.out.print("Introduce ID(s) separadas por comas (ej: 1,2): ");
            String line = sc.nextLine().trim();
            String[] parts = line.split(",");
            for (String p : parts) {
                try {
                    int depId = Integer.parseInt(p.trim());
                    if (db.getTarea(depId) != null) grafo.addDep(t.id, depId);
                } catch (Exception e) { /* skip invalid */ }
            }
            System.out.println("Dependencias registradas si los ID existian.");
        }
    }

    static void verTareas() {
        if (cola.isEmpty()) {
            System.out.println("No hay tareas");
            return;
        }
        System.out.println("\n--- Cola de prioridad ---");
        for (Tarea t : cola.toList()) System.out.println(t);
    }

    static void ordenarYBuscar() {
        GestorOrdenamiento gestor = new GestorOrdenamiento(db.tareas.values());
        System.out.println("1) Ordenar por urgencia\n2) Ordenar por fecha\n3) Buscar por ID");
        String o = sc.nextLine().trim();
        switch (o) {
            case "1": gestor.ordenarPorUrgencia(); gestor.mostrar(); break;
            case "2": gestor.ordenarPorFecha(); gestor.mostrar(); break;
            case "3":
                int id = readInt("ID a buscar: ", 1, Integer.MAX_VALUE);
                Tarea t = gestor.buscarPorID(id);
                System.out.println(t == null ? "No encontrada" : t);
                break;
            default: System.out.println("Opcion invalida");
        }
    }

    static void empleadosMenu() {
        System.out.println("1) Mostrar empleados\n2) Buscar empleado\n3) Agregar empleado");
        String o = sc.nextLine().trim();
        if (o.equals("1")) arbol.mostrarInOrder();
        else if (o.equals("2")) {
            System.out.print("Nombre: ");
            String n = sc.nextLine();
            System.out.println(arbol.buscar(n) ? "Encontrado" : "No existe");
        } else if (o.equals("3")) {
            System.out.print("Nombre empleado: ");
            String n = sc.nextLine();
            System.out.print("Departamento: ");
            String d = sc.nextLine();
            arbol.insertar(n, d);
            db.addEmpleado(n, d);
            System.out.println("Empleado agregado: " + n + " (" + d + ")");
        } else System.out.println("Opcion invalida");
    }

    static void hashMenu() {
        System.out.println("--- HashMap: Todas las tareas ---");
        db.mostrarTareas();
        System.out.println("\n--- Buscar tarea por ID (HashMap) ---");
        System.out.print("Deseas buscar por ID? (s/n): ");
        String s = sc.nextLine().trim();
        if (s.equalsIgnoreCase("s")) {
            int id = readInt("ID: ", 1, Integer.MAX_VALUE);
            Tarea t = db.getTarea(id);
            System.out.println(t == null ? "No existe esa tarea" : t);
        }
    }

    static void dependenciasMenu() {
        System.out.println("1) Mostrar dependencias\n2) Agregar dependencia");
        String o = sc.nextLine().trim();
        if (o.equals("1")) {
            grafo.mostrar(db.tareas);
        } else if (o.equals("2")) {
            int origen = readInt("ID tarea origen: ", 1, Integer.MAX_VALUE);
            int destino = readInt("ID tarea de la que depende: ", 1, Integer.MAX_VALUE);
            if (db.getTarea(origen) != null && db.getTarea(destino) != null) {
                grafo.addDep(origen, destino);
                System.out.println("Dependencia registrada.");
            } else System.out.println("Alguno de los ID no existe.");
        } else System.out.println("Opcion invalida");
    }

    // FUNCIONES NUEVAS SOLICITADAS 

    // marcar tarea completada por ID (busca en la BaseDatos)
    static void marcarTareaCompletada() {
        int id = readInt("ID de la tarea a marcar completada: ", 1, Integer.MAX_VALUE);
        Tarea t = db.getTarea(id);
        if (t == null) System.out.println("No existe tarea con ese ID.");
        else {
            t.marcarCompletada();
            System.out.println("Tarea marcada como completada: " + t);
        }
    }

    // eliminar tarea: la quitamos del hashmap y la cola (si aparece), y del grafo (limpiamos deps)
    static void eliminarTarea() {
        int id = readInt("ID de la tarea a eliminar: ", 1, Integer.MAX_VALUE);
        Tarea t = db.getTarea(id);
        if (t == null) { System.out.println("No existe tarea con ese ID."); return; }
        // quitar del hashmap
        db.tareas.remove(id);
        // quitar de la cola: reconstruimos pq sin esa tarea
        List<Tarea> tmp = cola.toList();
        tmp.removeIf(x -> x.id == id);
        cola = new ColaPrioridad();
        for (Tarea x : tmp) cola.add(x);
        // quitar dependencias que apuntan o salen de la tarea
        // removemos la entrada y tambien borramos referencias en listas
        grafo.adj.remove(id);
        for (List<Integer> deps : grafo.adj.values()) deps.removeIf(d -> d == id);
        System.out.println("Tarea eliminada de todas las estructuras: " + t.titulo);
    }

    // listar todas las tareas que hay en la base de datos (hashmap)
    static void listarTodasLasTareas() {
        System.out.println("--- Todas las tareas (BaseDatos) ---");
        db.mostrarTareas();
    }

    // ====== PERSISTENCIA: guardar y cargar estado (db + grafo) ======

    // guarda estado a disco (db y grafo)
    static void guardarEstado() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(ARCHIVO_DB))) {
            oos.writeObject(db);
            System.out.println("Base de datos guardada: " + ARCHIVO_DB);
        } catch (Exception e) {
            System.out.println("Error guardando DB: " + e.getMessage());
        }
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(ARCHIVO_GRAFO))) {
            oos.writeObject(grafo);
            System.out.println("Grafo guardado: " + ARCHIVO_GRAFO);
        } catch (Exception e) {
            System.out.println("Error guardando grafo: " + e.getMessage());
        }
    }

    // carga estado de disco (si existe)
    static void cargarEstado() {
        File fdb = new File(ARCHIVO_DB);
        if (fdb.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(fdb))) {
                BaseDatos loaded = (BaseDatos) ois.readObject();
                if (loaded != null) {
                    db = loaded;
                    // reconstruir cola desde db (para mantener consistencia)
                    cola = new ColaPrioridad();
                    for (Tarea t : db.tareas.values()) cola.add(t);
                    System.out.println("Base de datos cargada con " + db.tareas.size() + " tareas.");
                }
            } catch (Exception e) {
                System.out.println("Error cargando DB: " + e.getMessage());
            }
        }
        File fg = new File(ARCHIVO_GRAFO);
        if (fg.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(fg))) {
                GrafoDependencias loaded = (GrafoDependencias) ois.readObject();
                if (loaded != null) {
                    grafo = loaded;
                    System.out.println("Grafo de dependencias cargado.");
                }
            } catch (Exception e) {
                System.out.println("Error cargando grafo: " + e.getMessage());
            }
        }
    }

    // ====== Utils ======
    static int readInt(String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            try {
                int v = Integer.parseInt(sc.nextLine().trim());
                if (v >= min && v <= max) return v;
            } catch (Exception e) { }
            System.out.println("Numero invalido");
        }
    }

    // ====== Seed demo (ejemplos iniciales) ======
    static void seedDemo() {
        Tarea t1 = new Tarea("Deploy servidor", "TI", 5, new Date(System.currentTimeMillis() + 2*86400000L));
        Tarea t2 = new Tarea("Disenar logo", "Marketing", 2, new Date(System.currentTimeMillis() + 10*86400000L));
        Tarea t3 = new Tarea("Auditoria interna", "Finanzas", 4, new Date(System.currentTimeMillis() + 5*86400000L));

        cola.add(t1); cola.add(t2); cola.add(t3);
        db.addTarea(t1); db.addTarea(t2); db.addTarea(t3);

        arbol.insertar("Luis", "TI");
        arbol.insertar("Ana", "Marketing");
        arbol.insertar("Pedro", "Finanzas");

        db.addEmpleado("Luis", "TI");
        db.addEmpleado("Ana", "Marketing");
        db.addEmpleado("Pedro", "Finanzas");

        grafo.addDep(t1.id, t3.id); // deploy depende de auditoria
    }
}

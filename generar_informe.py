from pathlib import Path
from reportlab.lib import colors
from reportlab.lib.enums import TA_CENTER
from reportlab.lib.pagesizes import letter
from reportlab.lib.styles import ParagraphStyle, getSampleStyleSheet
from reportlab.lib.units import inch
from reportlab.lib.utils import ImageReader
from reportlab.platypus import SimpleDocTemplate, Paragraph, Spacer, PageBreak, Preformatted, Table, TableStyle, Image, KeepTogether

ROOT = Path(__file__).parent
OUT = ROOT / "output" / "pdf" / "Taller7_ExcepcionesEnumeraciones.pdf"
OUT.parent.mkdir(parents=True, exist_ok=True)
ANCHO_CAPTURA = 5.3 * inch

styles = getSampleStyleSheet()
styles.add(ParagraphStyle(name="TitleCenter", parent=styles["Title"], alignment=TA_CENTER, textColor=colors.HexColor("#17365D"), spaceAfter=16))
styles.add(ParagraphStyle(name="Section", parent=styles["Heading2"], textColor=colors.HexColor("#17365D"), spaceBefore=10, spaceAfter=7))
styles.add(ParagraphStyle(name="Subsection", parent=styles["Heading3"], textColor=colors.HexColor("#17365D"), spaceBefore=8, spaceAfter=5))
styles.add(ParagraphStyle(name="BodySmall", parent=styles["BodyText"], leading=14, spaceAfter=7))
styles.add(ParagraphStyle(name="Cell", parent=styles["BodyText"], fontSize=9, leading=12))
styles.add(ParagraphStyle(name="SourceCode", fontName="Courier", fontSize=7.2, leading=9, backColor=colors.HexColor("#F3F6F9"), borderColor=colors.HexColor("#B8C8D8"), borderWidth=.4, borderPadding=7, spaceAfter=8))
styles.add(ParagraphStyle(name="Console", fontName="Courier", fontSize=8.2, leading=11, textColor=colors.HexColor("#183A2A"), backColor=colors.HexColor("#E8F5EC"), borderColor=colors.HexColor("#8EB99A"), borderWidth=.5, borderPadding=10, spaceAfter=10))


def p(text, style="BodySmall"):
    return Paragraph(text, styles[style])


def c(identificador):
    return f'<font face="Courier">{identificador}</font>'


def code(archivo):
    fuente = Preformatted((ROOT / "src" / "solicitudes" / archivo).read_text(encoding="utf-8"), styles["SourceCode"], maxLineLength=115, newLineChars="        ")
    return KeepTogether([p(f"<b>{archivo}</b>"), fuente])


def captura(archivo, titulo, descripcion):
    ruta = str(ROOT / "capturas" / f"{archivo}.png")
    ancho, alto = ImageReader(ruta).getSize()
    return KeepTogether([p(titulo, "Subsection"), Image(ruta, width=ANCHO_CAPTURA, height=ANCHO_CAPTURA * alto / ancho), Spacer(1, 4), p(descripcion)])


def tabla(encabezados, filas, anchos, *antes):
    data = [[p(f"<b>{e}</b>", "Cell") for e in encabezados]] + [[p(celda, "Cell") for celda in fila] for fila in filas]
    table = Table(data, colWidths=[a * inch for a in anchos], repeatRows=1)
    table.setStyle(TableStyle([
        ("BACKGROUND", (0, 0), (-1, 0), colors.HexColor("#D9EAF7")),
        ("GRID", (0, 0), (-1, -1), .35, colors.HexColor("#A9B9C8")),
        ("VALIGN", (0, 0), (-1, -1), "TOP"),
        ("LEFTPADDING", (0, 0), (-1, -1), 6),
        ("RIGHTPADDING", (0, 0), (-1, -1), 6),
        ("TOPPADDING", (0, 0), (-1, -1), 4),
        ("BOTTOMPADDING", (0, 0), (-1, -1), 4),
    ]))
    return KeepTogether([*antes, table, Spacer(1, 10)])


FUENTES = ["TipoSolicitud.java", "EstadoSolicitud.java", "SolicitudInvalidaException.java", "SolicitudDuplicadaException.java",
           "Solicitud.java", "RegistroSolicitudes.java", "VentanaSolicitudes.java"]

story = [
    Spacer(1, .2 * inch),
    p("Taller 7 - Programación Orientada a Objetos", "TitleCenter"),
    p("<b>Excepciones y enumeraciones en Java</b>", "TitleCenter"),
    Spacer(1, .15 * inch),
    p("Este documento presenta la solución del taller: un sistema de registro de solicitudes académicas con interfaz gráfica en Java Swing. "
      "Incluye las enumeraciones, las excepciones personalizadas, el diseño de las clases, la solución de los retos, "
      "las evidencias de funcionamiento con registros válidos e inválidos y el código fuente completo."),
    p(f"Todas las clases están en el paquete {c('solicitudes')}. <b>Compilación y ejecución</b> desde la carpeta del proyecto "
      f"(o abrir la carpeta en IntelliJ IDEA y ejecutar {c('VentanaSolicitudes')}):"),
    Preformatted("javac -encoding UTF-8 -d out/production/taller7 src/solicitudes/*.java\n"
                 "java -cp out/production/taller7 solicitudes.VentanaSolicitudes", styles["Console"]),

    tabla(["Archivo", "Tipo", "Responsabilidad"], [
        (c("TipoSolicitud"), "Enumeración", f"Tipos permitidos: {c('CERTIFICADO')}, {c('CANCELACION')}, {c('HOMOLOGACION')} y {c('OTRO')}. "
                                            f"Cada constante guarda un nombre legible (\"Homologación\") que devuelve {c('toString()')}; así el "
                                            f"{c('JComboBox')} y la tabla muestran texto con tildes sin escribir cada opción a mano."),
        (c("EstadoSolicitud"), "Enumeración", f"Estados permitidos: {c('PENDIENTE')}, {c('EN_PROCESO')}, {c('APROBADA')} y {c('RECHAZADA')}. "
                                              f"El método {c('puedeCambiarA()')} define con un {c('switch')} sobre el enum el flujo válido: "
                                              f"{c('PENDIENTE')} -&gt; {c('EN_PROCESO')} -&gt; {c('APROBADA')} o {c('RECHAZADA')}."),
        (c("SolicitudInvalidaException"), f"Excepción comprobada ({c('extends Exception')})", "Datos que no cumplen las reglas del sistema y cambios de estado no permitidos."),
        (c("SolicitudDuplicadaException"), f"Excepción comprobada ({c('extends Exception')})", "Reto 5: intento de registrar una solicitud con el mismo código estudiantil y tipo que otra ya registrada."),
        (c("Solicitud"), "Clase", f"Representa una solicitud. Valida sus datos en el constructor: si alguno es inválido lanza "
                                  f"{c('SolicitudInvalidaException')} y el objeto no se crea. Toda solicitud nueva empieza en {c('PENDIENTE')}."),
        (c("RegistroSolicitudes"), f"Clase ({c('extends AbstractTableModel')})", f"Guarda la lista de solicitudes, impide duplicados y cambia estados. "
                                                                                 f"Es también el modelo de la {c('JTable')}: cada cambio avisa a la tabla para que se actualice sola."),
        (c("VentanaSolicitudes"), f"Clase ({c('extends JFrame')})", f"Interfaz gráfica y método {c('main')}. Lee el formulario, crea la solicitud, captura las excepciones y las muestra con {c('JOptionPane')}."),
    ], [2.2, 1.15, 3.71], p("1. Clases y enumeraciones", "Section")),

    p("2. Enumeraciones", "Section"),
    p("<b>Pregunta: ¿qué ventajas tiene utilizar estas enumeraciones en lugar de guardar los valores como cadenas de texto?</b>"),
    p(f"1. <b>Solo existen valores válidos.</b> Una variable {c('EstadoSolicitud')} únicamente puede tomar los cuatro valores declarados. "
      f"Con un {c('String')} se podría guardar \"aprobado\", \"Aprovada\" o \"finalizado\" y el error aparecería tarde, en ejecución. "
      f"Con el enum, escribir {c('EstadoSolicitud.APROVADA')} ni siquiera compila."),
    p(f"2. <b>Menos validaciones.</b> No hace falta comprobar mayúsculas, espacios ni errores de escritura: el compilador lo garantiza. "
      f"En la interfaz, {c('new JComboBox&lt;&gt;(TipoSolicitud.values())')} ofrece exactamente las opciones válidas y el usuario no puede escribir otra."),
    p(f"3. <b>Comparaciones seguras.</b> Los valores se comparan con {c('==')} (no con {c('equals')} ni ignorando mayúsculas) y se pueden usar en "
      f"{c('switch')}. En {c('puedeCambiarA()')} el compilador verifica que el {c('switch')} cubra todos los estados."),
    p(f"4. <b>Datos y comportamiento propios.</b> Un enum es una clase: {c('TipoSolicitud')} guarda el nombre que se muestra al usuario y "
      f"{c('EstadoSolicitud')} sabe a qué estados puede pasar. Esa regla queda en un solo lugar."),
    p(f"5. <b>Mantenimiento.</b> Para agregar un tipo de solicitud basta con añadir una constante; el combo se llena solo. "
      f"El IDE autocompleta los valores y permite renombrarlos en todo el proyecto sin buscar textos."),

    p("3. Manejo de excepciones", "Section"),
    tabla(["Dónde", "Qué se hace"], [
        (f"{c('throw')} en el constructor de {c('Solicitud')}", f"Lanza {c('SolicitudInvalidaException')} si el nombre está vacío, el código no tiene entre 5 y 12 dígitos "
                                                                 f"({c('codigo.matches(\"\\\\d{5,12}\")')}), falta el tipo o la descripción está vacía."),
        (f"{c('throw')} en {c('Solicitud.cambiarEstado()')}", f"Lanza {c('SolicitudInvalidaException')} si {c('estado.puedeCambiarA(nuevoEstado)')} es falso, por ejemplo de {c('PENDIENTE')} a {c('APROBADA')}."),
        (f"{c('throw')} en {c('RegistroSolicitudes.registrar()')}", f"Lanza {c('SolicitudDuplicadaException')} si ya hay una solicitud con el mismo código y tipo."),
        (c("throws"), f"El constructor de {c('Solicitud')}, {c('cambiarEstado()')} y {c('registrar()')} declaran la excepción con {c('throws')}: "
                      "como son excepciones comprobadas, el compilador obliga a quien las invoca a capturarlas o propagarlas."),
        (f"{c('try-catch')} en {c('registrarSolicitud()')}", f"Dos bloques {c('catch')}: {c('SolicitudInvalidaException')} muestra \"Error de validación\" y "
                                                              f"{c('SolicitudDuplicadaException')} muestra \"Solicitud duplicada\". Si se lanza una excepción no se ejecuta "
                                                              "el mensaje de éxito ni se limpia el formulario, así que el usuario puede corregir el dato."),
        (f"{c('try-catch')} en {c('cambiarEstado()')} de la ventana", f"Captura {c('SolicitudInvalidaException')} y muestra \"Cambio de estado no permitido\"."),
    ], [2.35, 4.71]),
    p("Las dos excepciones heredan de <b>Exception</b> y no de <b>RuntimeException</b>: son errores esperados del dominio que la interfaz "
      "siempre debe atender. Se separaron en dos clases porque piden respuestas distintas: un dato inválido se corrige en el formulario, "
      "mientras que un duplicado indica que la solicitud ya existe."),

    p("4. Diseño de clases y encapsulamiento", "Section"),
    p(f"Todos los atributos son {c('private')}. Estudiante, código, descripción y tipo además son {c('final')}: no cambian después de crear la "
      f"solicitud y solo se leen con getters. El único atributo que cambia es {c('estado')}, y solo mediante {c('cambiarEstado()')}, "
      f"que valida la transición. No hay {c('setEstado()')}: nadie puede saltarse el flujo."),
    p(f"El constructor valida antes de asignar, así que no puede existir un objeto {c('Solicitud')} con datos inválidos. "
      f"La lista de solicitudes es privada en {c('RegistroSolicitudes')}; la ventana solo puede usar {c('registrar()')} y {c('cambiarEstado()')}, "
      "por lo que la regla de duplicados no se puede evitar."),

    p("5. Retos de aplicación", "Section"),
    tabla(["Reto", "Solución"], [
        ("1. Consultar solicitudes", f"Una {c('JTable')} con columnas Estudiante, Código, Tipo, Estado y Descripción. Su modelo es {c('RegistroSolicitudes')}, "
                                     "así que cada registro o cambio de estado aparece de inmediato."),
        ("2. Cambiar el estado", f"Botón <b>Pasar a EN_PROCESO</b>: cambia la solicitud seleccionada de {c('PENDIENTE')} a {c('EN_PROCESO')}."),
        ("3. Aprobar o rechazar", f"Botones <b>Aprobar</b> y <b>Rechazar</b>: cambian una solicitud {c('EN_PROCESO')} a {c('APROBADA')} o {c('RECHAZADA')}. "
                                  f"Una solicitud debe revisarse antes de decidirla, y una decisión final no se puede cambiar. Si no hay fila seleccionada se muestra un aviso."),
        ("4. Validación adicional", f"{c('registrar()')} busca con {c('stream().anyMatch(...)')} otra solicitud con el mismo código y el mismo tipo. "
                                    "Un mismo estudiante sí puede tener solicitudes de tipos distintos."),
        ("5. Manejo de errores", f"{c('SolicitudDuplicadaException')}, capturada en su propio bloque {c('catch')}."),
    ], [1.6, 5.46]),
    PageBreak(),

    p("6. Evidencias de funcionamiento", "Section"),
    p("Las capturas muestran la ventana real del programa. Se generaron automáticamente con una herramienta que llena el formulario, "
      "pulsa los botones y guarda la imagen de la ventana y del mensaje que aparece. Se usó el aspecto multiplataforma de Swing (Metal), "
      "por eso la barra de título no es la del sistema operativo."),
    captura("00_ventana_inicial", "6.1 Ventana inicial",
            f"Formulario con los componentes pedidos. El {c('JComboBox')} se llenó con {c('TipoSolicitud.values()')}."),
    captura("01_registro_valido", "6.2 Registro válido",
            f"Datos correctos: la solicitud se crea en estado {c('PENDIENTE')}, aparece en la tabla y se confirma el registro."),
    captura("02_mismo_codigo_otro_tipo", "6.3 Mismo código, otro tipo (válido)",
            "Después de registrar dos solicitudes más, Laura Gómez registra una cancelación con el mismo código. Se acepta porque el tipo es distinto."),
    captura("03_nombre_vacio", "6.4 Inválido: nombre vacío",
            f"El constructor lanza {c('SolicitudInvalidaException')} y no se agrega ninguna fila."),
    captura("04_codigo_invalido", "6.5 Inválido: código con letras",
            f"\"20A1\" no cumple la expresión regular {c('\\d{5,12}')}: solo dígitos, entre 5 y 12."),
    captura("05_descripcion_vacia", "6.6 Inválido: descripción vacía",
            "La descripción solo tiene espacios; al quitarlos queda vacía."),
    captura("06_duplicada", "6.7 Inválido: solicitud duplicada (reto 5)",
            f"Ya existe un certificado para el código 202310045: se lanza {c('SolicitudDuplicadaException')}."),
    captura("07_en_proceso", "6.8 Cambio a EN_PROCESO (reto 2)",
            f"La primera solicitud pasó de {c('PENDIENTE')} a {c('EN_PROCESO')}."),
    captura("08_aprobada_rechazada", "6.9 Aprobar y rechazar (reto 3)",
            f"La primera solicitud fue aprobada, la segunda está en proceso y la tercera fue rechazada después de pasar por {c('EN_PROCESO')}."),
    captura("09_cambio_no_permitido", "6.10 Inválido: aprobar una solicitud PENDIENTE",
            f"{c('puedeCambiarA()')} no permite saltar de {c('PENDIENTE')} a {c('APROBADA')}; el estado no cambia."),
    captura("10_sin_seleccion", "6.11 Cambio de estado sin seleccionar una fila",
            "Se pide seleccionar una solicitud antes de cambiar su estado."),
    PageBreak(),

    p("7. Código fuente", "Section"),
    *[code(archivo) for archivo in FUENTES],
]


def footer(canvas, doc):
    canvas.saveState()
    canvas.setFont("Helvetica", 8)
    canvas.setFillColor(colors.HexColor("#526577"))
    canvas.drawString(.72 * inch, .45 * inch, "Taller 7 - POO")
    canvas.drawRightString(7.78 * inch, .45 * inch, f"Página {doc.page}")
    canvas.restoreState()


doc = SimpleDocTemplate(str(OUT), pagesize=letter, rightMargin=.72 * inch, leftMargin=.72 * inch, topMargin=.68 * inch, bottomMargin=.68 * inch,
                        title="Taller 7 - Excepciones y enumeraciones")
doc.build(story, onFirstPage=footer, onLaterPages=footer)
print(OUT)

package batallanaval.protocolo;

public enum TipoMensaje {
    // Conexión y autenticación
    LOGIN,                  // C -> S : texto = nickname
    LOGIN_OK,               // S -> C
    LOGIN_ERROR,            // S -> C : texto = motivo
    ESPERA,                 // S -> C : sala de espera
    PARTIDA_ENCONTRADA,     // S -> C : texto = nick del oponente
    // Posicionamiento
    COLOCAR_BARCOS,         // S -> C : pide la flota
    BARCOS_LISTOS,          // C -> S : barcos = posiciones
    ESPERAR_OPONENTE,       // S -> C : flota aceptada
    INICIO_JUEGO,           // S -> C
    // Juego
    TU_TURNO,               // S -> C
    TURNO_OPONENTE,         // S -> C
    DISPARO,                // C -> S : fila, col
    RESULTADO_DISPARO,      // S -> C (tirador) : fila, col, resultado, texto = barco hundido
    DISPARO_RECIBIDO,       // S -> C (defensor): fila, col, resultado, texto = barco hundido
    FIN_PARTIDA,            // S -> C : resultado = VICTORIA | DERROTA
    OPONENTE_DESCONECTADO,  // S -> C
    // Varios
    CHAT,                   // C -> S : texto ; S -> C : remitente + texto
    ERROR,                  // S -> C : texto
    SALIR                   // C -> S
}

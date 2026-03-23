# CamControl-API
![Minecraft 1.21.1](https://img.shields.io/badge/Minecraft-1.21.1-brightgreen.svg?logo=minecraft&logoColor=white)
![Fabric](https://img.shields.io/badge/Loader-Fabric-lightgrey.svg?logo=fabric)

**CamControl-API** es una API para controlar la cámara en Minecraft (Fabric). Sirve tanto para hacer cinemáticas como para que otros programadores la usen en sus propios mods. Puedes poner puntos en el mapa, añadir efectos de temblor y hacer que la cámara siga a cualquier mob o jugador automáticamente.

---

## Para Usuarios (Comandos)

Todos los comandos requieren nivel de permiso de OP (2 o superior).

### Creación de Cinemáticas
*   `/cam add [duración]`: Añade un punto de cámara en tu posición actual.
    *   *Ejemplo*: `/cam add 5.0` (Añade un punto con 5 segundos de transición desde el anterior).
*   `/cam add [duración] <entidad> [shake] [speed]`: Añade un punto que **mira fijamente** a una entidad (modo `lookat`).
*   `/cam save <nombre>`: Guarda tu sesión actual de puntos como una cinemática.
*   `/cam list`: Lista todas tus cinemáticas guardadas y los puntos de la sesión actual.
*   `/cam remove <nombre>`: Borra una cinemática guardada y limpia los puntos del mundo.
*   `/cam clear`: Limpia todos los puntos de la sesión actual sin borrar cinemáticas guardadas.

### Reproducción
*   `/cam play [jugadores] [nombre]`: Reproduce una cinemática para los jugadores seleccionados. Si no pones nombre, reproduce la sesión actual.
*   `/cam stop [jugadores]`: Detiene la reproducción de cualquier cinemática activa.

### Efectos Especiales (Shake)
*   `/cam effect shake <jugadores> start [nivel]`: Activa un temblor continuo (niveles 1-10).
*   `/cam effect shake <jugadores> stop`: Detiene el temblor.

---

## Para Desarrolladores (API)

El mod está diseñado para ser la base de cámaras de otros mods (modos historia, intros de partidas, eventos, etc.).

### Integración rápida
Para usar el mod en tu entorno de desarrollo, añade el `.jar` como dependencia y accede a la API:

```java
import dev.peter.api.CamControlAPI;

// Obtener la instancia de la API
CamControlAPI camAPI = CamControlAPI.getInstance();

// Reproducir una cinemática guardada para todos los jugadores
camAPI.playCinematic(server.getPlayerManager().getPlayerList(), "intro_del_juego");

// Activar un temblor de nivel 5 para un jugador específico
camAPI.startShake(List.of(player), 5);
```

### Métodos Disponibles
*   `playCinematic(players, name)`: Busca y reproduce una cinemática por nombre.
*   `playCinematic(players, keyframes)`: Reproduce una lista personalizada de `Keyframe` creada en tiempo real.
*   `stopCinematic(players)`: Cancela cualquier animación.
*   `startShake(players, level)`: Activa el efecto de agitación rotacional orgánico.
*   `stopShake(players)`: Detiene el efecto.

---

## Qué ofrece el mod
*   **Movimientos suaves**: La cámara va fluida incluso si estás siguiendo a alguien.
*   **Temblor realista**: El efecto "shake" no solo mueve la cámara de lado a lado, sino que usa rotación (Yaw y Pitch) para que parezca de verdad.
*   **Seguimiento de objetivos**: Con el modo LookAt la cámara no quita el ojo de cualquier entidad (jugadores, mobs, etc).
*   **No pierdes nada**: Los puntos que pongas se guardan solos. Si cierras el juego o el servidor se siguen guardando.

---

## Instalación
1.  Descarga el archivo `.jar` de la versión correspondiente.
2.  Cópialo en la carpeta `mods` de tu servidor y cliente de Minecraft.
3.  Asegúrate de tener instalado [Fabric API](https://curseforge.com/minecraft/mc-mods/fabric-api).

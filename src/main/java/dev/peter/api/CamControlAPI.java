package dev.peter.api;

import dev.peter.util.Keyframe;
import net.minecraft.server.network.ServerPlayerEntity;
import java.util.List;

/**
 * Main entry point for the CamControl API.
 * This interface provides methods to control camera cinematics and effects for players.
 */
public interface CamControlAPI {
    
    /**
     * @return The singleton instance of the CamControl API.
     */
    static CamControlAPI getInstance() {
        return dev.peter.CamControl.getApi();
    }

    /**
     * Plays a saved cinematic for specified players.
     * @param players The players to play the cinematic for.
     * @param name The name of the saved cinematic.
     * @return true if the cinematic was found and started, false otherwise.
     */
    boolean playCinematic(List<ServerPlayerEntity> players, String name);

    /**
     * Plays a custom list of keyframes for specified players.
     * @param players The players to play the cinematic for.
     * @param keyframes The list of keyframes defining the path.
     */
    void playCinematic(List<ServerPlayerEntity> players, List<Keyframe> keyframes);

    /**
     * Stops any active cinematic for specified players.
     * @param players The players to stop.
     */
    void stopCinematic(List<ServerPlayerEntity> players);

    /**
     * Starts a continuous camera shake for specified players.
     * @param players The targets for the shake.
     * @param level The intensity level (1-10).
     */
    void startShake(List<ServerPlayerEntity> players, int level);

    /**
     * Stops any continuous camera shake for specified players.
     * @param players The targets to stop shaking.
     */
    void stopShake(List<ServerPlayerEntity> players);
}

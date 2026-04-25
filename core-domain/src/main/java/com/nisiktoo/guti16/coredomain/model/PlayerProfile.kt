package com.nisiktoo.guti16.coredomain.model
import androidx.compose.ui.graphics.Color
import com.nisiktoo.guti16.core.gameengine.model.Player

/**
 * Represents a player's profile, including their name, avatar, and appearance settings.
 * @param id Unique identifier for the player profile.
 * @param name The player's display name.
 * @param player The player side (A or B) associated with this profile.
 * @param avatarUrl Optional URL to the player's avatar image.
 * @param appearance The player's appearance settings, including piece color and border color.
 */

data class PlayerProfile (
    val id: String,
    val name: String,
    val player: Player,
    val appearance: PlayerAppearance,
    val avatarUrl: String? = null,
)


/**
 * Represents the appearance settings for a player's pieces, including colors for the piece, border, and selected glow.
 * @param pieceColor The color of the player's pieces.
 * @param borderColor The color of the border around the player's pieces.
 * @param selectedGlowColor The color of the glow effect when a piece is selected.
 */
data class PlayerAppearance (
    var pieceColor : Color,
    var borderColor: Color,
    var selectedGlowColor: Color,
)
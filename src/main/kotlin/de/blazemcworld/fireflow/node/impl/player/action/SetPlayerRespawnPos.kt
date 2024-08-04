package de.blazemcworld.fireflow.node.impl.player.action

import de.blazemcworld.fireflow.node.*
import net.minestom.server.item.Material

object SetPlayerRespawnPos : BaseNode("Set Respawn Pos", Material.RED_BED) {
    private val signal = input("Signal", SignalType)
    private val player = input("Player", PlayerType)
    private val pos = input("Position", PositionType)
    private val next = output("Next", SignalType)

    override fun setup(ctx: NodeContext) {
        ctx[signal].signalListener = { eval ->
            run {
                (eval[ctx[player]]?.resolve() ?: return@run).respawnPoint = eval[ctx[pos]] ?: return@run
            }
            eval.emit(ctx[next])
        }
    }
}
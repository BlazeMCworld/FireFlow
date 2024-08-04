package de.blazemcworld.fireflow.node.impl.player.info

import de.blazemcworld.fireflow.node.BaseNode
import de.blazemcworld.fireflow.node.NodeContext
import de.blazemcworld.fireflow.node.NumberType
import de.blazemcworld.fireflow.node.PlayerType
import net.minestom.server.item.Material

object GetPlayerFlyingSpeed : BaseNode("Get Flying Speed", Material.FEATHER) {
    private val player = input("Player", PlayerType)
    private val speed = output("Speed", NumberType)

    override fun setup(ctx: NodeContext) {
        ctx[speed].defaultHandler = eval@{ eval ->
            run {
                return@eval (eval[ctx[player]]?.resolve() ?: return@run).flyingSpeed.toDouble() * 20
            }
            null
        }
    }
}
package de.blazemcworld.fireflow.tool

import de.blazemcworld.fireflow.node.FunctionCallNode
import de.blazemcworld.fireflow.node.FunctionInputsNode
import de.blazemcworld.fireflow.node.FunctionOutputsNode
import de.blazemcworld.fireflow.space.Space
import net.kyori.adventure.text.format.NamedTextColor
import net.minestom.server.entity.Player
import net.minestom.server.item.Material

object DeleteNodeTool : Tool {
    override val item = item(Material.REDSTONE_BLOCK,
        "Delete", NamedTextColor.RED,
        "Used for deleting nodes",
        "or connections in the code"
    )

    override fun handler(player: Player, space: Space) = object : Tool.Handler {
        override val tool = DeleteNodeTool

        override fun use() {
            val cursor = space.codeCursor(player)
            space.codeNodes.find { it.includes(cursor) }?.let {
                if (it.isBeingMoved) return
                if (it.node is FunctionInputsNode) {
                    if (space.codeNodes.any { call -> call.node is FunctionCallNode && call.node.fn == it.node.fn }) return
                }
                if (it.node is FunctionOutputsNode) {
                    if (space.codeNodes.any { call -> call.node is FunctionCallNode && call.node.fn == it.node.fn }) return
                }
                it.remove()
                space.codeNodes.remove(it)
                if (it.node is FunctionInputsNode) {
                    space.functions.removeIf { other ->
                        other.first.fn == it.node.fn
                    }
                    space.functionNodes.removeIf { f -> f.fn == it.node.fn }
                    space.codeNodes.removeIf { other ->
                        if (other.node !is FunctionOutputsNode || other.node.fn != it.node.fn) return@removeIf false
                        other.remove()
                        return@removeIf true
                    }
                }
                if (it.node is FunctionOutputsNode) {
                    space.functions.removeIf { other ->
                        other.first.fn == it.node.fn
                    }
                    space.functionNodes.removeIf { f -> f.fn == it.node.fn }
                    space.codeNodes.removeIf { other ->
                        if (other.node !is FunctionInputsNode || other.node.fn != it.node.fn) return@removeIf false
                        other.remove()
                        return@removeIf true
                    }
                }
                return
            }

            for (node in space.codeNodes) {
                for (input in node.inputs) {
                    for (line in input.lines) {
                        if (line.distance(cursor) < 0.1) {
                            val output = input.lineOutputMap[line]
                            input.connections.remove(output)
                            output?.connections?.remove(input)
                            input.update(space.codeInstance)
                            return
                        }
                    }
                }
            }
        }
    }
}
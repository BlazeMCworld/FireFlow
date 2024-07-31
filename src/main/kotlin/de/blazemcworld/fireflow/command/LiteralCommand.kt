package de.blazemcworld.fireflow.command

import de.blazemcworld.fireflow.node.impl.ValueLiteralNode
import de.blazemcworld.fireflow.space.SpaceManager
import de.blazemcworld.fireflow.util.sendError
import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.CommandExecutor
import net.minestom.server.command.builder.arguments.ArgumentString
import net.minestom.server.entity.Player

object LiteralCommand : Command("literal") {
    init {
        addSyntax(CommandExecutor exec@{ sender, ctx ->
            if (sender !is Player) return@exec

            val space = SpaceManager.currentSpace(sender)

            if (space == null || space.codeInstance != sender.instance) {
                sender.sendError("You must be coding to use this!")
                return@exec
            }

            val cursor = space.codeCursor(sender)
            space.codeNodes.find { it.node is ValueLiteralNode<*> && it.includes(cursor) }?.let {
                it.valueLiteral = ctx.get("value")
                it.update(space.codeInstance)
                if ((it.node as ValueLiteralNode<*>).type.parse(it.valueLiteral, space) == null) {
                    sender.sendError("Value seems invalid!")
                }
                return@exec
            }
            sender.sendError("You must be looking at a value literal node for this!")
        }, ArgumentString("value"))
    }
}
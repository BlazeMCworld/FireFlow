package de.blazemcworld.fireflow;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.node.NodeList;
import de.blazemcworld.fireflow.code.node.option.InputOptions;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.text.TextColor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class WikiGenerator {

    private static StringBuilder sidebar;
    private static int sidebarDepth = 0;

    public static void generate() {
        FireFlow.LOGGER.info("Generating wiki...");
        NodeList.init();

        sidebar = new StringBuilder();
        sidebar("Nodes", "nodes");
        sidebarDepth++;
        generateCategory(NodeList.root, "nodes");
        sidebarDepth--;

        try {
            Path p = Path.of("wiki/_sidebar.md");
            if (!Files.exists(p.getParent())) Files.createDirectories(p.getParent());
            Files.writeString(p, sidebar.toString(), StandardOpenOption.APPEND);
            FireFlow.LOGGER.info("Generated sidebar.md");
        } catch (IOException e) {
            FireFlow.LOGGER.error("Failed to write sidebar.md!", e);
        }

        FireFlow.LOGGER.info("Done!");
    }


    public static void sidebar(String name, String path) {
        sidebar.append("  ".repeat(sidebarDepth)).append("* [").append(name).append("](/").append(path).append(".md)\n");
    }

    private static void generateCategory(NodeList.Category category, String path) {
        StringBuilder sb = new StringBuilder();
        sb.append("# ");
        if (category.icon != null) sb.append(" ").append(icon(category.icon));
        sb.append(category.name);
        sb.append(" (").append(category.collectNodes().size()).append(" nodes)");
        sb.append("\n\n");

        if (!category.categories.isEmpty()) {
            sb.append("## Categories\n");
            for (NodeList.Category c : category.categories) {
                if (c.isFunctions) continue;
                String newPath = path + "/" + c.name.toLowerCase().replace(' ', '_');
                sidebar(c.name, newPath);
                sidebarDepth++;
                generateCategory(c, newPath);
                sidebarDepth--;
                sb.append("- ");
                if (c.icon != null) sb.append(icon(c.icon));
                sb.append("[").append(c.name).append("](/").append(path).append("/").append(c.name.toLowerCase().replace(' ', '_')).append(".md)\n");
            }
            sb.append("\n");
        }

        if (!category.nodes.isEmpty()) {
            sb.append("## Nodes\n");
            sb.append("<div style=\"display: grid; grid-template-columns: repeat(5, 1fr); text-align: center;\">\n");
            for (Node node : category.nodes) {
                generateNode(node, path + "/" + node.name.toLowerCase().replace(' ', '_'));
                sb.append("<a href=\"#/").append(path).append("/").append(node.name.toLowerCase().replace(' ', '_')).append("\">\n");
                sb.append(icon(node.icon, "3em")).append("<br>\n");
                sb.append(node.name).append("</a>\n");
            }
            sb.append("</div>\n");
        }

        try {
            Path p = Path.of("wiki/" + path + ".md");
            if (!Files.exists(p.getParent())) Files.createDirectories(p.getParent());
            Files.writeString(p, sb.toString());
            FireFlow.LOGGER.info("Generated {}.md", path);
        } catch (IOException e) {
            FireFlow.LOGGER.error("Failed to write {}.md!", path, e);
        }
    }

    private static void generateNode(Node node, String path) {
        StringBuilder sb = new StringBuilder();
        sb.append("# ").append(icon(node.icon)).append(node.name).append("\n\n");
        sb.append(node.description).append("\n\n");
        if (!node.inputs.isEmpty()) {
            sb.append("**Inputs**\n");
            for (Node.Input<?> input : node.inputs) {
                sb.append("- ");
                if (input.type != null) sb.append(colorStart(input.type.color));
                sb.append(input.name);
                if (input.type != null) sb.append(": ").append(icon(input.type.icon)).append(input.type.getName());
                if (input.options instanceof InputOptions.Choice choice) {
                    sb.append("  \nOne of:\n");
                    for (String option : choice.list()) {
                        sb.append("  - ").append(option).append("\n");
                    }
                }
                if (input.varargsParent != null) sb.append(" (Varargs)");
                if (input.type != null) sb.append(colorEnd());
                sb.append("\n");
            }
            sb.append("\n");
        }
        if (!node.outputs.isEmpty()) {
            sb.append("**Outputs**\n");
            for (Node.Output<?> output : node.outputs) {
                sb.append("- ");
                if (output.type != null) sb.append(colorStart(output.type.color));
                sb.append(output.name);
                if (output.type != null) {
                    sb.append(" (").append(icon(output.type.icon)).append(output.type.getName()).append(")").append(colorEnd());
                }
                sb.append("\n");
            }
        }
        try {
            Path p = Path.of("wiki/" + path + ".md");
            if (!Files.exists(p.getParent())) Files.createDirectories(p.getParent());
            Files.writeString(p, sb.toString());
            FireFlow.LOGGER.info("Generated {}.md", path);
        } catch (IOException e) {
            FireFlow.LOGGER.error("Failed to write {}.md!", path, e);
        }
    }

    private static String icon(Item icon) {
        return icon(icon, "1em");
    }

    private static String icon(Item m, String size) {
        return "<img src=\"https://rawcdn.githack.com/Owen1212055/mc-assets/refs/heads/main/assets/" + Registries.ITEM.getId(m).getPath().toUpperCase() + ".png\" style=\"height: " + size + "; transform: translateY(0.1em);\">";
    }

    private static String colorStart(TextColor color) {
        return "<span style=\"color: " + color.getHexCode() + " !important\">";
    }

    private static String colorEnd() {
        return "</span>";
    }

}

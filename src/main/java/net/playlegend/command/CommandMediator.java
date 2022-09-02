package net.playlegend.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import net.playlegend.LegendPerm;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class CommandMediator implements CommandExecutor, TabCompleter {

    private final LegendPerm plugin;
    private final CommandDispatcher<Object> dispatcher;

    public CommandMediator(LegendPerm plugin, CommandDispatcher<Object> dispatcher) {
        this.plugin = plugin;
        this.dispatcher = dispatcher;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                dispatcher.execute(buildCommand(label, args), sender);
            } catch (CommandSyntaxException e) {
                sender.sendMessage(e.getMessage());
            }
        });
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        ParseResults<Object> parse = dispatcher.parse(buildCommand(label, args), sender);

        CompletableFuture<Suggestions> completionSuggestions = dispatcher.getCompletionSuggestions(parse);

        Suggestions suggestions;
        try {
            suggestions = completionSuggestions.get(20, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            e.printStackTrace();
            return null;
        }

        List<String> result = new ArrayList<>();
        for (Suggestion suggestion : suggestions.getList()) {
            if (suggestion.getTooltip() != null) {
                result.add(suggestion.getTooltip().getString());
            }

            if (!suggestion.getText().isBlank()) {
                result.add(suggestion.getText());
            }
        }

        return result;
    }

    private String buildCommand(String label, String[] args) {
        return label + " " + String.join(" ", args);
    }

}

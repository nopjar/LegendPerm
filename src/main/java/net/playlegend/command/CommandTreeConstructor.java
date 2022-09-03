package net.playlegend.command;

import static com.mojang.brigadier.arguments.BoolArgumentType.bool;
import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static com.mojang.brigadier.arguments.StringArgumentType.string;
import static com.mojang.brigadier.builder.LiteralArgumentBuilder.literal;
import static com.mojang.brigadier.builder.RequiredArgumentBuilder.argument;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.concurrent.CompletableFuture;
import net.playlegend.LegendPerm;
import net.playlegend.domain.Group;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

class CommandTreeConstructor {

    private final LegendPerm plugin;

    public CommandTreeConstructor(LegendPerm plugin) {
        this.plugin = plugin;
    }

    public LiteralArgumentBuilder<Object> construct() {
        return literal("lp")
                .then(literal("test")
                        .then(argument("user", string())
                                .then(argument("perm", string())
                                        .executes(context -> {
                                            CommandSender sender = (CommandSender) context.getSource();
                                            Player player = Bukkit.getServer().getPlayerExact(context.getArgument("user", String.class));
                                            if (player == null) {
                                                sender.sendMessage("Player not online!");
                                                return 1;
                                            }

                                            boolean has = player.hasPermission(context.getArgument("perm", String.class));
                                            sender.sendMessage("Has: " + (has ? "§aTrue" : "§cFalse"));
                                            return 1;
                                        })
                                )
                        )
                )
                .then(literal("info")
                        .executes(new InfoCommand(plugin))
                )
                .then(literal("group")
                        .then(literal("list")
                                .executes(new ShowAllGroupsCommand(plugin))
                        )
                        .then(argument("groupName", string()).suggests(new CustomTooltip("groupName"))
                                .then(literal("create")
                                        .executes(new GroupCreateCommand(plugin))
                                        .then(argument("weight", integer()).suggests(new CustomTooltip("weight"))
                                                .executes(new GroupCreateCommand(plugin))
                                                .then(argument("prefix", string()).suggests(new CustomTooltip("prefix"))
                                                        .executes(new GroupCreateCommand(plugin))
                                                        .then(argument("suffix", string()).suggests(new CustomTooltip("suffix"))
                                                                .executes(new GroupCreateCommand(plugin))
                                                        )
                                                )
                                        )
                                )
                                .then(literal("delete")
                                        .executes(new GroupDeleteCommand(plugin))
                                )
                                .then(literal("info")
                                        .executes(new GroupInfoCommand(plugin))
                                )
                                .then(literal("add")
                                        .then(argument("permissionNode", string()).suggests(new CustomTooltip("permissionNode"))
                                                .then(argument("mode", bool()).suggests(new CustomTooltip("mode"))
                                                        .executes(new AddPermissionToGroupCommand(plugin))
                                                )
                                        )
                                )
                                .then(literal("remove")
                                        .then(argument("permissionNode", string()).suggests(new CustomTooltip("permissionNode"))
                                                .executes(new RemovePermissionFromGroupCommand(plugin))
                                        )
                                )
                                .then(literal("set")
                                        .then(argument("key", string()).suggests(new CustomSuggestions(Group.Property.VALUES_AS_STRINGS))
                                                .executes(new GroupSetPropertyCommand(plugin))
                                                .then(argument("value", string()).suggests(new CustomTooltip("value"))
                                                        .executes(new GroupSetPropertyCommand(plugin))
                                                )
                                        )
                                )
                        )
                )
                .then(literal("user")
                        .then(argument("userName", string()).suggests(new CustomTooltip("userName"))
                                .then(literal("info")
                                        .executes(new UserInfoCommand(plugin))
                                )
                                .then(literal("add")
                                        .then(argument("groupName", string()).suggests(new CustomTooltip("groupName"))
                                                .executes(new AddUserToGroupCommand(plugin))
                                                .then(argument("time", string()).suggests(new CustomTooltip("time"))
                                                        .executes(new AddUserToGroupCommand(plugin))
                                                )
                                        )
                                )
                                .then(literal("remove")
                                        .then(argument("groupName", string()).suggests(new CustomTooltip("groupName"))
                                                .executes(new RemoveUserFromGroupCommand(plugin))
                                                .then(argument("time", string()).suggests(new CustomTooltip("time"))
                                                        .executes(new RemoveUserFromGroupCommand(plugin))
                                                )
                                        )
                                )
                        )
                );
    }

    private class CustomSuggestions implements SuggestionProvider<Object> {

        private final String[] strings;

        public CustomSuggestions(String... strings) {
            this.strings = strings;
        }

        @Override
        public CompletableFuture<Suggestions> getSuggestions(CommandContext<Object> context, SuggestionsBuilder builder) throws CommandSyntaxException {
            for (String string : strings) {
                builder.suggest(string);
            }

            return builder.buildFuture();
        }

    }

    private class CustomTooltip implements SuggestionProvider<Object> {

        private final String tooltip;

        public CustomTooltip(String tooltip) {
            this.tooltip = tooltip;
        }

        @Override
        public @NotNull CompletableFuture<Suggestions> getSuggestions(CommandContext<Object> context, SuggestionsBuilder builder) throws CommandSyntaxException {
            return builder.suggest("<" + tooltip + ">").buildFuture();
        }

    }

}

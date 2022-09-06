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
import net.playlegend.configuration.ConfigurationService;
import net.playlegend.configuration.MessageConfig;
import net.playlegend.domain.Group;
import org.jetbrains.annotations.NotNull;

class CommandTreeConstructor {

    private final LegendPerm plugin;

    public CommandTreeConstructor(LegendPerm plugin) {
        this.plugin = plugin;
    }

    public LiteralArgumentBuilder<Object> construct() {
        MessageConfig config = plugin.getServiceRegistry()
                .get(ConfigurationService.class)
                .get(MessageConfig.class);
        return literal("lp")
                .then(literal("info")
                        .executes(new InfoCommand(plugin, config))
                )
                .then(literal("group")
                        .then(literal("list")
                                .executes(new ShowAllGroupsCommand(plugin, config))
                        )
                        .then(argument("groupName", string()).suggests(new CustomTooltip("groupName"))
                                .then(literal("create")
                                        .executes(new GroupCreateCommand(plugin, config))
                                        .then(argument("weight", integer()).suggests(new CustomTooltip("weight"))
                                                .executes(new GroupCreateCommand(plugin, config))
                                                .then(argument("prefix", string()).suggests(new CustomTooltip("prefix"))
                                                        .executes(new GroupCreateCommand(plugin, config))
                                                        .then(argument("suffix", string()).suggests(new CustomTooltip("suffix"))
                                                                .executes(new GroupCreateCommand(plugin, config))
                                                        )
                                                )
                                        )
                                )
                                .then(literal("delete")
                                        .executes(new GroupDeleteCommand(plugin, config))
                                )
                                .then(literal("info")
                                        .executes(new GroupInfoCommand(plugin, config))
                                )
                                .then(literal("add")
                                        .then(argument("permissionNode", string()).suggests(new CustomTooltip("permissionNode"))
                                                .then(argument("mode", bool()).suggests(new CustomTooltip("mode"))
                                                        .executes(new AddPermissionToGroupCommand(plugin, config))
                                                )
                                        )
                                )
                                .then(literal("remove")
                                        .then(argument("permissionNode", string()).suggests(new CustomTooltip("permissionNode"))
                                                .executes(new RemovePermissionFromGroupCommand(plugin, config))
                                        )
                                )
                                .then(literal("set")
                                        .then(argument("key", string()).suggests(new CustomSuggestions(Group.Property.VALUES_AS_STRINGS))
                                                .executes(new GroupSetPropertyCommand(plugin, config))
                                                .then(argument("value", string()).suggests(new CustomTooltip("value"))
                                                        .executes(new GroupSetPropertyCommand(plugin, config))
                                                )
                                        )
                                )
                        )
                )
                .then(literal("user")
                        .then(argument("userName", string()).suggests(new CustomTooltip("userName"))
                                .then(literal("info")
                                        .executes(new UserInfoCommand(plugin, config))
                                )
                                .then(literal("add")
                                        .then(argument("groupName", string()).suggests(new CustomTooltip("groupName"))
                                                .executes(new AddUserToGroupCommand(plugin, config))
                                                .then(argument("time", string()).suggests(new CustomTooltip("time"))
                                                        .executes(new AddUserToGroupCommand(plugin, config))
                                                )
                                        )
                                )
                                .then(literal("remove")
                                        .then(argument("groupName", string()).suggests(new CustomTooltip("groupName"))
                                                .executes(new RemoveUserFromGroupCommand(plugin, config))
                                                .then(argument("time", string()).suggests(new CustomTooltip("time"))
                                                        .executes(new RemoveUserFromGroupCommand(plugin, config))
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

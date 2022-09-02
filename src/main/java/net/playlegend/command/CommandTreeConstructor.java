package net.playlegend.command;

import static com.mojang.brigadier.arguments.BoolArgumentType.bool;
import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static com.mojang.brigadier.arguments.StringArgumentType.string;
import static com.mojang.brigadier.builder.LiteralArgumentBuilder.literal;
import static com.mojang.brigadier.builder.RequiredArgumentBuilder.argument;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.concurrent.CompletableFuture;
import net.playlegend.LegendPerm;
import net.playlegend.domain.Group;
import org.jetbrains.annotations.NotNull;

class CommandTreeConstructor {

    public LiteralArgumentBuilder<Object> construct() {
        return literal("lp")
                .then(literal("info")
                        .executes(new InfoCommand(LegendPerm.getInstance()))
                )
                .then(literal("group")
                        .then(literal("list")
                                .executes(new ShowAllGroupsCommand(LegendPerm.getInstance()))
                        )
                        .then(argument("groupName", string()).suggests(new CustomTooltip("groupName"))
                                .then(literal("create")
                                        .executes(new CreateGroupCommand(LegendPerm.getInstance()))
                                        .then(argument("weight", integer()).suggests(new CustomTooltip("weight"))
                                                .executes(new CreateGroupCommand(LegendPerm.getInstance()))
                                                .then(argument("prefix", string()).suggests(new CustomTooltip("prefix"))
                                                        .executes(new CreateGroupCommand(LegendPerm.getInstance()))
                                                        .then(argument("suffix", string()).suggests(new CustomTooltip("suffix"))
                                                                .executes(new CreateGroupCommand(LegendPerm.getInstance()))
                                                        )
                                                )
                                        )
                                )
                                .then(literal("info")
                                        .executes(new GroupInfoCommand(LegendPerm.getInstance()))
                                )
                                .then(literal("add")
                                        .then(argument("permissionNode", string()).suggests(new CustomTooltip("permissionNode"))
                                                .then(argument("mode", bool()).suggests(new CustomTooltip("mode"))
                                                        .executes(new AddPermissionToGroupCommand(LegendPerm.getInstance()))
                                                )
                                        )
                                )
                                .then(literal("remove")
                                        .then(argument("permissionNode", string()).suggests(new CustomTooltip("permissionNode"))
                                                .executes(new RemovePermissionFromGroupCommand(LegendPerm.getInstance()))
                                        )
                                )
                                .then(literal("set")
                                        .then(argument("key", string()).suggests(new CustomSuggestions(Group.Property.VALUES_AS_STRINGS))
                                                .executes(new GroupSetPropertyCommand(LegendPerm.getInstance()))
                                                .then(argument("value", string()).suggests(new CustomTooltip("value"))
                                                        .executes(new GroupSetPropertyCommand(LegendPerm.getInstance()))
                                                )
                                        )
                                )
                        )
                )
                .then(literal("user")
                        .then(argument("userName", string()).suggests(new CustomTooltip("userName"))
                                .then(literal("info")
                                        .executes(new UserInfoCommand(LegendPerm.getInstance()))
                                )
                                .then(literal("add")
                                        .then(argument("groupName", string()).suggests(new CustomTooltip("groupName"))
                                                .executes(new AddUserToGroupCommand(LegendPerm.getInstance()))
                                                .then(argument("time", string()).suggests(new CustomTooltip("time"))
                                                        .executes(new AddUserToGroupCommand(LegendPerm.getInstance()))
                                                )
                                        )
                                )
                                .then(literal("remove")
                                        .then(argument("groupName", string()).suggests(new CustomTooltip("groupName"))
                                                .executes(new RemoveUserFromGroupCommand(LegendPerm.getInstance()))
                                                .then(argument("time", string()).suggests(new CustomTooltip("time"))
                                                        .executes(new RemoveUserFromGroupCommand(LegendPerm.getInstance()))
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
            return builder.suggest("", new LiteralMessage("<" + tooltip + ">")).buildFuture();
        }

    }

}

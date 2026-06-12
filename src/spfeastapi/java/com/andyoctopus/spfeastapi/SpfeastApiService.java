package com.andyoctopus.spfeastapi;

import com.andyoctopus.spfeastbans.BanTemplate;
import com.andyoctopus.spfeastbans.BanTemplates;
import com.andyoctopus.spfeastbans.SpfeastBansPlugin;
import com.andyoctopus.spfeastbans.ban.BanEntry;
import com.andyoctopus.spfeastbans.ban.BanService;
import com.andyoctopus.spfeastbans.mute.MuteEntry;
import com.andyoctopus.spfeastbans.mute.MuteReason;
import com.andyoctopus.spfeastbans.mute.MuteService;
import com.andyoctopus.spfeastbans.util.CommandMessages;
import com.andyoctopus.spfeastbans.util.DurationParser;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;

final class SpfeastApiService implements SpfeastApi {
    private final SpfeastBansPlugin bansPlugin;

    SpfeastApiService(SpfeastBansPlugin bansPlugin) {
        this.bansPlugin = bansPlugin;
    }

    @Override
    public PunishmentResult tempBan(String targetQuery, String templateKey, String durationText, String reason, String actorName) {
        BanTemplate template = BanTemplates.get(templateKey);
        if (template == null) {
            return PunishmentResult.failure("Unknown template: " + templateKey);
        }

        Long parsedDuration = DurationParser.parseDurationMillis(durationText);
        if (parsedDuration == null || parsedDuration <= 0L) {
            return PunishmentResult.failure("Invalid duration: " + durationText);
        }

        BanService banService = bansPlugin.getBanService();
        Optional<BanService.ResolvedTarget> targetOptional = banService.resolveTarget(targetQuery);
        if (targetOptional.isEmpty()) {
            return PunishmentResult.failure("Target not found: " + targetQuery);
        }

        BanService.ResolvedTarget target = targetOptional.get();
        String resolvedReason = banService.resolveReason(template, reason);
        long expiresAtMillis = System.currentTimeMillis() + parsedDuration;
        BanEntry entry = banService.ban(target.uniqueId(), target.playerName(), templateKey, resolvedReason, actorName, expiresAtMillis);

        CommandSender actorSender = resolveActorSender(actorName);
        CommandMessages.broadcastPublicRemoval(entry.getPlayerName(), actorSender);
        banService.kickIfOnline(entry);
        return PunishmentResult.success(entry.getUniqueId(), entry.getPlayerName(), entry.getBanId());
    }

    @Override
    public PunishmentResult tempMute(String targetQuery, String reasonKey, String durationText, String actorName) {
        MuteReason reason = MuteReason.get(reasonKey);
        if (reason == null) {
            return PunishmentResult.failure("Unknown mute reason: " + reasonKey);
        }

        long durationMillis = reason.getDefaultDurationMillis();
        if (durationText != null && !durationText.isBlank()) {
            Long parsedDuration = DurationParser.parseDurationMillis(durationText);
            if (parsedDuration == null || parsedDuration <= 0L) {
                return PunishmentResult.failure("Invalid duration: " + durationText);
            }
            durationMillis = parsedDuration;
        }

        BanService banService = bansPlugin.getBanService();
        Optional<BanService.ResolvedTarget> targetOptional = banService.resolveTarget(targetQuery);
        if (targetOptional.isEmpty()) {
            return PunishmentResult.failure("Target not found: " + targetQuery);
        }

        BanService.ResolvedTarget target = targetOptional.get();
        MuteService muteService = bansPlugin.getMuteService();
        long expiresAtMillis = System.currentTimeMillis() + durationMillis;
        MuteEntry entry = muteService.mute(target.uniqueId(), target.playerName(), reason, actorName, expiresAtMillis);
        return PunishmentResult.success(entry.getUniqueId(), entry.getPlayerName(), entry.getMuteId());
    }

    @Override
    public List<String> getBanTemplateKeys() {
        return BanTemplates.getKeys();
    }

    @Override
    public List<String> getMuteReasonKeys() {
        return MuteReason.getKeys();
    }

    private CommandSender resolveActorSender(String actorName) {
        if (actorName == null || actorName.isBlank()) {
            return Bukkit.getConsoleSender();
        }
        Player player = Bukkit.getPlayerExact(actorName);
        return player != null ? player : Bukkit.getConsoleSender();
    }
}

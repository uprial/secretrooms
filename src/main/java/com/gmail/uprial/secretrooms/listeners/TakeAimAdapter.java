package com.gmail.uprial.secretrooms.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

import java.lang.reflect.InvocationTargetException;

/*
    Make sure you register the listener in JavaPlugin::onEnable, e.g.

    getServer().getPluginManager().registerEvents(new TakeAimAdapter(), this);
 */
public class TakeAimAdapter implements Listener {

    private static class TakeAimEntityTargetEvent extends EntityTargetEvent {
        private final TakeAimEntityTargetCallback callback;

        public TakeAimEntityTargetEvent(final Mob entity,
                                        final Player target,
                                        final TargetReason reason,
                                        final TakeAimEntityTargetCallback callback) {

            super(entity, target, reason);
            this.callback = callback;
        }

        public void handle(final EntityTargetEvent event) {
            final Mob mob = (Mob)event.getEntity();
            final Player player = (Player)event.getTarget();

            mob.setTarget(player);
            callback.call(mob, player);
        }
    }

    // MONITOR to let the previous listeners cancel the event
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityTarget(EntityTargetEvent event) {
        if(event instanceof TakeAimEntityTargetEvent) {
            ((TakeAimEntityTargetEvent)event).handle(event);
        }
    }

    public interface TakeAimEntityTargetCallback {
        void call(final Mob mob, final Player player);
    }

    public static void setTarget(final Mob source,
                                 final Player target,
                                 final EntityTargetEvent.TargetReason reason) {
        setTarget(source, target, reason, (final Mob mob, final Player player) -> {});
    }

    public static void setTarget(final Mob source,
                                 final Player target,
                                 final EntityTargetEvent.TargetReason reason,
                                 final TakeAimEntityTargetCallback callback) {
        /*
            Send an event to TakeAim, which Mob::setTarget does not.

            This event-based approach also allows other plugins
            to cancel targeting, e.g., VanishNoPacket.
         */
        Bukkit.getPluginManager().callEvent(
                new TakeAimEntityTargetEvent(source, target, reason, callback));
    }

    public interface LaunchFireballCallback <T extends Fireball> {
        void call(final T fireball);
    }

    public static <T extends Fireball> void launchFireball(final Mob source,
                                                           final Player target,
                                                           final EntityTargetEvent.TargetReason reason,
                                                           final Class<? extends T> tFireball) {
        launchFireball(source, target, reason, tFireball, (final T fireball) -> {});
    }

    public static <T extends Fireball> void launchFireball(final Mob source,
                                                           final Player target,
                                                           final EntityTargetEvent.TargetReason reason,
                                                           final Class<? extends T> tFireball,
                                                           final LaunchFireballCallback<T> callback) {
        setTarget(source, target, reason, (final Mob mob, final Player player) -> {
            T fireball = source.launchProjectile(tFireball);

            if(!isTakeAimEnabled()) {
                final Location targetLocation = getAimPoint(target);
                targetLocation.subtract(fireball.getLocation());

                final Vector newAcceleration = targetLocation.toVector();
                newAcceleration.multiply(fireball.getAcceleration().length() / targetLocation.length());

                fireball.setAcceleration(newAcceleration);
            }

            callback.call(fireball);
        });
    }

    // According to TakeAim:ProjectileHoming
    public static Location getAimPoint(final Player targetPlayer) {
        return targetPlayer.getLocation()
                .add(targetPlayer.getEyeLocation())
                .multiply(0.5D);
    }

    private static class TakeAimIntegrationError extends RuntimeException {
        TakeAimIntegrationError(final Throwable cause) {
            super(cause);
        }
    }

    private static boolean isTakeAimEnabled() {
        final Plugin plugin = Bukkit.getPluginManager().getPlugin("TakeAim");
        if(plugin == null) {
            return false;
        }
        try {
            final Object config = plugin.getClass().getMethod("getTakeAimConfig").invoke(plugin);

            return (Boolean)config.getClass().getMethod("isEnabled").invoke(config);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new TakeAimIntegrationError(e);
        }
    }
}
